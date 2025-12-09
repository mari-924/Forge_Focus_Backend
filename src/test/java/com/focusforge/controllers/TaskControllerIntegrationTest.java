package com.focusforge.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.focusforge.models.Task;
import com.focusforge.repositories.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void createTask_persistsAndReturnsTask() throws Exception {
        Task payload = Task.builder()
                .description("Write tests")
                .build();

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Write tests"))
                .andExpect(jsonPath("$.isCompleted").value(false));
    }

    @Test
    void getAllTasks_returnsTasks() throws Exception {
        taskRepository.save(Task.builder().description("A").build());
        taskRepository.save(Task.builder().description("B").build());

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()" ).value(2));
    }

    @Test
    void getTaskById_returnsTask() throws Exception {
        Task saved = taskRepository.save(Task.builder().description("Focus").build());

        mockMvc.perform(get("/tasks/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Focus"));
    }

    @Test
    void markTaskComplete_setsFlags() throws Exception {
        Task saved = taskRepository.save(Task.builder().description("Complete me").build());

        mockMvc.perform(put("/tasks/" + saved.getId() + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCompleted").value(true));

        Task refreshed = taskRepository.findById(saved.getId()).orElseThrow();
        assertThat(refreshed.getCompletedAt()).isNotNull();
    }

    @Test
    void deleteTask_removesTask() throws Exception {
        Task saved = taskRepository.save(Task.builder().description("Remove").build());

        mockMvc.perform(delete("/tasks/" + saved.getId()))
                .andExpect(status().isOk());

        assertThat(taskRepository.findById(saved.getId())).isEmpty();
    }
}
