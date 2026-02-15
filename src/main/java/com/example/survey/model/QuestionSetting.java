package com.example.survey.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "QUESTION_SETTINGS")
@Data
public class QuestionSetting {

    @Id
    @Column(name = "QUESTION_ID")
    private Integer questionId;

    @Column(name = "SCALE_MIN")
    private Integer scaleMin;

    @Column(name = "SCALE_MAX")
    private Integer scaleMax;

    @Column(name = "GRID_ROWS")
    private Integer gridRows;

    @Column(name = "GRID_COLUMNS")
    private Integer gridColumns;

    @Column(name = "GRID_ROWS_TEXT")
    private String gridRowsText;

    @Column(name = "GRID_COLUMNS_TEXT")
    private String gridColumnsText;

    @Column(name = "HAS_IMAGE")
    private Short hasImage;

    @Column(name = "IMAGE_PATH")
    private String imagePath;
}
