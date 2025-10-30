package com.focusforge.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "stats")
public class Stats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "total_focus_time")
    private Long totalFocusTime = 0L;

    @Column(name = "total_sessions")
    private Integer totalSessions = 0;

    @Column(name = "total_tasks")
    private Integer totalTasks = 0;

    @Column(name = "streak_days")
    private Integer streakDays = 0;

    @Column(name = "last_session_date")
    private LocalDate lastSessionDate;
}
