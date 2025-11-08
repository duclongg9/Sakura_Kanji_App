package com.example.kanji_learning_sakura.model;

/**
 * DTO Kanji dùng cho danh sách và chi tiết.
 */
public class KanjiDto {
    private long id;
    private String character;
    private String hanViet;
    private String onReading;
    private String kunReading;
    private String description;
    private Integer levelId;

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
}
