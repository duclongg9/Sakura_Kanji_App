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
import java.util.Locale;

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
                    AccountUpgradeRequest created = findById(id, connection);
                    if (created != null) {
                        return created;
                    }
                }
            }
        }
        throw new SQLException("Cannot create upgrade request");
    }

    /**
     * Tìm yêu cầu theo ID.
     *
     * @param requestId mã yêu cầu.
     * @return bản ghi {@link AccountUpgradeRequest} hoặc {@code null} nếu không tồn tại.
     * @throws SQLException lỗi truy vấn DB.
     */
    public AccountUpgradeRequest findById(long requestId) throws SQLException {
        try (Connection connection = getConnection()) {
            return findById(requestId, connection);
        }
    }

    /**
     * Lấy yêu cầu đang chờ xử lý mới nhất của người dùng.
     *
     * @param userId id người dùng.
     * @return yêu cầu mới nhất hoặc {@code null} nếu không có.
     * @throws SQLException lỗi truy vấn DB.
     */
    public AccountUpgradeRequest findLatestPendingByUser(long userId) throws SQLException {
        final String sql = "SELECT request_id, user_id, currentRoleId, targetRoleId, note, status, createdAt, processedAt "
                + "FROM AccountUpgradeRequest WHERE user_id = ? AND status = 'PENDING' "
                + "ORDER BY createdAt DESC LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Cập nhật trạng thái yêu cầu.
     *
     * @param requestId id yêu cầu cần cập nhật.
     * @param status    trạng thái mới (APPROVED/REJECTED/PENDING).
     * @return bản ghi sau khi cập nhật hoặc {@code null} nếu không có bản ghi phù hợp.
     * @throws SQLException lỗi truy vấn DB.
     */
    public AccountUpgradeRequest updateStatus(long requestId, String status) throws SQLException {
        String normalized = status != null ? status.toUpperCase(Locale.ROOT) : null;
        if (normalized == null) {
            throw new IllegalArgumentException("Status must not be null");
        }
        boolean pending = "PENDING".equals(normalized);
        StringBuilder sql = new StringBuilder("UPDATE AccountUpgradeRequest SET status = ?, processedAt = ");
        if (pending) {
            sql.append("NULL");
        } else {
            sql.append("CURRENT_TIMESTAMP");
        }
        sql.append(" WHERE request_id = ?");
        if (!pending) {
            sql.append(" AND status = 'PENDING'");
        }
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.setString(1, normalized);
            ps.setLong(2, requestId);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                return null;
            }
            return findById(requestId, connection);
        }
    }

    private AccountUpgradeRequest findById(long id, Connection existing) throws SQLException {
        final String sql = "SELECT request_id, user_id, currentRoleId, targetRoleId, note, status, createdAt, processedAt "
                + "FROM AccountUpgradeRequest WHERE request_id = ?";
        try (PreparedStatement ps = existing.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    private AccountUpgradeRequest mapRow(ResultSet rs) throws SQLException {
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
