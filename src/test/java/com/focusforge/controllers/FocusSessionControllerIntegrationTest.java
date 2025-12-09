package com.focusforge.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.focusforge.models.FocusSession;
import com.focusforge.models.User;
import com.focusforge.repositories.FocusSessionRepository;
import com.focusforge.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class FocusSessionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FocusSessionRepository sessionRepository;

    private User makeUser(String email) {
        return userRepository.save(User.builder().name(email).email(email).build());
    }

    private FocusSession buildSessionPayload() {
        return FocusSession.builder()
                .title("Morning Sprint")
                .durationMinutes(25)
                .participants(new HashSet<>())
                .build();
    }

    @Test
    void createSession_withHostEmail_setsHostAndParticipants() throws Exception {
        User host = makeUser("host@example.com");

        mockMvc.perform(post("/sessions")
                        .param("hostEmail", host.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildSessionPayload())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.host.email").value("host@example.com"))
                .andExpect(jsonPath("$.participants.length()").value(1));
    }

    @Test
    void joinSession_addsParticipant() throws Exception {
        User host = makeUser("host2@example.com");
        User user = makeUser("joiner@example.com");

        FocusSession created = objectMapper.readValue(
                mockMvc.perform(post("/sessions")
                                .param("hostEmail", host.getEmail())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(buildSessionPayload())))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(),
                FocusSession.class);

        mockMvc.perform(post("/sessions/" + created.getId() + "/join")
                        .param("userId", user.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participants.length()").value(2));
    }

    @Test
    void markSessionComplete_setsIsPrevTrue() throws Exception {
        User host = makeUser("host3@example.com");

        FocusSession created = objectMapper.readValue(
                mockMvc.perform(post("/sessions")
                                .param("hostEmail", host.getEmail())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(buildSessionPayload())))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(),
                FocusSession.class);

        mockMvc.perform(patch("/sessions/" + created.getId() + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isPrev").value(true));
    }

    @Test
    void getSessionsForUser_groupsByStatus() throws Exception {
        User host = makeUser("host4@example.com");

        FocusSession scheduled = sessionRepository.save(FocusSession.builder()
                .title("Scheduled")
                .durationMinutes(25)
                .host(host)
                .participants(new java.util.HashSet<>(java.util.List.of(host)))
                .isPrev(false)
                .build());

        FocusSession previous = sessionRepository.save(FocusSession.builder()
                .title("Done")
                .durationMinutes(25)
                .host(host)
                .participants(new java.util.HashSet<>(java.util.List.of(host)))
                .isPrev(true)
                .build());

        mockMvc.perform(get("/sessions/user/" + host.getEmail()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduled.length()").value(1))
                .andExpect(jsonPath("$.previous.length()").value(1));

        assertThat(sessionRepository.findByHostEmailAndIsPrev(host.getEmail(), true))
                .extracting(FocusSession::getId)
                .contains(previous.getId());
        assertThat(sessionRepository.findByHostEmailAndIsPrev(host.getEmail(), false))
                .extracting(FocusSession::getId)
                .contains(scheduled.getId());
    }
}
