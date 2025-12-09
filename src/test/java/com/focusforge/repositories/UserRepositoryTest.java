package com.focusforge.repositories;

import com.focusforge.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindUser() {
        User saved = userRepository.save(User.builder()
                .name("Tester")
                .email("tester@example.com")
                .googleId("gid-123")
                .build());

        assertThat(saved.getId()).isNotNull();
        assertThat(userRepository.findById(saved.getId())).isPresent();
        assertThat(userRepository.findByEmail("tester@example.com")).isPresent();
    }

    @Test
    void findAll_returnsAllUsers() {
        userRepository.save(User.builder().name("A").email("a@example.com").build());
        userRepository.save(User.builder().name("B").email("b@example.com").build());

        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(2);
    }

    @Test
    void deleteUser_removesEntity() {
        User saved = userRepository.save(User.builder().name("Delete").email("delete@example.com").build());

        userRepository.deleteById(saved.getId());

        assertThat(userRepository.findById(saved.getId())).isNotPresent();
    }
}
