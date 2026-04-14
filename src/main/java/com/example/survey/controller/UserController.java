package com.example.survey.controller;

import com.example.survey.model.Form;
import com.example.survey.model.User;
import com.example.survey.repository.FormRepository;
import com.example.survey.repository.SurveyResponseRepository;
import com.example.survey.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final FormRepository formRepository;
    private final SurveyResponseRepository surveyResponseRepository;

    public UserController(UserRepository userRepository, FormRepository formRepository, SurveyResponseRepository surveyResponseRepository) {
        this.userRepository = userRepository;
        this.formRepository = formRepository;
        this.surveyResponseRepository = surveyResponseRepository;
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<Form> userForms = formRepository.findByOwnerIdOrderByFormIdDesc(user.getUserId());
        
        long totalResponses = userForms.stream()
                .mapToLong(form -> surveyResponseRepository.findByFormId(form.getFormId()).size())
                .sum();

        List<Form> latestForms = userForms.stream().limit(5).collect(Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("formsCount", userForms.size());
        model.addAttribute("totalResponses", totalResponses);
        model.addAttribute("latestForms", latestForms);
        
        return "profile";
    }
}
