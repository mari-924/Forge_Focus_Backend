package com.focusforge.repositories;

import com.focusforge.models.FocusSession;
import com.focusforge.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FocusSessionRepositoryTest {

    @Autowired
    private FocusSessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByHostEmailAndIsPrev_filtersByStatus() {
        User host = userRepository.save(User.builder().name("Host").email("host@example.com").build());

        sessionRepository.save(FocusSession.builder()
                .title("Scheduled")
                .host(host)
                .participants(new HashSet<>(List.of(host)))
                .isPrev(false)
                .build());

        sessionRepository.save(FocusSession.builder()
                .title("Completed")
                .host(host)
                .participants(new HashSet<>(List.of(host)))
                .isPrev(true)
                .build());

        assertThat(sessionRepository.findByHostEmailAndIsPrev("host@example.com", false)).hasSize(1);
        assertThat(sessionRepository.findByHostEmailAndIsPrev("host@example.com", true)).hasSize(1);
    }

    @Test
    void findByHostIdAndIsPrev_supportsIdLookups() {
        User host = userRepository.save(User.builder().name("Host2").email("host2@example.com").build());

        sessionRepository.save(FocusSession.builder()
                .title("Prev")
                .host(host)
                .participants(new HashSet<>(List.of(host)))
                .isPrev(true)
                .build());

        List<FocusSession> results = sessionRepository.findByHostIdAndIsPrev(host.getId(), true);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Prev");
    }
}
