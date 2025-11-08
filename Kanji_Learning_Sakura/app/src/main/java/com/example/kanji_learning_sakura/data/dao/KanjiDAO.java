package com.example.kanji_learning_sakura.data.dao;

import com.example.kanji_learning_sakura.model.Kanji;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class KanjiDAO extends BaseDAO {

    private static LocalDateTime ts(Timestamp t) {
        return t == null ? null : t.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private Kanji map(ResultSet rs) throws SQLException {
        Kanji k = new Kanji();
        k.setId(rs.getLong("id"));
        k.setKanji(rs.getString("kanji"));
        k.setHanViet(rs.getString("hanViet"));
        k.setAmOn(rs.getString("amOn"));
        k.setAmKun(rs.getString("amKun"));
        k.setMoTa(rs.getString("moTa"));
        int lvl = rs.getInt("levelId");
        k.setLevelId(rs.wasNull() ? null : lvl);
        k.setCreatedAt(ts(rs.getTimestamp("createdAt")));
        k.setUpdatedAt(ts(rs.getTimestamp("updatedAt")));
        return k;
    }

    public long create(Kanji k) throws SQLException {
        String sql = "INSERT INTO Kanji(kanji, hanViet, amOn, amKun, moTa, levelId) " +
                "VALUES (?,?,?,?,?,?)";
        try (var c = getConnection();
             var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, k.getKanji());
            ps.setString(2, k.getHanViet());
            ps.setString(3, k.getAmOn());
            ps.setString(4, k.getAmKun());
            ps.setString(5, k.getMoTa());
            if (k.getLevelId() == null) ps.setNull(6, Types.INTEGER);
            else ps.setInt(6, k.getLevelId());
            ps.executeUpdate();
            try (var keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        }
        throw new SQLException("No generated key for Kanji");
    }

    public void update(Kanji k) throws SQLException {
        String sql = "UPDATE Kanji SET kanji=?, hanViet=?, amOn=?, amKun=?, moTa=?, levelId=? " +
                "WHERE id=?";
        try (var c = getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, k.getKanji());
            ps.setString(2, k.getHanViet());
            ps.setString(3, k.getAmOn());
            ps.setString(4, k.getAmKun());
            ps.setString(5, k.getMoTa());
            if (k.getLevelId() == null) ps.setNull(6, Types.INTEGER);
            else ps.setInt(6, k.getLevelId());
            ps.setLong(7, k.getId());
            ps.executeUpdate();
        }
    }

    public Optional<Kanji> findByIdForRole(long id, int roleId) throws SQLException {
        String sql = "SELECT k.* FROM Kanji k " +
                "LEFT JOIN Level l ON l.id = k.levelId " +
                "WHERE k.id = ? " +
                "AND ( l.id IS NULL OR l.isActive = TRUE ) " +
                "AND ( ? IN (1,3) OR l.accessTier = 'FREE' OR l.id IS NULL )";
        try (var c = getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setInt(2, roleId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        }
    }

    public List<Kanji> listByLevelForRole(int levelId, int roleId, int page, int pageSize) throws SQLException {
        int offset = Math.max(0, (page - 1) * pageSize);
        String sql = "SELECT k.* FROM Kanji k " +
                "JOIN Level l ON l.id = k.levelId " +
                "WHERE k.levelId = ? " +
                "AND l.isActive = TRUE " +
                "AND ( ? IN (1,3) OR l.accessTier = 'FREE' ) " +
                "ORDER BY k.id " +
                "LIMIT ? OFFSET ?";
        try (var c = getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setInt(1, levelId);
            ps.setInt(2, roleId);
            ps.setInt(3, pageSize);
            ps.setInt(4, offset);
            try (var rs = ps.executeQuery()) {
                List<Kanji> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    public int countByLevelForRole(int levelId, int roleId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Kanji k " +
                "JOIN Level l ON l.id = k.levelId " +
                "WHERE k.levelId = ? " +
                "AND l.isActive = TRUE " +
                "AND ( ? IN (1,3) OR l.accessTier = 'FREE' )";
        try (var c = getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setInt(1, levelId);
            ps.setInt(2, roleId);
            try (var rs = ps.executeQuery()) {
                rs.next(); return rs.getInt(1);
            }
        }
    }
}
