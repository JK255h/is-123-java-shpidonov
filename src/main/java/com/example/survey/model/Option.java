package com.example.survey.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "OPTIONS")
@Data
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OPTION_ID")
    private Integer optionId;

    @Column(name = "QUESTION_ID", nullable = false)
    private Integer questionId;

    @Column(name = "OPTION_TEXT", nullable = false)
    private String optionText;

    @Column(name = "IMAGE_PATH")
    private String imagePath;
}
