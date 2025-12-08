package com.focusforge.repositories;

import com.focusforge.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Custom finder for user by email
    Optional<User> findByEmail(String email);
}
