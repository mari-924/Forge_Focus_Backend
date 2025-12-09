package com.focusforge.repositories;

import com.focusforge.models.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void saveAndFindTask() {
        Task saved = taskRepository.save(Task.builder().description("Read").build());

        assertThat(saved.getId()).isNotNull();
        assertThat(taskRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void deleteTask_removesEntity() {
        Task saved = taskRepository.save(Task.builder().description("Remove").build());

        taskRepository.deleteById(saved.getId());

        assertThat(taskRepository.findById(saved.getId())).isEmpty();
    }
}
