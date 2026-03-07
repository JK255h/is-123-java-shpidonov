package com.example.survey.controller;

import com.example.survey.model.Form;
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
            List<Form> myForms = formService.getFormsByUser(currentUser.getUserId());
            model.addAttribute("forms", myForms);
        }
        return "forms/index";
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

    @GetMapping("/{id}")
    public String details(@PathVariable Integer id, Model model) {
        Form form = formService.getFormById(id).orElseThrow(() -> new IllegalArgumentException("Form not found"));
        model.addAttribute("form", form);
        model.addAttribute("questions", questionService.getQuestionsByForm(id));
        return "forms/details";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        // Add ownership check here
        formService.deleteForm(id);
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
