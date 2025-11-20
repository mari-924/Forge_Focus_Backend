package com.focusforge.controllers;

import com.focusforge.models.User;
import com.focusforge.repositories.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Automatically inserts or retrieves user on sign-in
    @PostMapping("/signin")
    public ResponseEntity<?> signInOrSignUp(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");

            // Verify Google ID token
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory()
            )
                    .setAudience(Collections.singletonList("YOUR_WEB_CLIENT_ID_HERE")) // from Google Cloud Console
                    .build();

            GoogleIdToken idToken = verifier.verify(token);
            if (idToken == null) {
                return ResponseEntity.status(401).body("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");
            String googleId = payload.getSubject(); // unique Google user ID

            // Lookup or create user
            Optional<User> existingUser = userRepository.findByEmail(email);
            User user = existingUser.orElseGet(() -> {
                User newUser = User.builder()
                        .googleId(googleId)
                        .name(name)
                        .email(email)
                        .profileImageUrl(picture)
                        .createdAt(LocalDateTime.now())
                        .build();
                return userRepository.save(newUser);
            });

            return ResponseEntity.ok(user);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body("Unauthorized: " + e.getMessage());
        }
    }

    // Standard endpoints
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_openid')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_openid')")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Returns info about currently authenticated user (via JWT)
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('SCOPE_openid')")
    public ResponseEntity<?> userInfo(@AuthenticationPrincipal OidcUser user) {
        return ResponseEntity.ok(user.getClaims());
    }
}
