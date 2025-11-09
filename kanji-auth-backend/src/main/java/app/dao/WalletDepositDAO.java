package app.dao;

import app.model.WalletDeposit;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * DAO phục vụ tính năng nạp tiền qua QR code.
 */
public class WalletDepositDAO extends BaseDAO {

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Tạo giao dịch nạp tiền mới với trạng thái PENDING.
     *
     * @param userId id người dùng.
     * @param amount số tiền nạp.
     * @param qrUrl  URL ảnh QR trả về cho người dùng.
     * @return đối tượng {@link WalletDeposit} mới được tạo.
     * @throws SQLException lỗi thao tác DB.
     */
    public WalletDeposit create(long userId, BigDecimal amount, String qrUrl) throws SQLException {
        final String sql = "INSERT INTO WalletDeposit (user_id, amount, qrCodeUrl) VALUES (?,?,?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, qrUrl);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    return findById(id, connection);
                }
            }
        }
        throw new SQLException("Unable to create deposit");
    }

    private WalletDeposit findById(long id, Connection existing) throws SQLException {
        final String sql = "SELECT deposit_id, user_id, amount, qrCodeUrl, status, createdAt, updatedAt FROM WalletDeposit WHERE deposit_id = ?";
        try (PreparedStatement ps = existing.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    WalletDeposit deposit = new WalletDeposit();
                    deposit.setDepositId(rs.getLong("deposit_id"));
                    deposit.setUserId(rs.getLong("user_id"));
                    deposit.setAmount(rs.getBigDecimal("amount"));
                    deposit.setQrCodeUrl(rs.getString("qrCodeUrl"));
                    deposit.setStatus(rs.getString("status"));
                    deposit.setCreatedAt(toLocalDateTime(rs.getTimestamp("createdAt")));
                    deposit.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updatedAt")));
                    return deposit;
                }
            }
        }
        throw new SQLException("Deposit not found after insert");
    }
}
