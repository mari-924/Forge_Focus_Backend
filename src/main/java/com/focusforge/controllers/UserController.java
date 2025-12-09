package com.focusforge.controllers;

import com.focusforge.auth.JwtService;
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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@RestController

@RequestMapping("/users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserController {

    private final UserRepository userRepository;
    private final JwtService jwtService;   // <-- ADD THIS

    // Inject the same secret key used in /api/auth/google when signing the JWT
    @Value("${jwt.secret}")
    private String jwtSecret;

    public UserController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
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

            String email = jwtService.extractEmail(token);
            String name = jwtService.extractClaim(token, "name");
            String nickname = jwtService.extractClaim(token, "nickname");
            String googleId = jwtService.extractClaim(token, "googleId");
            String picture = jwtService.extractClaim(token, "picture");

            if (name == null) name = nickname;
            if (name == null) name = email;

            // 🔥 Make effectively-final copies so lambdas can use them
            final String fEmail = email;
            final String fName = name;
            final String fGoogleId = googleId;
            final String fPicture = picture;

            Optional<User> existingUser = userRepository.findByEmail(email);

            User user = existingUser.map(u -> {
                boolean changed = false;

                if (fGoogleId != null && !fGoogleId.equals(u.getGoogleId())) {
                    u.setGoogleId(fGoogleId);
                    changed = true;
                }

                if (fName != null && !fName.equals(u.getName())) {
                    u.setName(fName);
                    changed = true;
                }

                if (fPicture != null && !fPicture.equals(u.getProfileImageUrl())) {
                    u.setProfileImageUrl(fPicture);
                    changed = true;
                }

                return changed ? userRepository.save(u) : u;
            }).orElseGet(() -> {
                User newUser = User.builder()
                        .googleId(fGoogleId)
                        .name(fName)
                        .email(fEmail)
                        .profileImageUrl(fPicture)
                        .createdAt(LocalDateTime.now())
                        .build();
                return userRepository.save(newUser);
            });

            return ResponseEntity.ok(user);

        } catch (Exception e) {
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
