package app.dao;

import app.model.Level;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng Level.
 */
public class LevelDAO extends BaseDAO {

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Lấy danh sách level theo JLPT.
     *
     * @param jlptId         id của cấp JLPT cần lấy.
     * @param includeInactive {@code true} nếu muốn bao gồm cả level đã ẩn.
     * @return danh sách level.
     * @throws SQLException nếu truy vấn thất bại.
     */
    public List<Level> findByJlpt(int jlptId, boolean includeInactive) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT id, name, jlptLevelId, description, isActive, accessTier, createdAt, updatedAt "
                + "FROM Level WHERE jlptLevelId = ? ");
        if (!includeInactive) {
            sql.append("AND isActive = 1 ");
        }
        sql.append("ORDER BY name");
        List<Level> levels = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.setInt(1, jlptId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    levels.add(map(rs));
                }
            }
        }
        return levels;
    }

    /**
     * Lấy danh sách level theo JLPT, chỉ trả về level đang hoạt động.
     */
    public List<Level> findByJlpt(int jlptId) throws SQLException {
        return findByJlpt(jlptId, false);
    }

    /**
     * Thêm level mới.
     */
    public Level insert(Level level) throws SQLException {
        final String sql = "INSERT INTO Level (name, jlptLevelId, description, isActive, accessTier) VALUES (?,?,?,?,?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, level.getName());
            ps.setInt(2, level.getJlptLevelId());
            ps.setString(3, level.getDescription());
            ps.setBoolean(4, level.isActive());
            ps.setString(5, level.getAccessTier());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    level.setId(keys.getInt(1));
                }
            }
        }
        return level;
    }

    /**
     * Lấy thông tin level theo khóa chính.
     *
     * @param id giá trị khóa chính.
     * @return {@link Level} nếu tồn tại, ngược lại {@code null}.
     * @throws SQLException nếu truy vấn gặp lỗi.
     */
    public Level findById(int id) throws SQLException {
        final String sql = "SELECT id, name, jlptLevelId, description, isActive, accessTier, createdAt, updatedAt FROM Level WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    /**
     * Cập nhật thông tin level đã tồn tại.
     *
     * @param level đối tượng level chứa dữ liệu mới.
     * @return {@code true} nếu câu lệnh cập nhật thành công.
     * @throws SQLException nếu truy vấn gặp lỗi.
     */
    public boolean update(Level level) throws SQLException {
        final String sql = "UPDATE Level SET name = ?, jlptLevelId = ?, description = ?, isActive = ?, accessTier = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, level.getName());
            ps.setInt(2, level.getJlptLevelId());
            ps.setString(3, level.getDescription());
            ps.setBoolean(4, level.isActive());
            ps.setString(5, level.getAccessTier());
            ps.setInt(6, level.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa level theo id.
     *
     * @param id khóa chính cần xóa.
     * @return {@code true} nếu đã xóa được dữ liệu.
     * @throws SQLException nếu truy vấn gặp lỗi.
     */
    public boolean delete(int id) throws SQLException {
        final String sql = "DELETE FROM Level WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Level map(ResultSet rs) throws SQLException {
        Level level = new Level();
        level.setId(rs.getInt("id"));
        level.setName(rs.getString("name"));
        level.setJlptLevelId(rs.getInt("jlptLevelId"));
        level.setDescription(rs.getString("description"));
        level.setActive(rs.getBoolean("isActive"));
        level.setAccessTier(rs.getString("accessTier"));
        level.setCreatedAt(toLocalDateTime(rs.getTimestamp("createdAt")));
        level.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updatedAt")));
        return level;
    }
}
