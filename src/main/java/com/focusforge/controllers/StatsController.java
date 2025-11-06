package com.focusforge.controllers;

import com.focusforge.models.Stats;
import com.focusforge.repositories.StatsRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final StatsRepository statsRepository;

    public StatsController(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    @GetMapping
    public List<Stats> getAllStats() {
        return statsRepository.findAll();
    }

    @GetMapping("/{id}")
    public Stats getStats(@PathVariable Long id) {
        return statsRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Stats createStats(@RequestBody Stats stats) {
        return statsRepository.save(stats);
    }

    @PutMapping("/{id}")
    public Stats updateStats(@PathVariable Long id, @RequestBody Stats updatedStats) {
        return statsRepository.findById(id).map(stats -> {
            stats.setTotalFocusTime(updatedStats.getTotalFocusTime());
            stats.setTotalSessions(updatedStats.getTotalSessions());
            stats.setTotalTasks(updatedStats.getTotalTasks());
            stats.setStreakDays(updatedStats.getStreakDays());
            stats.setLastSessionDate(updatedStats.getLastSessionDate());
            return statsRepository.save(stats);
        }).orElse(null);
    }
}
