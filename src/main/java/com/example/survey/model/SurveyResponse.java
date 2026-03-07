package com.example.survey.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "SURVEY_RESPONSES")
@Data
public class SurveyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESPONSE_ID")
    private Integer responseId;

    @Column(name = "FORM_ID", nullable = false)
    private Integer formId;

    @Column(name = "USER_ID")
    private Integer userId;

    @Column(name = "COMPLETED_AT")
    private LocalDateTime completedAt;
}
