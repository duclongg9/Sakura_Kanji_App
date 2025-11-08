package app.model;

/**
 * Mô tả cấp độ JLPT (N5 .. N1).
 */
public class JLPTLevel {
    private int id;
    private String nameLevel;

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
}
