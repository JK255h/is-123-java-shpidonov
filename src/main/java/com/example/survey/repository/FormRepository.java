package com.example.survey.repository;

import com.example.survey.model.Form;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FormRepository extends JpaRepository<Form, Integer> {
    List<Form> findByOwnerIdOrderByFormIdDesc(Integer ownerId);

    List<Form> findByIsPublishedOrderByFormIdDesc(Short isPublished);

    List<Form> findByIsPublishedAndTitleContainingIgnoreCaseOrderByFormIdDesc(Short isPublished, String title);

    List<Form> findAllByOrderByFormIdDesc();
}
