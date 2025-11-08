package app.dao;

import app.model.Lesson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO xử lý bảng Lesson.
 */
public class LessonDAO extends BaseDAO {

    public List<Lesson> findByLevel(int levelId) throws SQLException {
        final String sql = "SELECT lesson_id, level_id, title, overview, order_index FROM lessons WHERE level_id = ? ORDER BY order_index";
        List<Lesson> lessons = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, levelId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lessons.add(map(rs));
                }
            }
        }
        return lessons;
    }

    public Lesson insert(Lesson lesson) throws SQLException {
        final String sql = "INSERT INTO lessons (level_id, title, overview, order_index) VALUES (?,?,?,?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, lesson.getLevelId());
            ps.setString(2, lesson.getTitle());
            ps.setString(3, lesson.getOverview());
            ps.setInt(4, lesson.getOrderIndex());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    lesson.setId(keys.getLong(1));
                }
            }
        }
        return lesson;
    }

    public Lesson findById(long id) throws SQLException {
        final String sql = "SELECT lesson_id, level_id, title, overview, order_index FROM lessons WHERE lesson_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    private Lesson map(ResultSet rs) throws SQLException {
        Lesson lesson = new Lesson();
        lesson.setId(rs.getLong("lesson_id"));
        lesson.setLevelId(rs.getInt("level_id"));
        lesson.setTitle(rs.getString("title"));
        lesson.setOverview(rs.getString("overview"));
        lesson.setOrderIndex(rs.getInt("order_index"));
        return lesson;
    }
}
