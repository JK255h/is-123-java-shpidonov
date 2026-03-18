package com.example.survey.controller;

import com.example.survey.model.Form;
import com.example.survey.model.Question;
import com.example.survey.service.FormService;
import com.example.survey.service.QuestionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final FormService formService;

    public QuestionController(QuestionService questionService, FormService formService) {
        this.questionService = questionService;
        this.formService = formService;
    }

    @GetMapping("/create/{formId}")
    public String create(@PathVariable Integer formId, Model model) {
        Form form = formService.getFormById(formId).orElseThrow(() -> new IllegalArgumentException("Form not found"));
        model.addAttribute("form", form);
        return "questions/create";
    }

    @PostMapping("/create/{formId}")
    public String create(@PathVariable Integer formId,
                         @RequestParam String title,
                         @RequestParam String questionType,
                         @RequestParam(defaultValue = "false") Boolean isRequired,
                         @RequestParam(required = false) String description,
                         @RequestParam(value = "options", required = false) List<String> options,
                         @RequestParam(required = false) Integer scaleMin,
                         @RequestParam(required = false) Integer scaleMax) {
        Question question = questionService.createQuestion(formId, title, questionType, (short) (isRequired ? 1 : 0), description);
        
        // Сохраняем варианты для списков
        if (options != null && (questionType.equals("MULTIPLE_CHOICE") || questionType.equals("CHECKBOX"))) {
            for (String optionText : options) {
                if (optionText != null && !optionText.trim().isEmpty()) {
                    questionService.createOption(question.getQuestionId(), optionText.trim());
                }
            }
        }
        
        // Сохраняем настройки для шкалы
        if (questionType.equals("SCALE")) {
            com.example.survey.model.QuestionSetting settings = new com.example.survey.model.QuestionSetting();
            settings.setQuestionId(question.getQuestionId());
            settings.setScaleMin(scaleMin != null ? scaleMin : 1);
            settings.setScaleMax(scaleMax != null ? scaleMax : 5);
            questionService.saveQuestionSettings(settings);
        }
        
        return "redirect:/forms/" + formId;
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model) {
        Question question = questionService.getQuestionById(id).orElseThrow(() -> new IllegalArgumentException("Question not found"));
        model.addAttribute("question", question);
        model.addAttribute("options", questionService.getOptionsByQuestion(id));
        return "questions/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Integer id,
                         @RequestParam String title,
                         @RequestParam String questionType,
                         @RequestParam(defaultValue = "false") Boolean isRequired,
                         @RequestParam(required = false) String description,
                         @RequestParam(value = "options", required = false) List<String> options) {
        Question question = questionService.updateQuestion(id, title, questionType, (short) (isRequired ? 1 : 0), description);
        
        // Refresh options
        questionService.deleteOptionsByQuestion(id);
        if (options != null && (questionType.equals("MULTIPLE_CHOICE") || questionType.equals("CHECKBOX"))) {
            for (String optionText : options) {
                if (optionText != null && !optionText.trim().isEmpty()) {
                    questionService.createOption(id, optionText.trim());
                }
            }
        }
        
        return "redirect:/forms/" + question.getFormId();
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        Question question = questionService.getQuestionById(id).orElseThrow(() -> new IllegalArgumentException("Question not found"));
        Integer formId = question.getFormId();
        questionService.deleteQuestion(id);
        return "redirect:/forms/" + formId;
    }

    @PostMapping("/{id}/up")
    public String moveUp(@PathVariable Integer id) {
        Question q = questionService.getQuestionById(id).orElseThrow();
        questionService.moveUp(id);
        return "redirect:/forms/" + q.getFormId();
    }

    @PostMapping("/{id}/down")
    public String moveDown(@PathVariable Integer id) {
        Question q = questionService.getQuestionById(id).orElseThrow();
        questionService.moveDown(id);
        return "redirect:/forms/" + q.getFormId();
    }
}
