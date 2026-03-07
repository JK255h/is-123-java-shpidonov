package com.example.survey.repository;

import com.example.survey.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    List<Answer> findByResponseId(Integer responseId);
}
