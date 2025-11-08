package app.dao;

import app.model.JLPTLevel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO đọc danh sách cấp độ JLPT.
 */
public class JLPTLevelDAO extends BaseDAO {

    /**
     * Lấy toàn bộ danh sách cấp độ.
     */
    public List<JLPTLevel> findAll() throws SQLException {
        final String sql = "SELECT id, nameLevel FROM JLPTLevel ORDER BY id";
        List<JLPTLevel> levels = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                JLPTLevel level = new JLPTLevel();
                level.setId(rs.getInt("id"));
                level.setNameLevel(rs.getString("nameLevel"));
                levels.add(level);
            }
        }
        return levels;
    }
}
