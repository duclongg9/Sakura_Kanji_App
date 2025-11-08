package app.model;

import java.time.LocalDateTime;

/**
 * Thông tin level (bài học) cụ thể bên trong một cấp JLPT.
 */
public class Level {
    private int id;
    private String name;
    private int jlptLevelId;
    private String description;
    private boolean active;
    private String accessTier;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
