package com.example.survey.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "QUESTIONS")
@Data
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QUESTION_ID")
    private Integer questionId;

    @Column(name = "FORM_ID")
    private Integer formId;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "QUESTION_TYPE", nullable = false)
    private String questionType;

    @Column(name = "IS_REQUIRED", nullable = false)
    private Short isRequired;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
}
