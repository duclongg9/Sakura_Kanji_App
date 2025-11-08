package com.example.kanji_learning_sakura.dao;

import com.example.kanji_learning_sakura.model.*;
import java.time.ZoneId;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class LevelDAO extends BaseDAO {

    // ===== Helpers =====
    private AccessTier toAccessTier(String s) {
        return s == null ? AccessTier.FREE : AccessTier.valueOf(s);
    }
    private static LocalDateTime ts(Timestamp t) {
        return t == null ? null : t.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private Level mapLevel(ResultSet rs) throws SQLException {
        Level l = new Level();
        l.setId(rs.getInt("id"));
        l.setName(rs.getString("name"));
        l.setJlptLevelId(rs.getInt("jlptLevelId"));
        l.setDescription(rs.getString("description"));
        l.setActive(rs.getBoolean("isActive"));
        l.setAccessTier(toAccessTier(rs.getString("accessTier")));
        l.setCreatedAt(ts(rs.getTimestamp("createdAt")));
        l.setUpdatedAt(ts(rs.getTimestamp("updatedAt")));

        // Nếu có cột JLPT đi kèm (JOIN)
        try {
            int jlptId = rs.getInt("jlpt_id");
            if (!rs.wasNull()) {
                JLPTLevel j = new JLPTLevel(jlptId, rs.getString("jlpt_name"));
                l.setJlpt(j);
            }
        } catch (SQLException ignore) {}
        return l;
    }

    // ===== CREATE =====
    public int create(Level level) throws SQLException {
        final String sql =
                "INSERT INTO Level(name, jlptLevelId, description, isActive, accessTier) " +
                        "VALUES (?,?,?,?,?)";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, level.getName());
            ps.setInt(2, level.getJlptLevelId());

            if (level.getDescription() == null) ps.setNull(3, Types.VARCHAR);
            else ps.setString(3, level.getDescription());

            ps.setBoolean(4, level.isActive());
            ps.setString(5, level.getAccessTier().name());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("No generated key for Level");
    }

    // ===== UPDATE =====
    public void update(Level level) throws SQLException {
        final String sql =
                "UPDATE Level " +
                        "   SET name=?, jlptLevelId=?, description=?, isActive=?, accessTier=? " +
                        " WHERE id=?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, level.getName());
            ps.setInt(2, level.getJlptLevelId());

            if (level.getDescription() == null) ps.setNull(3, Types.VARCHAR);
            else ps.setString(3, level.getDescription());

            ps.setBoolean(4, level.isActive());
            ps.setString(5, level.getAccessTier().name());
            ps.setInt(6, level.getId());
            ps.executeUpdate();
        }
    }

    // ===== DELETE (soft delete khuyên dùng; đây demo hard delete) =====
    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM Level WHERE id = ?";
        try (var c = getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ===== FIND BY ID (kèm JLPT) – kiểm tra quyền xem theo roleId =====
    public Optional<Level> findByIdForRole(int id, int roleId) throws SQLException {
        String sql = "SELECT l.*, j.id AS jlpt_id, j.nameLevel AS jlpt_name "
                + "FROM Level l "
                + "JOIN JLPTLevel j ON j.id = l.jlptLevelId "
                + "WHERE l.id = ? AND l.isActive = TRUE "
                + "AND ( ? IN (1,3) OR l.accessTier = 'FREE' )";
        try (var c = getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, roleId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapLevel(rs));
                return Optional.empty();
            }
        }
    }

    public List<Level> pageForRole(int roleId, int page, int pageSize) throws SQLException {
        int offset = Math.max(0, (page - 1) * pageSize);
        String sql = "SELECT l.*, j.id AS jlpt_id, j.nameLevel AS jlpt_name "
                + "FROM Level l JOIN JLPTLevel j ON j.id = l.jlptLevelId "
                + "WHERE l.isActive = TRUE AND ( ? IN (1,3) OR l.accessTier = 'FREE' ) "
                + "ORDER BY j.id, l.name LIMIT ? OFFSET ?";
        try (var c = getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            ps.setInt(2, pageSize);
            ps.setInt(3, offset);
            try (var rs = ps.executeQuery()) {
                List<Level> list = new ArrayList<>();
                while (rs.next()) list.add(mapLevel(rs));
                return list;
            }
        }
    }
    // ===== COUNT FOR ROLE =====
    public int countForRole(int roleId) throws SQLException {
        final String sql =
                "SELECT COUNT(*) " +
                        "FROM Level l " +
                        "WHERE l.isActive = TRUE " +
                        "  AND ( ? IN (1,3) OR l.accessTier = 'FREE' )";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
