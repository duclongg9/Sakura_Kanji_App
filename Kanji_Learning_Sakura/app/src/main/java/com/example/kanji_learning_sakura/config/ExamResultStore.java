package com.example.kanji_learning_sakura.config;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;

/**
 * Lưu và truy xuất kết quả bài thi gần nhất của từng level theo người dùng.
 */
public class ExamResultStore {

    private static final String FILE = "exam_results";
    private static final String SEP = "|";

    private final SharedPreferences prefs;

    public ExamResultStore(Context context) {
        this.prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    /**
     * Lưu kết quả mới nhất.
     */
    public void save(long userId, int levelId, int score, int total) {
        String value = score + SEP + total + SEP + System.currentTimeMillis();
        prefs.edit().putString(key(userId, levelId), value).apply();
    }

    /**
     * Đọc kết quả gần nhất nếu có.
     */
    @Nullable
    public ExamResult load(long userId, int levelId) {
        String raw = prefs.getString(key(userId, levelId), null);
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        String[] parts = raw.split("\\|");
        if (parts.length != 3) {
            return null;
        }
        try {
            int score = Integer.parseInt(parts[0]);
            int total = Integer.parseInt(parts[1]);
            long timestamp = Long.parseLong(parts[2]);
            return new ExamResult(score, total, timestamp);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String key(long userId, int levelId) {
        return "result_" + userId + "_" + levelId;
    }

    /**
     * DTO kết quả lưu trong SharedPreferences.
     */
    public static class ExamResult {
        private final int score;
        private final int total;
        private final long timestamp;

        public ExamResult(int score, int total, long timestamp) {
            this.score = score;
            this.total = total;
            this.timestamp = timestamp;
        }

        public int getScore() {
            return score;
        }

        public int getTotal() {
            return total;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
