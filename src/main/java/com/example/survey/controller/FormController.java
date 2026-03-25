package com.example.survey.controller;

import com.example.survey.model.Form;
import com.example.survey.model.Question;
import com.example.survey.model.User;
import com.example.survey.service.FormService;
import com.example.survey.service.QuestionService;
import com.example.survey.service.UserDetailsServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/forms")
public class FormController {

    private final FormService formService;
    private final QuestionService questionService;
    private final UserDetailsServiceImpl userDetailsService;

    public FormController(FormService formService, QuestionService questionService, UserDetailsServiceImpl userDetailsService) {
        this.formService = formService;
        this.questionService = questionService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping
    public String listForms(Model model) {
        User currentUser = userDetailsService.getCurrentUser();
        if (currentUser != null) {
            boolean isAdmin = currentUser.getIsAdmin() != null && currentUser.getIsAdmin() == 1;
            List<Form> forms = isAdmin ? formService.getAllForms() : formService.getFormsByUser(currentUser.getUserId());
            model.addAttribute("forms", forms);
            model.addAttribute("isAdmin", isAdmin);
        }
        return "forms/index";
    }

    @GetMapping("/public")
    public String publicForms(@RequestParam(required = false) String search, Model model) {
        model.addAttribute("forms", formService.getPublishedForms(search));
        model.addAttribute("search", search);
        return "forms/public";
    }

    @GetMapping("/create")
    public String createForm() {
        return "forms/create";
    }

    @PostMapping("/create")
    public String createForm(@RequestParam String title, @RequestParam(required = false) String description) {
        User currentUser = userDetailsService.getCurrentUser();
        if (currentUser != null) {
            formService.createForm(currentUser.getUserId(), title, description);
        }
        return "redirect:/forms";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        Form form = formService.getFormById(id).orElseThrow(() -> new IllegalArgumentException("Form not found"));
        // Check ownership or admin
        User currentUser = userDetailsService.getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.getIsAdmin() != null && currentUser.getIsAdmin() == 1;
        if (currentUser == null || (!form.getOwnerId().equals(currentUser.getUserId()) && !isAdmin)) {
            return "redirect:/login";
        }
        model.addAttribute("form", form);
        return "forms/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateForm(@PathVariable Integer id, @RequestParam String title, @RequestParam(required = false) String description) {
        Form form = formService.getFormById(id).orElseThrow(() -> new IllegalArgumentException("Form not found"));
        // Check ownership or admin
        User currentUser = userDetailsService.getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.getIsAdmin() != null && currentUser.getIsAdmin() == 1;
        if (currentUser == null || (!form.getOwnerId().equals(currentUser.getUserId()) && !isAdmin)) {
            return "redirect:/login";
        }
        form.setTitle(title);
        form.setDescription(description);
        formService.save(form);
        return "redirect:/forms/" + id;
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Integer id, Model model) {
        Form form = formService.getFormById(id).orElseThrow(() -> new IllegalArgumentException("Form not found"));
        model.addAttribute("form", form);
        
        List<Question> questions = questionService.getQuestionsByForm(id);
        
        // Ручная подгрузка данных для отображения
        java.util.Map<Integer, List<com.example.survey.model.Option>> optionsMap = new java.util.HashMap<>();
        java.util.Map<Integer, com.example.survey.model.QuestionSetting> settingsMap = new java.util.HashMap<>();
        
        for (Question q : questions) {
            optionsMap.put(q.getQuestionId(), questionService.getOptionsByQuestion(q.getQuestionId()));
            questionService.getQuestionSettings(q.getQuestionId()).ifPresent(s -> settingsMap.put(q.getQuestionId(), s));
        }
        
        model.addAttribute("questions", questions);
        model.addAttribute("optionsMap", optionsMap);
        model.addAttribute("settingsMap", settingsMap);
        return "forms/details";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        Form form = formService.getFormById(id).orElseThrow(() -> new IllegalArgumentException("Form not found"));
        User currentUser = userDetailsService.getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.getIsAdmin() != null && currentUser.getIsAdmin() == 1;
        
        if (currentUser != null && (form.getOwnerId().equals(currentUser.getUserId()) || isAdmin)) {
            formService.deleteForm(id);
        }
        return "redirect:/forms";
    }

    @PostMapping("/{id}/publish")
    public String publish(@PathVariable Integer id) {
        Form form = formService.getFormById(id).orElseThrow(() -> new IllegalArgumentException("Form not found"));
        form.setIsPublished((short) 1);
        formService.save(form);
        return "redirect:/forms/" + id;
    }

    @PostMapping("/{id}/unpublish")
    public String unpublish(@PathVariable Integer id) {
        Form form = formService.getFormById(id).orElseThrow(() -> new IllegalArgumentException("Form not found"));
        form.setIsPublished((short) 0);
        formService.save(form);
        return "redirect:/forms/" + id;
    }
}
