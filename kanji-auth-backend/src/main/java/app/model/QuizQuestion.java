package app.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Câu hỏi luyện thi cho từng bài học.
 */
public class QuizQuestion {
    private long id;
    private long lessonId;
    private String prompt;
    private String explanation;
    private int orderIndex;
    private final List<QuizChoice> choices = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public List<QuizChoice> getChoices() {
        return choices;
    }
}
