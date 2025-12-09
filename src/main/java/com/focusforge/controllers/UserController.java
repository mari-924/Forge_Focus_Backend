package com.focusforge.controllers;

import com.focusforge.models.User;
import com.focusforge.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    // Inject the same secret key used in /api/auth/google when signing the JWT
    @Value("${jwt.secret}")
    private String jwtSecret;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** ✅ Handles normal user creation (manual) */
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    /** ✅ Automatically create or retrieve user based on backend-issued JWT */
    @PostMapping("/signin")
    public ResponseEntity<?> signInOrSignUp(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");

            // Parse and validate JWT using your backend secret
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();

            // Extract info from token payload
            String email = claims.getSubject();
            String name = (String) claims.getOrDefault("name", claims.get("nickname"));
            String googleId = (String) claims.get("googleId");  // null for GitHub
            String picture = (String) claims.get("picture");

            // Look up user by email, or create if missing
            Optional<User> existingUser = userRepository.findByEmail(email);
            User user = existingUser.orElseGet(() -> {
                User newUser = User.builder()
                        .googleId(googleId) // null ok
                        .name(name)
                        .email(email)
                        .profileImageUrl(picture)
                        .createdAt(LocalDateTime.now())
                        .build();
                return userRepository.save(newUser);
            });

            return ResponseEntity.ok(user);

        } catch (JwtException e) {
            return ResponseEntity.status(401).body("Invalid or expired JWT: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /** ✅ Get all users */
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /** ✅ Get user by ID */
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /** ✅ Get info about currently authenticated OIDC user */
    @GetMapping("/me")
    public Object userInfo(@AuthenticationPrincipal OidcUser user) {
        return user != null ? user.getClaims() : null;
    }
}
