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
        sb.append("<h1>").append(form.getTitle()).append("</h1>");
        sb.append("<p>Всего ответов: ").append(responses.size()).append("</p>");
        
        sb.append("<table><thead><tr><th>№</th>");
        for (Question q : questions) {
            sb.append("<th>").append(q.getTitle()).append("</th>");
        }
        sb.append("</tr></thead><tbody>");

        for (int i = 0; i < responses.size(); i++) {
            SurveyResponse resp = responses.get(i);
            sb.append("<tr><td>").append(i + 1).append("</td>");
            List<Answer> answers = responseAnswers.get(resp.getResponseId());
            for (Question q : questions) {
                sb.append("<td>");
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
                    sb.append(answerText);
                }
                sb.append("</td>");
            }
            sb.append("</tr>");
        }

        sb.append("</tbody></table></body></html>");
        return sb.toString();
    }

    @Override
    public String getFileType() {
        return "html";
    }
}
