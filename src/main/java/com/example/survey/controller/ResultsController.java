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
    private final List<ReportGenerator> reportGenerators;

    public ResultsController(FormService formService, QuestionService questionService, ResponseService responseService, UserDetailsServiceImpl userDetailsService, List<ReportGenerator> reportGenerators) {
        this.formService = formService;
        this.questionService = questionService;
        this.responseService = responseService;
        this.userDetailsService = userDetailsService;
        this.reportGenerators = reportGenerators;
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
        Map<Integer, Map<String, Long>> stats = new java.util.HashMap<>();
        Map<Integer, List<String>> textAnswers = new java.util.HashMap<>();
        
        for (Question q : questions) {
            List<Answer> allAnswers = responses.stream()
                .flatMap(r -> responseService.getAnswersByResponse(r.getResponseId()).stream())
                .filter(a -> a.getQuestionId().equals(q.getQuestionId()))
                .collect(Collectors.toList());
            
            if (q.getQuestionType().equals("MULTIPLE_CHOICE") || q.getQuestionType().equals("CHECKBOX") || q.getQuestionType().equals("DROPDOWN")) {
                List<Option> options = questionService.getOptionsByQuestion(q.getQuestionId());
                Map<Integer, String> optionMap = options.stream().collect(Collectors.toMap(Option::getOptionId, Option::getOptionText));
                
                Map<String, Long> optionCounts = allAnswers.stream()
                    .filter(a -> a.getOptionId() != null)
                    .collect(Collectors.groupingBy(a -> optionMap.getOrDefault(a.getOptionId(), "Unknown"), Collectors.counting()));
                
                for (Option opt : options) {
                    optionCounts.putIfAbsent(opt.getOptionText(), 0L);
                }
                stats.put(q.getQuestionId(), optionCounts);
            } else if (q.getQuestionType().equals("SCALE")) {
                Map<String, Long> scaleCounts = allAnswers.stream()
                    .filter(a -> a.getAnswerText() != null)
                    .collect(Collectors.groupingBy(Answer::getAnswerText, Collectors.counting()));
                stats.put(q.getQuestionId(), scaleCounts);
            } else if (q.getQuestionType().equals("TEXT") || q.getQuestionType().equals("PARAGRAPH")) {
                List<String> answers = allAnswers.stream()
                    .map(Answer::getAnswerText)
                    .filter(t -> t != null && !t.isEmpty())
                    .collect(Collectors.toList());
                textAnswers.put(q.getQuestionId(), answers);
            }
        }
        
        model.addAttribute("stats", stats);
        model.addAttribute("textAnswers", textAnswers);
        
        return "forms/results";
    }

    @GetMapping("/export/{format}")
    public org.springframework.http.ResponseEntity<byte[]> export(@PathVariable Integer formId, @PathVariable String format) {
        Form form = formService.getFormById(formId).orElseThrow(() -> new IllegalArgumentException("Form not found"));
        
        // Ownership check or admin
        User currentUser = userDetailsService.getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.getIsAdmin() != null && currentUser.getIsAdmin() == 1;
        if (currentUser == null || (!form.getOwnerId().equals(currentUser.getUserId()) && !isAdmin)) {
            return org.springframework.http.ResponseEntity.status(403).build();
        }

        List<SurveyResponse> responses = responseService.getResponsesByForm(formId);
        List<Question> questions = questionService.getQuestionsByForm(formId);
        
        Map<Integer, List<Answer>> responseAnswers = new java.util.HashMap<>();
        for (SurveyResponse r : responses) {
            responseAnswers.put(r.getResponseId(), responseService.getAnswersByResponse(r.getResponseId()));
        }

        ReportGenerator generator = reportGenerators.stream()
                .filter(g -> g.getFileType().equalsIgnoreCase(format))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported format: " + format));

        String content = generator.generateReport(form, questions, responses, responseAnswers);
        byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        String contentType = "text/plain";
        if (format.equalsIgnoreCase("xml")) contentType = "application/xml";
        else if (format.equalsIgnoreCase("html")) contentType = "text/html";

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=results_" + formId + "." + format)
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .body(bytes);
    }
}
