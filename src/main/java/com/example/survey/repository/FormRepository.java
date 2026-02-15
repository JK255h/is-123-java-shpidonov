package com.example.survey.repository;

import com.example.survey.model.Form;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FormRepository extends JpaRepository<Form, Integer> {
    List<Form> findByOwnerIdOrderByCreatedAtDesc(Integer ownerId);
    List<Form> findByIsPublishedOrderByCreatedAtDesc(Short isPublished);
}
