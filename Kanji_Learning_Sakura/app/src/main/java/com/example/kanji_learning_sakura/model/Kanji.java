package com.example.kanji_learning_sakura.model;

import java.time.LocalDateTime;

/**
 * Kanji gắn vào Level (tuỳ chọn để list theo Level).
 * DB: Kanji(id, kanji, hanViet, amOn, amKun, moTa, levelId, createdAt, updatedAt)
 */
public class Kanji {
    private long id;
    private String kanji;        // ký tự Kanji (nếu muốn chặt chẽ: enforce 1 kí tự ở DAO/validate)
    private String hanViet;      // nullable
    private String amOn;         // nullable
    private String amKun;        // nullable
    private String moTa;         // nullable
    private Integer levelId;     // nullable (FK -> Level.id)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // (Optional) khi JOIN chi tiết
    private transient Level level;

    public Kanji() {}

    public Kanji(long id, String kanji, String hanViet, String amOn, String amKun,
                 String moTa, Integer levelId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.kanji = kanji;
        this.hanViet = hanViet;
        this.amOn = amOn;
        this.amKun = amKun;
        this.moTa = moTa;
        this.levelId = levelId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // getters/setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getKanji() { return kanji; }
    public void setKanji(String kanji) { this.kanji = kanji; }

    public String getHanViet() { return hanViet; }
    public void setHanViet(String hanViet) { this.hanViet = hanViet; }

    public String getAmOn() { return amOn; }
    public void setAmOn(String amOn) { this.amOn = amOn; }

    public String getAmKun() { return amKun; }
    public void setAmKun(String amKun) { this.amKun = amKun; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public Integer getLevelId() { return levelId; }
    public void setLevelId(Integer levelId) { this.levelId = levelId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Level getLevel() { return level; }
    public void setLevel(Level level) { this.level = level; }

    @Override public String toString() {
        return "Kanji{id=" + id + ", kanji='" + kanji + "'}";
    }
}
