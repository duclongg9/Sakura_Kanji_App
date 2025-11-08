package com.example.kanji_learning_sakura.model;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO câu hỏi trắc nghiệm kèm danh sách phương án.
 */
public class QuizQuestionDto {
    private long questionId;
    private long lessonId;
    private String prompt;
    private String explanation;
    private int orderIndex;
    private final List<QuizChoiceDto> choices = new ArrayList<>();

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public long getLessonId() {
        return lessonId;
    }

    public void setLessonId(long lessonId) {
        this.lessonId = lessonId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public List<QuizChoiceDto> getChoices() {
        return choices;
    }
}
