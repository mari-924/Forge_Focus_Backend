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
        session.setStartTime(LocalDateTime.now());
        if (session.getDurationMinutes() != null) {
            session.setEndTime(session.getStartTime().plusMinutes(session.getDurationMinutes()));
        }

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
}
