package com.example.survey.service;

import com.example.survey.model.User;
import com.example.survey.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void registerUser_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("hashed_password");

        userService.registerUser("testuser", "test@example.com", "password");

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_DuplicateUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser("testuser", "test@example.com", "password");
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_DuplicateEmail() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser("testuser", "test@example.com", "password");
        });

        verify(userRepository, never()).save(any(User.class));
    }
}
