package com.example.kanji_learning_sakura.model;

/** JLPT cố định: N5..N1 (bảng JLPTLevel: id, nameLevel). */
public class JLPTLevel {
    private int id;              // PK (INT AUTO_INCREMENT)
    private String nameLevel;    // "N5".."N1"

    public JLPTLevel() {}

    public JLPTLevel(int id, String nameLevel) {
        this.id = id;
        this.nameLevel = nameLevel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNameLevel() {
        return nameLevel;
    }
    public void setNameLevel(String nameLevel) {
        this.nameLevel = nameLevel;
    }

    @Override public String toString() {
        return "JLPTLevel{id=" + id + ", nameLevel='" + nameLevel + "'}";
    }
}
