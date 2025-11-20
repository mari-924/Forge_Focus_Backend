package com.focusforge.controllers;

import com.focusforge.models.Stats;
import com.focusforge.repositories.StatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatsControllerTest {
    @Mock
    private StatsRepository statsRepository;
    @InjectMocks
    private StatsController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllStats_returnsList() {
        Stats s1 = new Stats();
        Stats s2 = new Stats();
        when(statsRepository.findAll()).thenReturn(Arrays.asList(s1, s2));
        List<Stats> result = controller.getAllStats();
        assertEquals(2, result.size());
    }

    @Test
    void getStats_returnsStats_whenExists() {
        Stats stats = new Stats();
        when(statsRepository.findById(1L)).thenReturn(Optional.of(stats));
        assertEquals(stats, controller.getStats(1L));
    }

    @Test
    void getStats_returnsNull_whenNotExists() {
        when(statsRepository.findById(2L)).thenReturn(Optional.empty());
        assertNull(controller.getStats(2L));
    }

    @Test
    void createStats_savesAndReturnsStats() {
        Stats stats = new Stats();
        when(statsRepository.save(stats)).thenReturn(stats);
        assertEquals(stats, controller.createStats(stats));
    }

    @Test
    void updateStats_updatesAndReturnsStats_whenExists() {
        Stats existingStats = new Stats();
        existingStats.setTotalFocusTime(100L);
        
        Stats updatedStats = new Stats();
        updatedStats.setTotalFocusTime(200L);
        updatedStats.setTotalSessions(5);
        updatedStats.setTotalTasks(10);
        updatedStats.setStreakDays(3);

        when(statsRepository.findById(1L)).thenReturn(Optional.of(existingStats));
        when(statsRepository.save(existingStats)).thenReturn(existingStats);

        Stats result = controller.updateStats(1L, updatedStats);

        assertEquals(200L, result.getTotalFocusTime());
        assertEquals(5, result.getTotalSessions());
        assertEquals(10, result.getTotalTasks());
        assertEquals(3, result.getStreakDays());
        verify(statsRepository, times(1)).save(existingStats);
    }

    @Test
    void updateStats_returnsNull_whenNotExists() {
        Stats newStats = new Stats();
        when(statsRepository.findById(2L)).thenReturn(Optional.empty());
        assertNull(controller.updateStats(2L, newStats));
    }
}