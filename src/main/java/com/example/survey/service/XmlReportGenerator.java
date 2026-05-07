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
public class XmlReportGenerator implements ReportGenerator {

    private final QuestionService questionService;

    public XmlReportGenerator(QuestionService questionService) {
        this.questionService = questionService;
    }

    @Override
    public String generateReport(Form form, List<Question> questions, List<SurveyResponse> responses, Map<Integer, List<Answer>> responseAnswers) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<survey_results>\n");
        sb.append("  <form_info>\n");
        sb.append("    <title>").append(escapeXml(form.getTitle())).append("</title>\n");
        sb.append("    <responses_count>").append(responses.size()).append("</responses_count>\n");
        sb.append("  </form_info>\n");
        sb.append("  <responses>\n");

        for (SurveyResponse resp : responses) {
            sb.append("    <response id=\"").append(resp.getResponseId()).append("\">\n");
            List<Answer> answers = responseAnswers.get(resp.getResponseId());
            for (Question q : questions) {
                sb.append("      <answer question=\"").append(escapeXml(q.getTitle())).append("\">");
                if (answers != null) {
                    String answerText = answers.stream()
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
                    sb.append(escapeXml(answerText));
                }
                sb.append("</answer>\n");
            }
            sb.append("    </response>\n");
        }

        sb.append("  </responses>\n");
        sb.append("</survey_results>");
        return sb.toString();
    }

    @Override
    public String getFileType() {
        return "xml";
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

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}
