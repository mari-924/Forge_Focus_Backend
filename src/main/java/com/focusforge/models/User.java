package com.focusforge.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String googleId;
    private String name;
    private String email;
    private String profileImageUrl;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
