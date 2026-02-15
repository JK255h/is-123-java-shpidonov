package com.example.survey.repository;

import com.example.survey.model.QuestionSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionSettingRepository extends JpaRepository<QuestionSetting, Integer> {
}
