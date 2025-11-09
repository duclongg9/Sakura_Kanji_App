package app.dao;

import app.model.MomoPayment;
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
 * DAO quản lý bảng lưu giao dịch MoMo.
 */
public class MomoPaymentDAO extends BaseDAO {

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Tạo bản ghi giao dịch ở trạng thái PENDING.
     *
     * @param userId    người dùng thực hiện giao dịch.
     * @param amount    số tiền cần thanh toán.
     * @param planCode  gói nâng cấp áp dụng.
     * @param orderId   mã đơn hàng do server tạo.
     * @param requestId mã request gửi tới MoMo.
     * @param payUrl    URL thanh toán trên web.
     * @param deeplink  deeplink mở ứng dụng MoMo (có thể null).
     * @return bản ghi {@link MomoPayment} đã lưu.
     * @throws SQLException khi thao tác INSERT thất bại.
     */
    public MomoPayment createPending(long userId, BigDecimal amount, String planCode, String orderId, String requestId,
                                     String payUrl, String deeplink) throws SQLException {
        final String sql = "INSERT INTO MomoPayment (user_id, amount, plan_code, order_id, request_id, payUrl, deeplink) "
                + "VALUES (?,?,?,?,?,?,?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, planCode);
            ps.setString(4, orderId);
            ps.setString(5, requestId);
            ps.setString(6, payUrl);
            ps.setString(7, deeplink);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return findByOrderId(orderId);
                }
            }
        }
        throw new SQLException("Unable to create MoMo payment");
    }

    /**
     * Tìm giao dịch theo orderId.
     */
    public MomoPayment findByOrderId(String orderId) throws SQLException {
        final String sql = "SELECT payment_id, user_id, amount, plan_code, order_id, request_id, status, payUrl, deeplink, "
                + "resultCode, message, momoTransId, createdAt, updatedAt FROM MomoPayment WHERE order_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    /**
     * Cập nhật trạng thái giao dịch khi thanh toán thành công.
     */
    public boolean markSuccess(String orderId, int resultCode, String message, String momoTransId) throws SQLException {
        final String sql = "UPDATE MomoPayment SET status = 'SUCCESS', resultCode = ?, message = ?, momoTransId = ? "
                + "WHERE order_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, resultCode);
            ps.setString(2, message);
            ps.setString(3, momoTransId);
            ps.setString(4, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật trạng thái giao dịch khi thanh toán thất bại hoặc bị hủy.
     */
    public boolean markFailure(String orderId, int resultCode, String message) throws SQLException {
        final String sql = "UPDATE MomoPayment SET status = 'FAILED', resultCode = ?, message = ? WHERE order_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, resultCode);
            ps.setString(2, message);
            ps.setString(3, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    private MomoPayment map(ResultSet rs) throws SQLException {
        MomoPayment payment = new MomoPayment();
        payment.setId(rs.getLong("payment_id"));
        payment.setUserId(rs.getLong("user_id"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setPlanCode(rs.getString("plan_code"));
        payment.setOrderId(rs.getString("order_id"));
        payment.setRequestId(rs.getString("request_id"));
        payment.setStatus(rs.getString("status"));
        payment.setPayUrl(rs.getString("payUrl"));
        payment.setDeeplink(rs.getString("deeplink"));
        payment.setResultCode((Integer) rs.getObject("resultCode"));
        payment.setMessage(rs.getString("message"));
        payment.setMomoTransId(rs.getString("momoTransId"));
        payment.setCreatedAt(toLocalDateTime(rs.getTimestamp("createdAt")));
        payment.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updatedAt")));
        return payment;
    }
}
