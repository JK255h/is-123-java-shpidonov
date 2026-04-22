package com.example.survey.service;

import com.example.survey.model.Form;
import com.example.survey.model.User;
import com.example.survey.repository.FormRepository;
import com.example.survey.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FormServiceTest {

    @Mock
    private FormRepository formRepository;

    @Mock
    private UserRepository userRepository;

    private FormService formService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        formService = new FormService(formRepository, userRepository);
    }

    @Test
    void createForm_Success() {
        User user = new User();
        user.setUserId(1);
        user.setUsername("testowner");
        
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(formRepository.save(any(Form.class))).thenAnswer(i -> i.getArguments()[0]);

        Form created = formService.createForm(1, "Test Title", "Test Description");

        assertNotNull(created);
        assertEquals("Test Title", created.getTitle());
        assertEquals(1, created.getOwnerId());
        assertEquals("testowner", created.getOwnerName());
        assertEquals((short) 0, created.getIsPublished());
        verify(formRepository, times(1)).save(any(Form.class));
    }

    @Test
    void createForm_UserNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            formService.createForm(1, "Title", "Desc");
        });

        verify(formRepository, never()).save(any(Form.class));
    }
}
