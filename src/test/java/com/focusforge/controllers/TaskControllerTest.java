package com.focusforge.controllers;

import com.focusforge.models.Task;
import com.focusforge.repositories.TaskRepository;
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

class TaskControllerTest {
    @Mock
    private TaskRepository taskRepository;
    @InjectMocks
    private TaskController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTask_savesAndReturnsTask() {
        Task task = new Task();
        when(taskRepository.save(task)).thenReturn(task);
        assertEquals(task, controller.createTask(task));
    }

    @Test
    void getAllTasks_returnsList() {
        Task t1 = new Task();
        Task t2 = new Task();
        when(taskRepository.findAll()).thenReturn(Arrays.asList(t1, t2));
        List<Task> result = controller.getAllTasks();
        assertEquals(2, result.size());
    }

    @Test
    void getTask_returnsTask_whenExists() {
        Task task = new Task();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        assertEquals(task, controller.getTask(1L));
    }

    @Test
    void getTask_returnsNull_whenNotExists() {
        when(taskRepository.findById(2L)).thenReturn(Optional.empty());
        assertNull(controller.getTask(2L));
    }

    @Test
    void markTaskComplete_updatesAndReturnsTask() {
        Task task = new Task();
        task.setIsCompleted(false);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = controller.markTaskComplete(1L);

        assertTrue(result.getIsCompleted());
        assertNotNull(result.getCompletedAt());
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void markTaskComplete_returnsNull_whenNotExists() {
        when(taskRepository.findById(2L)).thenReturn(Optional.empty());
        assertNull(controller.markTaskComplete(2L));
    }

    @Test
    void deleteTask_deletesById() {
        controller.deleteTask(1L);
        verify(taskRepository, times(1)).deleteById(1L);
    }
}