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

    public ResponseService(SurveyResponseRepository responseRepository, AnswerRepository answerRepository) {
        this.responseRepository = responseRepository;
        this.answerRepository = answerRepository;
    }

    @Transactional
    public void saveResponse(Integer formId, Integer userId, Map<String, String[]> params) {
        SurveyResponse response = new SurveyResponse();
        response.setFormId(formId);
        response.setUserId(userId);
        response.setCompletedAt(LocalDateTime.now());
        response = responseRepository.save(response);

        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("q_")) {
                Integer questionId = Integer.parseInt(key.substring(2));
                String[] values = entry.getValue();
                
                for (String val : values) {
                    Answer answer = new Answer();
                    answer.setResponseId(response.getResponseId());
                    answer.setQuestionId(questionId);
                    
                    try {
                        // Try to parse as optionId
                        Integer optionId = Integer.parseInt(val);
                        answer.setOptionId(optionId);
                    } catch (NumberFormatException e) {
                        // Otherwise it's text answer
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
