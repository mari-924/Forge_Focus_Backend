package com.focusforge.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.focusforge.models.FocusSession;
import com.focusforge.models.User;
import com.focusforge.repositories.FocusSessionRepository;
import com.focusforge.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/sessions")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class FocusSessionController {

    private final FocusSessionRepository sessionRepository;
    private final UserRepository userRepository;

    public FocusSessionController(FocusSessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<FocusSession> createSession(
            @RequestParam(required = false) Long hostId,
            @RequestParam(required = false) String hostEmail,
            @RequestBody FocusSession session) {

        if (hostId == null && (hostEmail == null || hostEmail.isBlank())) {
            // You can customize this error body if you want
            return ResponseEntity.badRequest().build();
        }

        Optional<User> hostOpt;

        if (hostEmail != null && !hostEmail.isBlank()) {
            hostOpt = userRepository.findByEmail(hostEmail);
        } else {
            hostOpt = userRepository.findById(hostId);
        }

        if (hostOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User host = hostOpt.get();

        session.setHost(host);
        session.getParticipants().add(host);

        if (session.getIsPrev() == null) {
            session.setIsPrev(false); // New sessions are not previous yet
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

        var previous = sessionRepository.findByHostEmailAndIsPrev(email, true);
        var scheduled = sessionRepository.findByHostEmailAndIsPrev(email, false);

        return ResponseEntity.ok(new UserSessionsResponse(previous, scheduled));
    }
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateSession(
            @PathVariable Long id,
            @RequestBody FocusSession updates
    ) {
        Optional<FocusSession> opt = sessionRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        FocusSession session = opt.get();

        if (updates.getTitle() != null) session.setTitle(updates.getTitle());
        if (updates.getDurationMinutes() != null) session.setDurationMinutes(updates.getDurationMinutes());
        if (updates.getNotes() != null) session.setNotes(updates.getNotes());
        if (updates.getAudioFile() != null) session.setAudioFile(updates.getAudioFile());
        if (updates.getIsPrev() != null) session.setIsPrev(updates.getIsPrev());

        FocusSession saved = sessionRepository.save(session);

        // ⭐ Return a DTO instead of the entity
        return ResponseEntity.ok(Map.of(
                "id", saved.getId(),
                "title", saved.getTitle(),
                "duration", saved.getDurationMinutes(),
                "audioFile", saved.getAudioFile(),
                "notes", saved.getNotes(),
                "isPrev", saved.getIsPrev()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSession(@PathVariable Long id) {
        if (!sessionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        sessionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    public record UserSessionsResponse(
            java.util.List<FocusSession> previous,
            java.util.List<FocusSession> scheduled
    ) {}
}
