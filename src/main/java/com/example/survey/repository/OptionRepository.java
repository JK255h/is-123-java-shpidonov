package com.example.survey.repository;

import com.example.survey.model.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OptionRepository extends JpaRepository<Option, Integer> {
    List<Option> findByQuestionIdOrderByOptionId(Integer questionId);
    void deleteByQuestionId(Integer questionId);
}
