
package com.focusforge.controllers;

import com.focusforge.models.Stats;
import com.focusforge.repositories.StatsRepository;
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
import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatsController.class)
public class StatsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private StatsRepository statsRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getAllStats_returnsList() throws Exception {
        Stats stats = new Stats();
        when(statsRepository.findAll()).thenReturn(List.of(stats));

        mvc.perform(get("/stats").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(stats))));
    }

    @Test
    public void getStats_found() throws Exception {
        Stats stats = new Stats();
        when(statsRepository.findById(1L)).thenReturn(Optional.of(stats));

        mvc.perform(get("/stats/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(stats)));
    }

    @Test
    public void getStats_notFound_returnsEmptyBody() throws Exception {
        when(statsRepository.findById(2L)).thenReturn(Optional.empty());

        mvc.perform(get("/stats/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void createStats_savesAndReturns() throws Exception {
        Stats input = new Stats();
        input.setTotalFocusTime(120L);
        input.setTotalSessions(3);
        input.setTotalTasks(5);
        input.setStreakDays(2);
        input.setLastSessionDate(LocalDate.parse("2025-01-01"));

        when(statsRepository.save(ArgumentMatchers.any(Stats.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/stats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(input)));
    }

    @Test
    public void updateStats_updatesAndReturns() throws Exception {
        Stats existing = new Stats();
        existing.setTotalFocusTime(50L);
        existing.setTotalSessions(1);
        existing.setTotalTasks(1);
        existing.setStreakDays(0);
        existing.setLastSessionDate(LocalDate.parse("2024-01-01"));

        when(statsRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(statsRepository.save(ArgumentMatchers.any(Stats.class))).thenAnswer(inv -> inv.getArgument(0));

        Stats updated = new Stats();
        updated.setTotalFocusTime(300L);
        updated.setTotalSessions(10);
        updated.setTotalTasks(20);
        updated.setStreakDays(5);
        updated.setLastSessionDate(LocalDate.parse("2025-02-02"));

        mvc.perform(put("/stats/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFocusTime").value(300))
                .andExpect(jsonPath("$.totalSessions").value(10))
                .andExpect(jsonPath("$.totalTasks").value(20))
                .andExpect(jsonPath("$.streakDays").value(5))
                .andExpect(jsonPath("$.lastSessionDate").value("2025-02-02"));
    }
}