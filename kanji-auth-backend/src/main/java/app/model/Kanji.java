package app.model;

import java.time.LocalDateTime;

/**
 * Thực thể Kanji nằm trong từng level.
 */
public class Kanji {
    private long id;
    private String character;
    private String hanViet;
    private String onReading;
    private String kunReading;
    private String description;
    private Integer levelId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public String getHanViet() {
        return hanViet;
    }

    public void setHanViet(String hanViet) {
        this.hanViet = hanViet;
    }

    public String getOnReading() {
        return onReading;
    }

    public void setOnReading(String onReading) {
        this.onReading = onReading;
    }

    public String getKunReading() {
        return kunReading;
    }

    public void setKunReading(String kunReading) {
        this.kunReading = kunReading;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getLevelId() {
        return levelId;
    }

    public void setLevelId(Integer levelId) {
        this.levelId = levelId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
