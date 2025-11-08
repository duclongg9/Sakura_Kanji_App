package com.example.kanji_learning_sakura.model;

/**
 * DTO phương án trả lời câu hỏi trắc nghiệm.
 */
public class QuizChoiceDto {
    private long choiceId;
    private long questionId;
    private String content;
    private boolean correct;

    public long getChoiceId() {
        return choiceId;
    }

    public void setChoiceId(long choiceId) {
        this.choiceId = choiceId;
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}
