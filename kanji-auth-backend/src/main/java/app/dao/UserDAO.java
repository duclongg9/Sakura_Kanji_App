package app.dao;

import app.model.User;
import java.sql.Connection;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Truy xuất dữ liệu người dùng.
 */
public class UserDAO extends BaseDAO {

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * Tìm người dùng theo email/username và mật khẩu.
     *
     * @param identifier email hoặc tên đăng nhập.
     * @param password   mật khẩu plain-text lưu trong cột {@code matKhau}.
     * @return {@link User} nếu hợp lệ, null nếu sai thông tin.
     * @throws SQLException khi truy vấn thất bại.
     */
    public User login(String identifier, String password) throws SQLException {
        final String sql = "SELECT id, userName, email, imgUrl, matKhau, roleId, accountTier, vipExpiresAt, accountBalance, bio, createdAt, updatedAt "
                + "FROM `User` WHERE (LOWER(email) = LOWER(?) OR LOWER(userName) = LOWER(?)) AND matKhau = ? LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            ps.setString(3, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lưu thông tin người dùng OAuth (Google) nếu chưa tồn tại.
     *
     * @param email địa chỉ email đã xác thực.
     * @param name  tên hiển thị.
     * @param img   avatar (có thể null).
     * @return bản ghi người dùng.
     * @throws SQLException khi thao tác với DB thất bại.
     */
    public User ensureOAuthUser(String email, String name, String img) throws SQLException {
        final String selectSql = "SELECT id, userName, email, imgUrl, matKhau, roleId, accountTier, vipExpiresAt, accountBalance, bio, createdAt, updatedAt "
                + "FROM `User` WHERE email = ? LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement find = connection.prepareStatement(selectSql)) {
            find.setString(1, email);
            try (ResultSet rs = find.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }

            final String insertSql = "INSERT INTO `User` (userName, email, imgUrl, roleId, accountTier) VALUES (?,?,?,?,?)";
            try (PreparedStatement insert = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insert.setString(1, name != null ? name : email);
                insert.setString(2, email);
                insert.setString(3, img);
                insert.setInt(4, 2);
                insert.setString(5, "FREE");
                insert.executeUpdate();
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    if (keys.next()) {
                        long id = keys.getLong(1);
                        User user = new User();
                        user.setId(id);
                        user.setUserName(name != null ? name : email);
                        user.setEmail(email);
                        user.setImgUrl(img);
                        user.setRoleId(2);
                        user.setAccountTier("FREE");
                        user.setAccountBalance(BigDecimal.ZERO);
                        return user;
                    }
                }
            }
        }
        throw new SQLException("Unable to create OAuth user");
    }

    /**
     * Tìm người dùng theo ID.
     */
    public User findById(long id) throws SQLException {
        final String sql = "SELECT id, userName, email, imgUrl, matKhau, roleId, accountTier, vipExpiresAt, accountBalance, bio, createdAt, updatedAt FROM `User` WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    /**
     * Nâng cấp tài khoản lên VIP và cập nhật thời gian hết hạn.
     *
     * @param userId    người dùng cần nâng cấp.
     * @param expiresAt thời điểm VIP hết hạn.
     * @throws SQLException nếu thao tác cập nhật thất bại.
     */
    public void upgradeToVip(long userId, LocalDateTime expiresAt) throws SQLException {
        final String sql = "UPDATE `User` SET roleId = 3, accountTier = 'VIP', vipExpiresAt = ?, updatedAt = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            if (expiresAt == null) {
                ps.setNull(1, java.sql.Types.TIMESTAMP);
            } else {
                ps.setTimestamp(1, Timestamp.valueOf(expiresAt));
            }
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUserName(rs.getString("userName"));
        user.setEmail(rs.getString("email"));
        user.setImgUrl(rs.getString("imgUrl"));
        user.setPassword(rs.getString("matKhau"));
        user.setRoleId(rs.getInt("roleId"));
        user.setAccountTier(rs.getString("accountTier"));
        user.setVipExpiresAt(toLocalDateTime(rs.getTimestamp("vipExpiresAt")));
        user.setAccountBalance(rs.getBigDecimal("accountBalance"));
        user.setBio(rs.getString("bio"));
        user.setCreatedAt(toLocalDateTime(rs.getTimestamp("createdAt")));
        user.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updatedAt")));
        return user;
    }
}
