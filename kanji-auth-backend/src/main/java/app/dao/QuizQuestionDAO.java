package app.dao;

import app.model.QuizChoice;
import app.model.QuizQuestion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO đọc câu hỏi luyện thi.
 */
public class QuizQuestionDAO extends BaseDAO {

    /**
     * Lấy danh sách câu hỏi theo lesson.
     */
    public List<QuizQuestion> findByLesson(long lessonId) throws SQLException {
        final String sql = "SELECT q.question_id, q.lesson_id, q.prompt, q.explanation, q.order_index, "
                + "c.choice_id, c.content, c.is_correct "
                + "FROM quiz_questions q "
                + "LEFT JOIN quiz_choices c ON c.question_id = q.question_id "
                + "WHERE q.lesson_id = ? ORDER BY q.order_index, c.choice_id";
        Map<Long, QuizQuestion> questionMap = new LinkedHashMap<>();
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, lessonId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long questionId = rs.getLong("question_id");
                    QuizQuestion question = questionMap.computeIfAbsent(questionId, id -> {
                        QuizQuestion q = new QuizQuestion();
                        q.setId(id);
                        q.setLessonId(lessonId);
                        q.setPrompt(getSafeString(rs, "prompt"));
                        q.setExplanation(getSafeString(rs, "explanation"));
                        q.setOrderIndex(rs.getInt("order_index"));
                        return q;
                    });

                    long choiceId = rs.getLong("choice_id");
                    if (!rs.wasNull()) {
                        QuizChoice choice = new QuizChoice();
                        choice.setId(choiceId);
                        choice.setQuestionId(questionId);
                        choice.setContent(getSafeString(rs, "content"));
                        choice.setCorrect(rs.getBoolean("is_correct"));
                        question.getChoices().add(choice);
                    }
                }
            }
        }
        return new ArrayList<>(questionMap.values());
    }

    private String getSafeString(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (SQLException e) {
            return null;
        }
    }
}
