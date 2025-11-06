
package com.focusforge.controllers;

import com.focusforge.models.FocusSession;
import com.focusforge.repositories.FocusSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FocusSessionController.class)
public class FocusSessionControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private FocusSessionRepository sessionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getAllSessions_returnsList() throws Exception {
        FocusSession s = new FocusSession();
        when(sessionRepository.findAll()).thenReturn(List.of(s));

        mvc.perform(get("/sessions").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(s))));
    }

    @Test
    public void getSession_found() throws Exception {
        FocusSession s = new FocusSession();
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(s));

        mvc.perform(get("/sessions/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(s)));
    }

    @Test
    public void getSession_notFound_returnsEmptyBody() throws Exception {
        when(sessionRepository.findById(2L)).thenReturn(Optional.empty());

        mvc.perform(get("/sessions/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void createSession_savesAndReturns() throws Exception {
        FocusSession input = new FocusSession();
        when(sessionRepository.save(ArgumentMatchers.any(FocusSession.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(input)));
    }

    @Test
    public void deleteSession_invokesRepositoryDelete() throws Exception {
        mvc.perform(delete("/sessions/7"))
                .andExpect(status().isOk());

        verify(sessionRepository, times(1)).deleteById(7L);
    }
}