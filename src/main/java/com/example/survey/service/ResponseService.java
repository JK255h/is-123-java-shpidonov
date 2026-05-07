package com.example.survey.service;

import com.example.survey.model.Answer;
import com.example.survey.model.SurveyResponse;
import com.example.survey.repository.AnswerRepository;
import com.example.survey.repository.SurveyResponseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ResponseService {

    private final SurveyResponseRepository responseRepository;
    private final AnswerRepository answerRepository;
    private final com.example.survey.repository.QuestionRepository questionRepository;

    public ResponseService(SurveyResponseRepository responseRepository, AnswerRepository answerRepository, com.example.survey.repository.QuestionRepository questionRepository) {
        this.responseRepository = responseRepository;
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public void saveResponse(Integer formId, Integer userId, Map<String, String[]> params) {
        SurveyResponse response = new SurveyResponse();
        response.setFormId(formId);
        response.setUserId(userId);
        response = responseRepository.save(response);

        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("q_")) {
                Integer questionId;
                Integer rowIndex = null;
                
                String qPart = key.substring(2);
                if (qPart.contains("_r")) {
                    String[] parts = qPart.split("_r");
                    questionId = Integer.parseInt(parts[0]);
                    rowIndex = Integer.parseInt(parts[1]);
                } else {
                    questionId = Integer.parseInt(qPart);
                }
                
                String[] values = entry.getValue();
                final Integer qId = questionId;
                String qType = questionRepository.findById(qId).map(com.example.survey.model.Question::getQuestionType).orElse("");
                
                for (String val : values) {
                    Answer answer = new Answer();
                    answer.setResponseId(response.getResponseId());
                    answer.setQuestionId(questionId);
                    
                    if (rowIndex != null) {
                        answer.setAnswerText("row_" + rowIndex + ":" + val);
                    } else if (qType.equals("MULTIPLE_CHOICE") || qType.equals("CHECKBOX") || qType.equals("DROPDOWN")) {
                        try {
                            answer.setOptionId(Integer.parseInt(val));
                        } catch (NumberFormatException e) {
                            answer.setAnswerText(val);
                        }
                    } else {
                        // For SCALE, DATE, TIME, TEXT, PARAGRAPH - always save as text
                        answer.setAnswerText(val);
                    }
                    answerRepository.save(answer);
                }
            }
        }
    }

    public List<SurveyResponse> getResponsesByForm(Integer formId) {
        return responseRepository.findByFormId(formId);
    }

    public List<Answer> getAnswersByResponse(Integer responseId) {
        return answerRepository.findByResponseId(responseId);
    }
}
