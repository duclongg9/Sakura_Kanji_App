package app.api.payments;

import app.api.support.JsonRequestHelper;
import app.dao.MomoPaymentDAO;
import app.dao.UserDAO;
import app.integration.momo.MomoConfig;
import app.integration.momo.MomoSignature;
import app.integration.momo.MomoUpgradePlan;
import app.model.MomoPayment;
import app.model.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.json.JSONObject;

/**
 * IPN nhận callback từ MoMo thông báo kết quả thanh toán.
 */
@WebServlet(name = "MomoCallbackServlet", urlPatterns = "/api/payments/momo/callback")
public class MomoCallbackServlet extends HttpServlet {

    private final MomoConfig config = MomoConfig.load();

    /**
     * Nhận thông báo IPN từ MoMo và cập nhật trạng thái giao dịch.
     *
     * @param req  body JSON do MoMo gửi.
     * @param resp phản hồi JSON báo thành công hoặc lỗi.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String body = readBody(req);
        if (body.isBlank()) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "EmptyBody", "Callback body is empty");
            return;
        }
        JSONObject payload = new JSONObject(body);
        String orderId = payload.optString("orderId", null);
        int resultCode = payload.optInt("resultCode", -1);
        String message = payload.optString("message", "");
        String signature = payload.optString("signature", null);
        String requestId = payload.optString("requestId", null);
        String partnerCode = payload.optString("partnerCode", null);
        String amount = payload.optString("amount", "0");
        String extraData = payload.optString("extraData", "");
        String orderInfo = payload.optString("orderInfo", "");
        String orderType = payload.optString("orderType", "momo_wallet");
        String payType = payload.optString("payType", "");
        String responseTime = payload.optString("responseTime", "");
        String transId = payload.optString("transId", "");

        if (orderId == null) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "MissingOrderId", "orderId missing in callback");
            return;
        }

        try {
            MomoPaymentDAO dao = new MomoPaymentDAO();
            MomoPayment payment = dao.findByOrderId(orderId);
            if (payment == null) {
                JsonRequestHelper.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "PaymentNotFound", "Order not found");
                return;
            }

            if (!config.isStubMode()) {
                String rawSignature = "accessKey=" + config.getAccessKey()
                        + "&amount=" + amount
                        + "&extraData=" + extraData
                        + "&message=" + message
                        + "&orderId=" + orderId
                        + "&orderInfo=" + orderInfo
                        + "&orderType=" + orderType
                        + "&partnerCode=" + partnerCode
                        + "&payType=" + payType
                        + "&requestId=" + requestId
                        + "&responseTime=" + responseTime
                        + "&resultCode=" + resultCode
                        + "&transId=" + transId;
                String calculated = MomoSignature.sign(rawSignature, config.getSecretKey());
                if (signature == null || !signature.equals(calculated)) {
                    JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "SignatureMismatch", "Invalid signature");
                    return;
                }
            }

            if (resultCode == 0) {
                boolean updated = dao.markSuccess(orderId, resultCode, message, transId);
                if (updated) {
                    upgradeUserTier(payment);
                }
            } else {
                dao.markFailure(orderId, resultCode, message);
            }

            JSONObject response = new JSONObject()
                    .put("resultCode", 0)
                    .put("message", "success");
            resp.getWriter().print(response.toString());
        } catch (SQLException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DatabaseError", "Cannot update payment status");
        }
    }

    /**
     * Gia hạn thời gian VIP cho người dùng dựa trên gói đã thanh toán.
     */
    private void upgradeUserTier(MomoPayment payment) throws SQLException {
        MomoUpgradePlan plan = MomoUpgradePlan.fromCode(payment.getPlanCode());
        UserDAO userDAO = new UserDAO();
        User user = userDAO.findById(payment.getUserId());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime base = now;
        if (user != null && user.getVipExpiresAt() != null && user.getVipExpiresAt().isAfter(now)) {
            base = user.getVipExpiresAt();
        }
        LocalDateTime newExpiry = base.plus(plan.getDuration());
        userDAO.upgradeToVip(payment.getUserId(), newExpiry);
    }

    /**
     * Đọc toàn bộ request body dưới dạng chuỗi.
     */
    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
