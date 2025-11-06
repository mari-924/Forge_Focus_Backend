package com.focusforge.controllers;

import com.focusforge.models.FocusSession;
import com.focusforge.repositories.FocusSessionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sessions")
public class FocusSessionController {

    private final FocusSessionRepository sessionRepository;

    public FocusSessionController(FocusSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @PostMapping
    public FocusSession createSession(@RequestBody FocusSession session) {
        return sessionRepository.save(session);
    }

    @GetMapping
    public List<FocusSession> getAllSessions() {
        return sessionRepository.findAll();
    }

    @GetMapping("/{id}")
    public FocusSession getSession(@PathVariable Long id) {
        return sessionRepository.findById(id).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void deleteSession(@PathVariable Long id) {
        sessionRepository.deleteById(id);
    }
}
