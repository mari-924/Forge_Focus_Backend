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
            System.out.println("🔥 /users/signin called");
            System.out.println("🔥 Authorization header: " + authHeader);

            String token = authHeader.replace("Bearer ", "");
            System.out.println("🔥 Extracted JWT: " + token);

            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();

            System.out.println("🔥 JWT Claims: " + claims);

            String email = claims.getSubject();
            System.out.println("🔥 Email (subject): " + email);

            String name = claims.get("name") != null
                    ? (String) claims.get("name")
                    : (String) claims.get("nickname");
            System.out.println("🔥 Name extracted: " + name);

            String googleId = (String) claims.getOrDefault("googleId", null);
            System.out.println("🔥 Google ID: " + googleId);

            String picture = (String) claims.getOrDefault("picture", null);
            System.out.println("🔥 Picture URL: " + picture);

            // Lookup
            Optional<User> existingUser = userRepository.findByEmail(email);
            System.out.println("🔥 existingUser present? " + existingUser.isPresent());

            User user = existingUser.orElseGet(() -> {
                System.out.println("🔥 Creating NEW user in DB...");

                User newUser = User.builder()
                        .googleId(googleId)
                        .name(name != null ? name : email)
                        .email(email)
                        .profileImageUrl(picture)
                        .createdAt(LocalDateTime.now())
                        .build();

                System.out.println("🔥 NEW USER BEFORE SAVE: " + newUser);

                return userRepository.save(newUser);
            });

            System.out.println("🔥 FINAL USER RETURNED: " + user);

            return ResponseEntity.ok(user);

        } catch (JwtException e) {
            System.out.println("❌ JWT EXCEPTION: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid or expired JWT: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ GENERAL EXCEPTION in /users/signin: " + e.getMessage());
            e.printStackTrace();
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
