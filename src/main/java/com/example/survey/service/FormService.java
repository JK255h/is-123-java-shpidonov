package com.example.survey.service;

import com.example.survey.model.Form;
import com.example.survey.model.User;
import com.example.survey.repository.FormRepository;
import com.example.survey.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FormService {

    private final FormRepository formRepository;
    private final UserRepository userRepository;

    public FormService(FormRepository formRepository, UserRepository userRepository) {
        this.formRepository = formRepository;
        this.userRepository = userRepository;
    }

    public List<Form> getFormsByUser(Integer userId) {
        return formRepository.findByOwnerIdOrderByFormIdDesc(userId);
    }
    
    public List<Form> getPublishedForms(String query) {
        if (query == null || query.trim().isEmpty()) {
            return formRepository.findByIsPublishedOrderByFormIdDesc((short) 1);
        }
        return formRepository.findByIsPublishedAndTitleContainingIgnoreCaseOrderByFormIdDesc((short) 1, query);
    }

    public Form createForm(Integer ownerId, String title, String description) {
        User user = userRepository.findById(ownerId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Form form = new Form();
        form.setOwnerId(ownerId);
        // form.setOwnerName(user.getUsername());
        form.setTitle(title);
        form.setDescription(description);
        form.setIsPublished((short) 0);
        // form.setCreatedAt(LocalDateTime.now());
        
        return formRepository.save(form);
    }
    
    public Optional<Form> getFormById(Integer formId) {
        return formRepository.findById(formId);
    }
    
    @Transactional
    public void deleteForm(Integer formId) {
        formRepository.deleteById(formId);
    }

    public Form save(Form form) {
        return formRepository.save(form);
    }
}
