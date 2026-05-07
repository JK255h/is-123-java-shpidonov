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
public class HtmlReportGenerator implements ReportGenerator {

    private final QuestionService questionService;

    public HtmlReportGenerator(QuestionService questionService) {
        this.questionService = questionService;
    }

    @Override
    public String generateReport(Form form, List<Question> questions, List<SurveyResponse> responses, Map<Integer, List<Answer>> responseAnswers) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Results</title>");
        sb.append("<style>table { border-collapse: collapse; width: 100%; } th, td { border: 1px solid #ddd; padding: 8px; } th { background-color: #f2f2f2; }</style>");
        sb.append("</head><body>");
        sb.append("<h1>").append(escapeHtml(form.getTitle())).append("</h1>");
        sb.append("<p>Всего ответов: ").append(responses.size()).append("</p>");
        
        sb.append("<table><thead><tr><th>№</th>");
        for (Question q : questions) {
            sb.append("<th>").append(escapeHtml(q.getTitle())).append("</th>");
        }
        sb.append("</tr></thead><tbody>");

        for (int i = 0; i < responses.size(); i++) {
            SurveyResponse resp = responses.get(i);
            sb.append("<tr><td>").append(i + 1).append("</td>");
            List<Answer> answers = responseAnswers.get(resp.getResponseId());
            for (Question q : questions) {
                sb.append("<td>");
                if (answers != null) {
                    final Question currentQ = q;
                    String answerText = answers.stream()
                        .filter(a -> a.getQuestionId().equals(currentQ.getQuestionId()))
                        .map(a -> {
                            if (currentQ.getQuestionType().equals("GRID")) {
                                return resolveGridLabel(currentQ, a.getAnswerText());
                            }
                            if (currentQ.getQuestionType().equals("SCALE")) {
                                return a.getAnswerText() != null ? a.getAnswerText() : String.valueOf(a.getOptionId());
                            }
                            if (currentQ.getQuestionType().equals("MULTIPLE_CHOICE") || 
                                currentQ.getQuestionType().equals("CHECKBOX") || 
                                currentQ.getQuestionType().equals("DROPDOWN")) {
                                if (a.getOptionId() != null) {
                                    return questionService.getOptionsByQuestion(currentQ.getQuestionId()).stream()
                                        .filter(o -> o.getOptionId().equals(a.getOptionId()))
                                        .map(Option::getOptionText).findFirst().orElse("ID:" + a.getOptionId());
                                }
                            }
                            return a.getAnswerText();
                        })
                        .filter(s -> s != null && !s.isEmpty() && !s.equals("null"))
                        .collect(Collectors.joining(", "));
                    sb.append(escapeHtml(answerText));
                }
                sb.append("</td>");
            }
            sb.append("</tr>");
        }

        sb.append("</tbody></table></body></html>");
        return sb.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
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

    @Override
    public String getFileType() {
        return "html";
    }
}
