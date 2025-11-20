package com.focusforge.controllers;

import com.focusforge.models.User;
import com.focusforge.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserController controller;


    @Test
    void createUser_savesAndReturnsUser() {
        User user = new User();
        when(userRepository.save(user)).thenReturn(user);
        assertEquals(user, controller.createUser(user));
    }

    @Test
    void getAllUsers_returnsList() {
        User u1 = new User();
        User u2 = new User();
        when(userRepository.findAll()).thenReturn(Arrays.asList(u1, u2));
        List<User> result = controller.getAllUsers();
        assertEquals(2, result.size());
    }

    @Test
    void getUser_returnsUser_whenExists() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertEquals(user, controller.getUser(1L));
    }

    @Test
    void getUser_returnsNull_whenNotExists() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertNull(controller.getUser(2L));
    }
}