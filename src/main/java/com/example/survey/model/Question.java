package com.example.survey.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

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

    @Column(name = "TITLE")
    private String title;

    @Column(name = "QUESTION_TYPE")
    private String questionType;

    @Column(name = "IS_REQUIRED")
    private Short isRequired;

    @Column(name = "DESCRIPTION", length = 2000)
    private String description;
}
