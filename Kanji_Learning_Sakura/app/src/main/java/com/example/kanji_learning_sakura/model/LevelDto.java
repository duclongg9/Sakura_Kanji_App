package com.example.kanji_learning_sakura.model;

/**
 * DTO level được chọn trong admin và học Kanji.
 */
public class LevelDto {
    private int id;
    private String name;
    private int jlptLevelId;
    private String description;
    private boolean active;
    private String accessTier;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getJlptLevelId() {
        return jlptLevelId;
    }

    public void setJlptLevelId(int jlptLevelId) {
        this.jlptLevelId = jlptLevelId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getAccessTier() {
        return accessTier;
    }

    public void setAccessTier(String accessTier) {
        this.accessTier = accessTier;
    }

    @Override
    public String toString() {
        return name;
    }
}
