package com.example.survey.controller;

import com.example.survey.model.*;
import com.example.survey.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/forms/{formId}/results")
public class ResultsController {

    private final FormService formService;
    private final QuestionService questionService;
    private final ResponseService responseService;
    private final UserDetailsServiceImpl userDetailsService;

    public ResultsController(FormService formService, QuestionService questionService, ResponseService responseService, UserDetailsServiceImpl userDetailsService) {
        this.formService = formService;
        this.questionService = questionService;
        this.responseService = responseService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping
    public String viewResults(@PathVariable Integer formId, Model model) {
        Form form = formService.getFormById(formId).orElseThrow(() -> new IllegalArgumentException("Form not found"));
        
        // Ownership check or admin
        User currentUser = userDetailsService.getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.getIsAdmin() != null && currentUser.getIsAdmin() == 1;
        if (currentUser == null || (!form.getOwnerId().equals(currentUser.getUserId()) && !isAdmin)) {
            return "redirect:/login";
        }

        List<SurveyResponse> responses = responseService.getResponsesByForm(formId);
        List<Question> questions = questionService.getQuestionsByForm(formId);
        
        model.addAttribute("form", form);
        model.addAttribute("responsesCount", responses.size());
        model.addAttribute("questions", questions);
        
        // Prepare statistics
        // For each question, we can count answers
        // Map<QuestionId, Map<Value, Count>>
        Map<Integer, Map<String, Long>> stats = new java.util.HashMap<>();
        
        for (Question q : questions) {
            List<Answer> allAnswers = responses.stream()
                .flatMap(r -> responseService.getAnswersByResponse(r.getResponseId()).stream())
                .filter(a -> a.getQuestionId().equals(q.getQuestionId()))
                .collect(Collectors.toList());
            
            if (q.getQuestionType().equals("MULTIPLE_CHOICE") || q.getQuestionType().equals("CHECKBOX")) {
                List<Option> options = questionService.getOptionsByQuestion(q.getQuestionId());
                Map<Integer, String> optionMap = options.stream().collect(Collectors.toMap(Option::getOptionId, Option::getOptionText));
                
                Map<String, Long> optionCounts = allAnswers.stream()
                    .filter(a -> a.getOptionId() != null)
                    .collect(Collectors.groupingBy(a -> optionMap.getOrDefault(a.getOptionId(), "Unknown"), Collectors.counting()));
                
                // Ensure all options are present in stats even if count is 0
                for (Option opt : options) {
                    optionCounts.putIfAbsent(opt.getOptionText(), 0L);
                }
                stats.put(q.getQuestionId(), optionCounts);
            } else if (q.getQuestionType().equals("SCALE")) {
                Map<String, Long> scaleCounts = allAnswers.stream()
                    .filter(a -> a.getAnswerText() != null)
                    .collect(Collectors.groupingBy(Answer::getAnswerText, Collectors.counting()));
                stats.put(q.getQuestionId(), scaleCounts);
            }
        }
        
        model.addAttribute("stats", stats);
        
        return "forms/results";
    }

    @GetMapping("/export/txt")
    @ResponseBody
    public String exportTxt(@PathVariable Integer formId) {
        Form form = formService.getFormById(formId).orElseThrow(() -> new IllegalArgumentException("Form not found"));
        List<SurveyResponse> responses = responseService.getResponsesByForm(formId);
        List<Question> questions = questionService.getQuestionsByForm(formId);

        StringBuilder sb = new StringBuilder();
        sb.append("Результаты опроса: ").append(form.getTitle()).append("\n");
        sb.append("Всего ответов: ").append(responses.size()).append("\n\n");

        for (SurveyResponse resp : responses) {
            sb.append("Ответ №").append(resp.getResponseId()).append(" :\n");
            List<Answer> answers = responseService.getAnswersByResponse(resp.getResponseId());
            for (Question q : questions) {
                sb.append("  - ").append(q.getTitle()).append(": ");
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
                    .collect(Collectors.joining(", "));
                sb.append(answerText).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
