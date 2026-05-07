package com.example.survey.service;

import com.example.survey.model.Answer;
import com.example.survey.model.Form;
import com.example.survey.model.Question;
import com.example.survey.model.SurveyResponse;

import java.util.List;
import java.util.Map;

public interface ReportGenerator {
    String generateReport(Form form, List<Question> questions, List<SurveyResponse> responses, Map<Integer, List<Answer>> responseAnswers);
    String getFileType();
}
