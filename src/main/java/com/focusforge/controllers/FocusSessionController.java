package com.focusforge.controllers;

import com.focusforge.models.FocusSession;
import com.focusforge.models.User;
import com.focusforge.repositories.FocusSessionRepository;
import com.focusforge.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/sessions")
public class FocusSessionController {

    private final FocusSessionRepository sessionRepository;
    private final UserRepository userRepository;

    public FocusSessionController(FocusSessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<FocusSession> createSession(
            @RequestParam Long hostId,
            @RequestBody FocusSession session) {

        Optional<User> host = userRepository.findById(hostId);
        if (host.isEmpty()) return ResponseEntity.badRequest().build();

        session.setHost(host.get());
        session.getParticipants().add(host.get()); // host joins automatically

        FocusSession saved = sessionRepository.save(session);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<FocusSession> joinSession(
            @PathVariable Long id,
            @RequestParam Long userId) {

        Optional<FocusSession> sessionOpt = sessionRepository.findById(id);
        Optional<User> userOpt = userRepository.findById(userId);
        if (sessionOpt.isEmpty() || userOpt.isEmpty()) return ResponseEntity.badRequest().build();

        FocusSession session = sessionOpt.get();
        session.getParticipants().add(userOpt.get());
        sessionRepository.save(session);

        return ResponseEntity.ok(session);
    }
    @PatchMapping("/{id}/complete")
    public ResponseEntity<FocusSession> markSessionComplete(@PathVariable Long id) {
        Optional<FocusSession> opt = sessionRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        FocusSession session = opt.get();
        session.setIsPrev(true);
        FocusSession saved = sessionRepository.save(session);

        return ResponseEntity.ok(saved);
    }
    @GetMapping("/user/{email}")
    public ResponseEntity<UserSessionsResponse> getSessionsForUser(@PathVariable String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // previous sessions (already done)
        var previous = sessionRepository.findByHostEmailAndIsPrev(email, true);

        // scheduled/upcoming sessions (not yet completed)
        var scheduled = sessionRepository.findByHostEmailAndIsPrev(email, false);

        return ResponseEntity.ok(new UserSessionsResponse(previous, scheduled));
    }

    // DTO to wrap two lists
    public record UserSessionsResponse(
            java.util.List<FocusSession> previous,
            java.util.List<FocusSession> scheduled
    ) {}
}
