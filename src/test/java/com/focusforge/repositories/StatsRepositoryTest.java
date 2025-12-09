package com.focusforge.repositories;

import com.focusforge.models.Stats;
import com.focusforge.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class StatsRepositoryTest {

    @Autowired
    private StatsRepository statsRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindStats() {
        User user = userRepository.save(User.builder().name("User").email("stat@example.com").build());

        Stats saved = statsRepository.save(Stats.builder()
                .user(user)
                .totalSessions(2)
                .totalTasks(3)
                .streakDays(1)
                .build());

        assertThat(saved.getId()).isNotNull();
        assertThat(statsRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void updateStats_persistsNewValues() {
        User user = userRepository.save(User.builder().name("User2").email("stat2@example.com").build());
        Stats saved = statsRepository.save(Stats.builder()
                .user(user)
                .totalFocusTime(5L)
                .totalSessions(1)
                .totalTasks(1)
                .streakDays(1)
                .build());

        saved.setTotalFocusTime(15L);
        saved.setTotalSessions(4);
        saved.setTotalTasks(6);
        saved.setStreakDays(3);

        Stats updated = statsRepository.save(saved);

        assertThat(updated.getTotalFocusTime()).isEqualTo(15L);
        assertThat(updated.getTotalSessions()).isEqualTo(4);
        assertThat(updated.getTotalTasks()).isEqualTo(6);
        assertThat(updated.getStreakDays()).isEqualTo(3);
    }
}
