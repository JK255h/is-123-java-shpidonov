package com.example.survey.controller;

import com.example.survey.model.Form;
import com.example.survey.model.Question;
import com.example.survey.model.User;
import com.example.survey.service.FormService;
import com.example.survey.service.QuestionService;
import com.example.survey.service.ResponseService;
import com.example.survey.service.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/survey")
public class SurveyController {

    private final FormService formService;
    private final QuestionService questionService;
    private final ResponseService responseService;
    private final UserDetailsServiceImpl userDetailsService;

    public SurveyController(FormService formService, QuestionService questionService, ResponseService responseService, UserDetailsServiceImpl userDetailsService) {
        this.formService = formService;
        this.questionService = questionService;
        this.responseService = responseService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/{id}")
    public String takeSurvey(@PathVariable Integer id, Model model) {
        Form form = formService.getFormById(id).orElseThrow(() -> new IllegalArgumentException("Form not found"));
        if (form.getIsPublished() == 0) {
            model.addAttribute("error", "Этот опрос еще не опубликован.");
            return "error";
        }
        
        List<Question> questions = questionService.getQuestionsByForm(id);
        model.addAttribute("form", form);
        model.addAttribute("questions", questions);
        
        // Add options for each question
        Map<Integer, List<com.example.survey.model.Option>> optionsMap = new java.util.HashMap<>();
        for (Question q : questions) {
            if (q.getQuestionType().equals("MULTIPLE_CHOICE") || q.getQuestionType().equals("CHECKBOX")) {
                optionsMap.put(q.getQuestionId(), questionService.getOptionsByQuestion(q.getQuestionId()));
            }
        }
        model.addAttribute("optionsMap", optionsMap);
        
        return "survey/take";
    }

    @PostMapping("/{id}/submit")
    public String submitSurvey(@PathVariable Integer id, HttpServletRequest request) {
        User currentUser = userDetailsService.getCurrentUser();
        Integer userId = (currentUser != null) ? currentUser.getUserId() : null;
        
        Map<String, String[]> params = request.getParameterMap();
        responseService.saveResponse(id, userId, params);
        
        return "redirect:/survey/success";
    }

    @GetMapping("/success")
    public String success() {
        return "survey/success";
    }
}
