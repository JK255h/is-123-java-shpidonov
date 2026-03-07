package com.example.survey.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ANSWERS")
@Data
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ANSWER_ID")
    private Integer answerId;

    @Column(name = "RESPONSE_ID", nullable = false)
    private Integer responseId;

    @Column(name = "QUESTION_ID", nullable = false)
    private Integer questionId;

    @Column(name = "OPTION_ID")
    private Integer optionId;

    @Column(name = "ANSWER_TEXT")
    private String answerText;
}
