package com.example.kanji_learning_sakura.data.dao;

import com.example.kanji_learning_sakura.data.DBConnection;
import com.example.kanji_learning_sakura.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class UserDAO extends BaseDAO {

    private static LocalDateTime ts(Timestamp t) {
        return t == null ? null : t.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    private static final String LOGIN_SQL =
            "SELECT id, userName, email, imgUrl, matKhau, roleId, createdAt, updatedAt " +
                    "FROM `User` WHERE (LOWER(email) = LOWER(?)) " +
                    "AND matKhau = ? LIMIT 1";

    /**
     * Thử đăng nhập bằng email/username và mật khẩu.
     *
     * @param identifier email người dùng nhập.
     * @param password   mật khẩu dạng plain-text (khớp với cột {@code matKhau}).
     * @return {@link User} nếu thông tin hợp lệ, {@code null} nếu sai.
     * @throws SQLException           lỗi khi truy vấn CSDL.
     * @throws ClassNotFoundException thiếu driver MySQL.
     */
    public User authenticate(String identifier, String password) throws SQLException, ClassNotFoundException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(LOGIN_SQL)) {
            ps.setString(1, identifier);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Chuyển đổi {@link ResultSet} thành đối tượng {@link User}.
     */
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUserName(rs.getString("userName"));
        user.setEmail(rs.getString("email"));
        user.setImgUrl(rs.getString("imgUrl"));
        user.setMatKhau(rs.getString("matKhau"));
        user.setRoleId(rs.getInt("roleId"));

        user.setCreatedAt(ts(rs.getTimestamp("createdAt"))); // ⬅ dùng helper
        user.setUpdatedAt(ts(rs.getTimestamp("updatedAt"))); // ⬅ dùng helper
        return user;
    }

    public Integer findRoleIdByUserId(long userId) throws SQLException {
        String sql = "SELECT roleId FROM User WHERE id = ?";
        try (var c = getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("roleId");
                return null;
            }
        }
    }

}
