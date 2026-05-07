package com.example.survey.service;

import com.example.survey.model.Answer;
import com.example.survey.model.Form;
import com.example.survey.model.Question;
import com.example.survey.model.SurveyResponse;
import com.example.survey.model.Option;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TxtReportGenerator implements ReportGenerator {
    
    private final QuestionService questionService;

    public TxtReportGenerator(QuestionService questionService) {
        this.questionService = questionService;
    }

    @Override
    public String generateReport(Form form, List<Question> questions, List<SurveyResponse> responses, Map<Integer, List<Answer>> responseAnswers) {
        StringBuilder sb = new StringBuilder();
        sb.append("Результаты опроса: ").append(form.getTitle()).append("\n");
        sb.append("Описание: ").append(form.getDescription() != null ? form.getDescription() : "Нет описания").append("\n");
        sb.append("Всего ответов: ").append(responses.size()).append("\n\n");

        for (int i = 0; i < responses.size(); i++) {
            SurveyResponse resp = responses.get(i);
            sb.append("Ответ №").append(i + 1).append(" (ID: ").append(resp.getResponseId()).append("):\n");
            List<Answer> answers = responseAnswers.get(resp.getResponseId());
            
            for (Question q : questions) {
                sb.append("  - ").append(q.getTitle()).append(": ");
                String answerText = "нет ответа";
                if (answers != null) {
                    answerText = answers.stream()
                        .filter(a -> a.getQuestionId().equals(q.getQuestionId()))
                        .map(a -> {
                            if (q.getQuestionType().equals("GRID")) {
                                return resolveGridLabel(q, a.getAnswerText());
                            }
                            if (q.getQuestionType().equals("SCALE")) {
                                return a.getAnswerText() != null ? a.getAnswerText() : String.valueOf(a.getOptionId());
                            }
                            if (a.getOptionId() != null) {
                                return questionService.getOptionsByQuestion(q.getQuestionId()).stream()
                                    .filter(o -> o.getOptionId().equals(a.getOptionId()))
                                    .map(Option::getOptionText).findFirst().orElse("ID:" + a.getOptionId());
                            }
                            return a.getAnswerText();
                        })
                        .filter(s -> s != null && !s.isEmpty())
                        .collect(Collectors.joining(", "));
                    if (answerText.isEmpty()) answerText = "нет ответа";
                }
                sb.append(answerText).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String getFileType() {
        return "txt";
    }

    private String resolveGridLabel(Question q, String answerText) {
        if (answerText == null || !answerText.startsWith("row_")) return answerText;
        try {
            String[] parts = answerText.split(":");
            int rIdx = Integer.parseInt(parts[0].substring(4));
            int cIdx = Integer.parseInt(parts[1]);
            
            return questionService.getQuestionSettings(q.getQuestionId()).map(s -> {
                String[] rows = s.getGridRowsText().split("\\|");
                String[] cols = s.getGridColumnsText().split("\\|");
                if (rIdx < rows.length && cIdx < cols.length) {
                    return rows[rIdx].trim() + ": " + cols[cIdx].trim();
                }
                return answerText;
            }).orElse(answerText);
        } catch (Exception e) {
            return answerText;
        }
    }
}
