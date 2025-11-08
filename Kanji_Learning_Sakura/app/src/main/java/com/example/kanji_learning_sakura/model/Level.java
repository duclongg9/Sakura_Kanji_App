package com.example.kanji_learning_sakura.model;

import java.time.LocalDateTime;

/**
 * Level do admin tạo, gắn với JLPTLevel và cờ accessTier để lọc theo role.
 * DB: Level(id, name, jlptLevelId, description, isActive, accessTier, createdAt, updatedAt)
 */
public class Level {
    private int id;
    private String name;
    private int jlptLevelId;         // FK -> JLPTLevel.id
    private String description;      // nullable
    private boolean isActive = true;
    private AccessTier accessTier = AccessTier.FREE;
    private LocalDateTime createdAt; // mapping TIMESTAMP
    private LocalDateTime updatedAt;

    // (Optional) khi JOIN chi tiết, có thể set kèm đối tượng JLPTLevel
    private transient JLPTLevel jlpt; // không bắt buộc map DB, dùng tiện cho view

    public Level() {}

    public Level(int id, String name, int jlptLevelId, String description,
                 boolean isActive, AccessTier accessTier,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.jlptLevelId = jlptLevelId;
        this.description = description;
        this.isActive = isActive;
        this.accessTier = accessTier;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // getters/setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getJlptLevelId() { return jlptLevelId; }
    public void setJlptLevelId(int jlptLevelId) { this.jlptLevelId = jlptLevelId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public AccessTier getAccessTier() { return accessTier; }
    public void setAccessTier(AccessTier accessTier) { this.accessTier = accessTier; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public JLPTLevel getJlpt() { return jlpt; }
    public void setJlpt(JLPTLevel jlpt) { this.jlpt = jlpt; }

    @Override public String toString() {
        return "Level{id=" + id + ", name='" + name + '\'' +
                ", jlptLevelId=" + jlptLevelId +
                ", accessTier=" + accessTier +
                ", isActive=" + isActive + "}";
    }
}
