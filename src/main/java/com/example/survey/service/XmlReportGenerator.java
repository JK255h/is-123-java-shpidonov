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
                            if (a.getOptionId() != null) {
                                return questionService.getOptionsByQuestion(q.getQuestionId()).stream()
                                    .filter(o -> o.getOptionId().equals(a.getOptionId()))
                                    .map(Option::getOptionText).findFirst().orElse("?");
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

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}
