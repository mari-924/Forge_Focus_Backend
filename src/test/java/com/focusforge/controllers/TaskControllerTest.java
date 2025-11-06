
package com.focusforge.controllers;

import com.focusforge.models.Task;
import com.focusforge.repositories.TaskRepository;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getAllTasks_returnsList() throws Exception {
        Task task = new Task();
        when(taskRepository.findAll()).thenReturn(List.of(task));

        mvc.perform(get("/tasks").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(task))));
    }

    @Test
    public void getTask_found() throws Exception {
        Task task = new Task();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        mvc.perform(get("/tasks/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(task)));
    }

    @Test
    public void getTask_notFound_returnsEmptyBody() throws Exception {
        when(taskRepository.findById(2L)).thenReturn(Optional.empty());

        mvc.perform(get("/tasks/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void markTaskComplete_updatesAndReturns() throws Exception {
        Task task = new Task();
        when(taskRepository.findById(3L)).thenReturn(Optional.of(task));
        when(taskRepository.save(ArgumentMatchers.any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(put("/tasks/3/complete").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCompleted").value(true))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());
    }

    @Test
    public void deleteTask_invokesRepositoryDelete() throws Exception {
        mvc.perform(delete("/tasks/4"))
                .andExpect(status().isOk());

        verify(taskRepository, times(1)).deleteById(4L);
    }
}