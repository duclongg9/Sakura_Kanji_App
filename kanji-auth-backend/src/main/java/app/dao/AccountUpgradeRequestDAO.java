package app.dao;

import app.model.AccountUpgradeRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * DAO xử lý lưu trữ yêu cầu nâng cấp tài khoản VIP.
 */
public class AccountUpgradeRequestDAO extends BaseDAO {

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Kiểm tra người dùng đã có yêu cầu đang chờ xử lý chưa.
     *
     * @param userId id người dùng.
     * @return {@code true} nếu có yêu cầu đang ở trạng thái PENDING.
     * @throws SQLException lỗi truy vấn DB.
     */
    public boolean hasPendingRequest(long userId) throws SQLException {
        final String sql = "SELECT 1 FROM AccountUpgradeRequest WHERE user_id = ? AND status = 'PENDING' LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Tạo yêu cầu nâng cấp mới.
     *
     * @param userId        id người dùng gửi yêu cầu.
     * @param currentRoleId role hiện tại.
     * @param targetRoleId  role mong muốn.
     * @param note          ghi chú thêm từ người dùng.
     * @return đối tượng {@link AccountUpgradeRequest} vừa tạo.
     * @throws SQLException lỗi khi ghi DB.
     */
    public AccountUpgradeRequest create(long userId, int currentRoleId, int targetRoleId, String note) throws SQLException {
        final String sql = "INSERT INTO AccountUpgradeRequest (user_id, currentRoleId, targetRoleId, note) VALUES (?,?,?,?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.setInt(2, currentRoleId);
            ps.setInt(3, targetRoleId);
            ps.setString(4, note);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    return findById(id, connection);
                }
            }
        }
        throw new SQLException("Cannot create upgrade request");
    }

    private AccountUpgradeRequest findById(long id, Connection existing) throws SQLException {
        final String sql = "SELECT request_id, user_id, currentRoleId, targetRoleId, note, status, createdAt, processedAt "
                + "FROM AccountUpgradeRequest WHERE request_id = ?";
        try (PreparedStatement ps = existing.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AccountUpgradeRequest request = new AccountUpgradeRequest();
                    request.setRequestId(rs.getLong("request_id"));
                    request.setUserId(rs.getLong("user_id"));
                    request.setCurrentRoleId(rs.getInt("currentRoleId"));
                    request.setTargetRoleId(rs.getInt("targetRoleId"));
                    request.setNote(rs.getString("note"));
                    request.setStatus(rs.getString("status"));
                    request.setCreatedAt(toLocalDateTime(rs.getTimestamp("createdAt")));
                    request.setProcessedAt(toLocalDateTime(rs.getTimestamp("processedAt")));
                    return request;
                }
            }
        }
        throw new SQLException("Upgrade request not found after insert");
    }
}
