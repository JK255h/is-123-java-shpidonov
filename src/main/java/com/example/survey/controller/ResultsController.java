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
        Map<Integer, Map<String, Map<String, Long>>> gridStats = new java.util.HashMap<>();
        Map<Integer, QuestionSetting> settingsMap = new java.util.HashMap<>();
        
        for (Question q : questions) {
            List<Answer> allAnswers = responses.stream()
                .flatMap(r -> responseService.getAnswersByResponse(r.getResponseId()).stream())
                .filter(a -> a.getQuestionId().equals(q.getQuestionId()))
                .collect(Collectors.toList());
            
            questionService.getQuestionSettings(q.getQuestionId()).ifPresent(s -> settingsMap.put(q.getQuestionId(), s));

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
                QuestionSetting s = settingsMap.get(q.getQuestionId());
                int min = (s != null && s.getScaleMin() != null) ? s.getScaleMin() : 1;
                int max = (s != null && s.getScaleMax() != null) ? s.getScaleMax() : 5;
                
                Map<String, Long> scaleCounts = new java.util.TreeMap<>((a, b) -> Integer.compare(Integer.parseInt(a), Integer.parseInt(b)));
                for (int i = min; i <= max; i++) {
                    scaleCounts.put(String.valueOf(i), 0L);
                }

                for (Answer a : allAnswers) {
                    String val = null;
                    if (a.getAnswerText() != null) val = a.getAnswerText();
                    else if (a.getOptionId() != null) val = String.valueOf(a.getOptionId());
                    
                    if (val != null && scaleCounts.containsKey(val)) {
                        scaleCounts.put(val, scaleCounts.get(val) + 1);
                    }
                }
                stats.put(q.getQuestionId(), scaleCounts);
            } else if (q.getQuestionType().equals("GRID")) {
                QuestionSetting s = settingsMap.get(q.getQuestionId());
                if (s != null && s.getGridRowsText() != null && s.getGridColumnsText() != null) {
                    String[] rows = s.getGridRowsText().split("\\|");
                    String[] cols = s.getGridColumnsText().split("\\|");
                    
                    Map<String, Map<String, Long>> rowStats = new java.util.HashMap<>();
                    for (String row : rows) {
                        Map<String, Long> colCounts = new java.util.HashMap<>();
                        for (String col : cols) colCounts.put(col.trim(), 0L);
                        rowStats.put(row.trim(), colCounts);
                    }

                    for (Answer a : allAnswers) {
                        if (a.getAnswerText() != null && a.getAnswerText().startsWith("row_")) {
                            try {
                                String[] parts = a.getAnswerText().split(":");
                                int rIdx = Integer.parseInt(parts[0].substring(4));
                                int cIdx = Integer.parseInt(parts[1]);
                                if (rIdx < rows.length && cIdx < cols.length) {
                                    String rowName = rows[rIdx].trim();
                                    String colName = cols[cIdx].trim();
                                    Map<String, Long> colCounts = rowStats.get(rowName);
                                    colCounts.put(colName, colCounts.get(colName) + 1);
                                }
                            } catch (Exception e) {}
                        }
                    }
                    gridStats.put(q.getQuestionId(), rowStats);
                }
            } else {
                List<String> answers = allAnswers.stream()
                    .map(Answer::getAnswerText)
                    .filter(t -> t != null && !t.isEmpty())
                    .collect(Collectors.toList());
                textAnswers.put(q.getQuestionId(), answers);
            }
        }
        
        model.addAttribute("stats", stats);
        model.addAttribute("textAnswers", textAnswers);
        model.addAttribute("gridStats", gridStats);
        model.addAttribute("settingsMap", settingsMap);
        
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
