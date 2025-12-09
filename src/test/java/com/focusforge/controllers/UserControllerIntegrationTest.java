package com.focusforge.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.focusforge.models.User;
import com.focusforge.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createUser_returnsSavedUser() throws Exception {
        User payload = User.builder()
                .name("Test User")
                .email("test@example.com")
                .googleId("gid-1")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        assertThat(userRepository.findByEmail("test@example.com")).isPresent();
    }

    @Test
    void getAllUsers_returnsList() throws Exception {
        userRepository.save(User.builder().name("A").email("a@example.com").build());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].email").value("a@example.com"));
    }

    @Test
    void getUserById_returnsUser() throws Exception {
        User saved = userRepository.save(User.builder()
                .name("Jane")
                .email("jane@example.com")
                .build());

        mockMvc.perform(get("/users/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane"))
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }
}
