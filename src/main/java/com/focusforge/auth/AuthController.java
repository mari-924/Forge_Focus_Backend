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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@SuppressWarnings("unchecked")
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
    // GITHUB STEP 2: VERIFY ACCESS TOKEN (Mobile -> Backend)
    // Body: { "token": "<github_access_token>" }
    // Returns JWT for your app.
    // ------------------------------------------------------------------------
    @PostMapping("/github")
    public ResponseEntity<?> verifyGithubAuth0Token(@RequestBody Map<String, String> body) {

        String idToken = body.get("token"); // Auth0 ID token

        try {
            Auth0TokenVerifier verifier = new Auth0TokenVerifier("dev-fjosqdjeu3tei3ei.us.auth0.com");
            Map<String, Object> claims = verifier.verify(idToken);

            String email = (String) claims.get("email");
            if (email == null || email.isBlank()) {
                return ResponseEntity.status(400).body("Auth0 token missing email");
            }

            String name = (String) claims.getOrDefault("nickname", claims.get("name"));
            String picture = (String) claims.get("picture");

            Map<String, Object> extra = new HashMap<>();
            if (name != null) extra.put("name", name);
            if (picture != null) extra.put("picture", picture);
            // googleId intentionally not included for GitHub users

            String jwt = jwtService.generateTokenWithClaims(email, extra);

            return ResponseEntity.ok(Map.of("access_token", jwt));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body("Invalid Auth0 GitHub token: " + e.getMessage());
        }
    }

    }

