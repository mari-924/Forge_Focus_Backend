
package com.focusforge.controllers;

import com.focusforge.models.FocusSession;
import com.focusforge.models.User;
import com.focusforge.repositories.FocusSessionRepository;
import com.focusforge.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FocusSessionControllerTest1 {

    @Mock
    private FocusSessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FocusSessionController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void createSession_withValidHost_savesAndReturnsSession() {
        User host = User.builder().id(1L).name("Host").build();
        FocusSession session = FocusSession.builder()
                .durationMinutes(25)
                .participants(new HashSet<>())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(host));
        when(sessionRepository.save(any(FocusSession.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<FocusSession> response = controller.createSession(1L, null, session);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(host, response.getBody().getHost());
        verify(sessionRepository, times(1)).save(any(FocusSession.class));
    }

    @Test
    public void createSession_withInvalidHost_returnsBadRequest() {
        FocusSession session = new FocusSession();
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<FocusSession> response = controller.createSession(999L, null, session);

        assertEquals(400, response.getStatusCode().value());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    public void joinSession_withValidData_addsUserToSession() {
        User user = User.builder().id(2L).name("User").build();
        FocusSession session = FocusSession.builder()
                .id(1L)
                .participants(new HashSet<>())
                .build();

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(sessionRepository.save(any(FocusSession.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<FocusSession> response = controller.joinSession(1L, 2L);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(response.getBody().getParticipants().contains(user));
        verify(sessionRepository, times(1)).save(session);
    }

    @Test
    public void joinSession_withInvalidSession_returnsBadRequest() {
        when(sessionRepository.findById(999L)).thenReturn(Optional.empty());
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));

        ResponseEntity<FocusSession> response = controller.joinSession(999L, 2L);

        assertEquals(400, response.getStatusCode().value());
    }
}