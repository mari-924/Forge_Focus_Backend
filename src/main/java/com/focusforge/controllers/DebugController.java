package com.focusforge.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DebugController {

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        return Map.of("principal", auth == null ? null : auth.getPrincipal());
    }
}

