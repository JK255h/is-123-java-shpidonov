package com.example.survey.service;

import com.example.survey.model.Option;
import com.example.survey.model.Question;
import com.example.survey.model.QuestionSetting;
import com.example.survey.repository.OptionRepository;
import com.example.survey.repository.QuestionRepository;
import com.example.survey.repository.QuestionSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final QuestionSettingRepository questionSettingRepository;

    public QuestionService(QuestionRepository questionRepository, OptionRepository optionRepository, QuestionSettingRepository questionSettingRepository) {
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.questionSettingRepository = questionSettingRepository;
    }

    public List<Question> getQuestionsByForm(Integer formId) {
        return questionRepository.findByFormIdOrderByQuestionId(formId);
    }
    
    public Optional<Question> getQuestionById(Integer questionId) {
        return questionRepository.findById(questionId);
    }

    public Question createQuestion(Integer formId, String title, String questionType, Short isRequired, String description) {
        Question question = new Question();
        question.setFormId(formId);
        question.setTitle(title);
        question.setQuestionType(questionType);
        question.setIsRequired(isRequired);
        question.setDescription(description);
        question.setCreatedAt(LocalDateTime.now());
        return questionRepository.save(question);
    }

    public Question updateQuestion(Integer questionId, String title, String questionType, Short isRequired, String description) {
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new IllegalArgumentException("Question not found"));
        question.setTitle(title);
        question.setQuestionType(questionType);
        question.setIsRequired(isRequired);
        question.setDescription(description);
        return questionRepository.save(question);
    }
    
    @Transactional
    public void deleteQuestion(Integer questionId) {
        // Cascade delete options
        optionRepository.deleteByQuestionId(questionId);
        // Cascade delete settings if exists
        if (questionSettingRepository.existsById(questionId)) {
            questionSettingRepository.deleteById(questionId);
        }
        questionRepository.deleteById(questionId);
    }

    public List<Option> getOptionsByQuestion(Integer questionId) {
        return optionRepository.findByQuestionIdOrderByOptionId(questionId);
    }
    
    public Option createOption(Integer questionId, String text) {
        Option option = new Option();
        option.setQuestionId(questionId);
        option.setOptionText(text);
        return optionRepository.save(option);
    }
    
    @Transactional
    public void deleteOptionsByQuestion(Integer questionId) {
        optionRepository.deleteByQuestionId(questionId);
    }

    public Optional<QuestionSetting> getQuestionSettings(Integer questionId) {
        return questionSettingRepository.findById(questionId);
    }
    
    public QuestionSetting saveQuestionSettings(QuestionSetting settings) {
        return questionSettingRepository.save(settings);
    }
}
