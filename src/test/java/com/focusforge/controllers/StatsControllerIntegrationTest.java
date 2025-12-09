package com.focusforge.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.focusforge.models.Stats;
import com.focusforge.models.User;
import com.focusforge.repositories.StatsRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class StatsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StatsRepository statsRepository;

    @Autowired
    private UserRepository userRepository;

    private User makeUser(String email) {
        return userRepository.save(User.builder().name("User").email(email).build());
    }

    @Test
    void createStats_persistsAndReturnsStats() throws Exception {
        User user = makeUser("stats@example.com");
        Stats payload = Stats.builder()
                .user(user)
                .totalFocusTime(10L)
                .totalSessions(1)
                .totalTasks(2)
                .streakDays(3)
                .build();

        mockMvc.perform(post("/stats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFocusTime").value(10));
    }

    @Test
    void getAllStats_returnsList() throws Exception {
        User user = makeUser("list@example.com");
        statsRepository.save(Stats.builder().user(user).totalSessions(2).build());

        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalSessions").value(2));
    }

    @Test
    void updateStats_overwritesFields() throws Exception {
        User user = makeUser("update@example.com");
        Stats saved = statsRepository.save(Stats.builder()
                .user(user)
                .totalFocusTime(5L)
                .totalSessions(1)
                .totalTasks(1)
                .streakDays(1)
                .build());

        Stats update = Stats.builder()
                .totalFocusTime(20L)
                .totalSessions(3)
                .totalTasks(4)
                .streakDays(2)
                .build();

        mockMvc.perform(put("/stats/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFocusTime").value(20))
                .andExpect(jsonPath("$.totalSessions").value(3))
                .andExpect(jsonPath("$.totalTasks").value(4))
                .andExpect(jsonPath("$.streakDays").value(2));

        Stats refreshed = statsRepository.findById(saved.getId()).orElseThrow();
        assertThat(refreshed.getTotalFocusTime()).isEqualTo(20L);
    }
}
