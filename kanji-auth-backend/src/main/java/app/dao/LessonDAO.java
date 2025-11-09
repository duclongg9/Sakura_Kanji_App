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

    /**
     * Lấy danh sách bài học thuộc một level.
     *
     * @param levelId id level cần truy vấn.
     * @return danh sách bài học.
     * @throws SQLException nếu truy vấn gặp lỗi.
     */
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

    /**
     * Tạo mới một bài học trong cơ sở dữ liệu.
     *
     * @param lesson dữ liệu bài học cần lưu.
     * @return bài học kèm khóa chính vừa tạo.
     * @throws SQLException nếu câu lệnh SQL lỗi.
     */
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

    /**
     * Lấy thông tin bài học theo khóa chính.
     *
     * @param id khóa chính bài học.
     * @return {@link Lesson} hoặc {@code null} nếu không tồn tại.
     * @throws SQLException nếu truy vấn lỗi.
     */
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

    /**
     * Cập nhật nội dung bài học.
     *
     * @param lesson bản ghi đã chỉnh sửa.
     * @return {@code true} nếu cập nhật thành công.
     * @throws SQLException nếu có lỗi SQL.
     */
    public boolean update(Lesson lesson) throws SQLException {
        final String sql = "UPDATE lessons SET level_id = ?, title = ?, overview = ?, order_index = ? WHERE lesson_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, lesson.getLevelId());
            ps.setString(2, lesson.getTitle());
            ps.setString(3, lesson.getOverview());
            ps.setInt(4, lesson.getOrderIndex());
            ps.setLong(5, lesson.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa bài học theo id.
     *
     * @param id khóa chính cần xóa.
     * @return {@code true} nếu xóa thành công.
     * @throws SQLException nếu gặp lỗi truy vấn.
     */
    public boolean delete(long id) throws SQLException {
        final String sql = "DELETE FROM lessons WHERE lesson_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
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
