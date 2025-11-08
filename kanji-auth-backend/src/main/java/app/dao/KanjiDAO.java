package app.dao;

import app.model.Kanji;
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
 * DAO cho bảng Kanji.
 */
public class KanjiDAO extends BaseDAO {

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public List<Kanji> findByLevel(int levelId) throws SQLException {
        final String sql = "SELECT id, kanji, hanViet, amOn, amKun, moTa, levelId, createdAt, updatedAt FROM Kanji WHERE levelId = ? ORDER BY kanji";
        List<Kanji> kanjiList = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, levelId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    kanjiList.add(map(rs));
                }
            }
        }
        return kanjiList;
    }

    public Kanji insert(Kanji kanji) throws SQLException {
        final String sql = "INSERT INTO Kanji (kanji, hanViet, amOn, amKun, moTa, levelId) VALUES (?,?,?,?,?,?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, kanji.getCharacter());
            ps.setString(2, kanji.getHanViet());
            ps.setString(3, kanji.getOnReading());
            ps.setString(4, kanji.getKunReading());
            ps.setString(5, kanji.getDescription());
            if (kanji.getLevelId() == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, kanji.getLevelId());
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    kanji.setId(keys.getLong(1));
                }
            }
        }
        return kanji;
    }

    private Kanji map(ResultSet rs) throws SQLException {
        Kanji kanji = new Kanji();
        kanji.setId(rs.getLong("id"));
        kanji.setCharacter(rs.getString("kanji"));
        kanji.setHanViet(rs.getString("hanViet"));
        kanji.setOnReading(rs.getString("amOn"));
        kanji.setKunReading(rs.getString("amKun"));
        kanji.setDescription(rs.getString("moTa"));
        int levelId = rs.getInt("levelId");
        kanji.setLevelId(rs.wasNull() ? null : levelId);
        kanji.setCreatedAt(toLocalDateTime(rs.getTimestamp("createdAt")));
        kanji.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updatedAt")));
        return kanji;
    }
}
