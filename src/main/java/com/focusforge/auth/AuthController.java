package com.focusforge.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;
    private final GoogleIdTokenVerifier verifier;

    private final WebClient github = WebClient.builder()
            .baseUrl("https://github.com")
            .defaultHeader("Accept", "application/json")
            .defaultHeader("User-Agent", "focusforge-auth")
            .build();

    private final WebClient ghApi = WebClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeader("Accept", "application/vnd.github+json")
            .defaultHeader("User-Agent", "focusforge-auth")
            .build();

    public AuthController(JwtService jwtService, GoogleIdTokenVerifier verifier) {
        this.jwtService = jwtService;
        this.verifier = verifier;
    }

    // ------------------------------------------------------------------------
    // GOOGLE LOGIN
    // ------------------------------------------------------------------------
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

            String jwt = jwtService.generateTokenWithClaims(
                    email,
                    Map.of("name", name, "googleId", googleId, "picture", picture)
            );

            return ResponseEntity.ok(Map.of("access_token", jwt));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token verification failed");
        }
    }

    // ------------------------------------------------------------------------
    // GITHUB STEP 1: CALLBACK ENDPOINT (GitHub -> Backend)
    // GitHub OAuth Apps REQUIRE an HTTPS redirect. This endpoint receives:
    //   GET /api/auth/github/callback?code=XXXX
    // Then exchanges code -> GitHub access token.
    // Then redirects mobile back into the app via deep link.
    // ------------------------------------------------------------------------
    @GetMapping("/github/callback")
    public ResponseEntity<?> githubCallback(@RequestParam String code) {

        String clientId = System.getenv("GITHUB_CLIENT_ID");
        String clientSecret = System.getenv("GITHUB_CLIENT_SECRET");

        try {
            // Exchange code -> access_token
            Map<String, Object> res = github.post()
                    .uri("/login/oauth/access_token")
                    .bodyValue(Map.of(
                            "client_id", clientId,
                            "client_secret", clientSecret,
                            "code", code
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (res == null || !res.containsKey("access_token")) {
                System.err.println("GitHub exchange returned null or no access_token");
                return ResponseEntity.status(400).body("Failed to exchange code");
            }

            String accessToken = (String) res.get("access_token");

            // Redirect mobile app via deep link
            URI deepLink = URI.create("forgefocus://redirect?token=" + accessToken);

            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, deepLink.toString())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("GitHub callback processing error");
        }
    }

    // ------------------------------------------------------------------------
    // GITHUB STEP 2: VERIFY ACCESS TOKEN (Mobile -> Backend)
    // Body: { "token": "<github_access_token>" }
    // Returns JWT for your app.
    // ------------------------------------------------------------------------
    @PostMapping("/github")
    public ResponseEntity<?> verifyGithubAccessToken(@RequestBody Map<String, String> body) {

        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body("Missing GitHub access token");
        }

        try {
            // 1) Fetch GitHub user profile
            Map<String, Object> user = ghApi.get()
                    .uri("/user")
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (user == null || user.get("id") == null) {
                return ResponseEntity.status(401).body("Invalid GitHub access token");
            }

            // 2) Retrieve user email(s)
            String email = null;
            try {
                List<Map<String, Object>> emails = ghApi.get()
                        .uri("/user/emails")
                        .headers(h -> h.setBearerAuth(token))
                        .retrieve()
                        .bodyToMono(List.class)
                        .block();

                if (emails != null) {
                    email = emails.stream()
                            .filter(e -> Boolean.TRUE.equals(e.get("primary")) && Boolean.TRUE.equals(e.get("verified")))
                            .map(e -> (String) e.get("email"))
                            .findFirst()
                            .orElse(null);
                }

            } catch (WebClientResponseException ignored) {
                // Fallback to public email
            }

            if (email == null && user.get("email") instanceof String s && !s.isBlank()) {
                email = s;
            }

            if (email == null) {
                return ResponseEntity.status(400)
                        .body("GitHub email not available. Enable 'user:email' scope or make email public.");
            }

            // 3) Generate JWT for your app
            String jwt = jwtService.generateToken(email);

            return ResponseEntity.ok(Map.of("access_token", jwt));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("GitHub token verification failed");
        }
    }
}
