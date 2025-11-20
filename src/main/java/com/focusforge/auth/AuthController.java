// com.focusforge.auth.AuthController
package com.focusforge.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;
    private final GoogleIdTokenVerifier verifier;
    private final WebClient web = WebClient.builder().build();

    public AuthController(JwtService jwtService, GoogleIdTokenVerifier verifier) {
        this.jwtService = jwtService;
        this.verifier = verifier;
    }

    // --------- GOOGLE (existing) ----------
    @PostMapping("/google")
    public ResponseEntity<?> verifyGoogleToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        try {
            GoogleIdToken idToken = verifier.verify(token);
            if (idToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Google token");
            }
            var payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");
            String googleId = payload.getSubject();
            String jwt = jwtService.generateTokenWithClaims(email, Map.of(
                    "name", name,
                    "googleId", googleId,
                    "picture", picture
            ));
            return ResponseEntity.ok(Map.of("access_token", jwt));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token verification failed");
        }
    }


    // --------- GITHUB (new) ----------
    // Body: { "token": "<github_access_token>" }
    private final WebClient gh = WebClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeader("User-Agent", "focusforge-auth/1.0") // REQUIRED
            .defaultHeader("Accept", "application/vnd.github+json")
            .build();
    @PostMapping("/github")
    public ResponseEntity<?> verifyGithubToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body("Missing token");
        }

        try {
            // 1) /user
            Map<String, Object> user = gh.get()
                    .uri("/user")
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (user == null || user.get("id") == null) {
                return ResponseEntity.status(401).body("Invalid GitHub token (no user id)");
            }

            // 2) /user/emails (may require user:email)
            List<Map<String, Object>> emails = null;
            try {
                emails = gh.get()
                        .uri("/user/emails")
                        .headers(h -> h.setBearerAuth(token))
                        .retrieve()
                        .bodyToMono(List.class)
                        .block();
            } catch (WebClientResponseException e) {
                // 404 or 403 likely due to missing scope; we’ll fall back to user.email
                System.out.println("GitHub /user/emails error: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            }

            String email = null;
            if (emails != null) {
                email = emails.stream()
                        .filter(e -> Boolean.TRUE.equals(e.get("primary")) && Boolean.TRUE.equals(e.get("verified")))
                        .map(e -> (String) e.get("email"))
                        .findFirst()
                        .orElse(null);
                if (email == null && !emails.isEmpty()) {
                    // pick any verified email if primary+verified not found
                    email = emails.stream()
                            .filter(e -> Boolean.TRUE.equals(e.get("verified")))
                            .map(e -> (String) e.get("email"))
                            .findFirst()
                            .orElse(null);
                }
            }
            if (email == null) {
                Object maybe = user.get("email"); // may be present if public email is set
                if (maybe instanceof String s && !s.isBlank()) email = s;
            }
            if (email == null) {
                return ResponseEntity.status(400).body("GitHub email not available. Ensure the token has scope 'user:email' or user has a public email.");
            }

            String jwt = jwtService.generateToken(email);
            return ResponseEntity.ok(Map.of("access_token", jwt));

        } catch (WebClientResponseException e) {
            // Show the real GitHub API error in logs
            System.err.println("GitHub API error: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode().value())
                    .body("GitHub token verification failed: " + e.getStatusText());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(401).body("GitHub token verification failed");
        }
    }
}