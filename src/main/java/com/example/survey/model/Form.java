package com.example.survey.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "FORMS")
@Data
public class Form {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FORM_ID")
    private Integer formId;

    @Column(name = "OWNER_ID", nullable = false)
    private Integer ownerId;

    /*
    @Column(name = "OWNER_NAME")
    private String ownerName;
    */

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "IS_PUBLISHED", nullable = false)
    private Short isPublished;

    /*
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
    */
}
