package app.api.payments;

import app.api.support.JsonRequestHelper;
import app.dao.MomoPaymentDAO;
import app.dao.UserDAO;
import app.integration.momo.MomoClient;
import app.integration.momo.MomoConfig;
import app.integration.momo.MomoCreatePaymentResult;
import app.integration.momo.MomoUpgradePlan;
import app.model.MomoPayment;
import app.model.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import org.json.JSONObject;

/**
 * API tạo giao dịch thanh toán MoMo và truy vấn trạng thái.
 */
@WebServlet(name = "MomoPaymentServlet", urlPatterns = "/api/payments/momo")
public class MomoPaymentServlet extends HttpServlet {

    private final MomoConfig config = MomoConfig.load();

    /**
     * Khởi tạo giao dịch thanh toán MoMo mới cho người dùng hiện tại.
     *
     * @param req  payload JSON chứa <code>planCode</code>.
     * @param resp phản hồi JSON với URL thanh toán và thông tin đơn hàng.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        Long userId = resolveUserId(req);
        if (userId == null) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Missing or invalid token");
            return;
        }

        JSONObject payload;
        try {
            payload = JsonRequestHelper.readJsonObject(req);
        } catch (IllegalArgumentException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "InvalidRequest", ex.getMessage());
            return;
        }

        MomoUpgradePlan plan;
        try {
            plan = MomoUpgradePlan.fromCode(payload.optString("planCode", null));
        } catch (IllegalArgumentException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "UnsupportedPlan", ex.getMessage());
            return;
        }

        String orderId = "VIP-" + userId + "-" + System.currentTimeMillis();
        JSONObject extraData = new JSONObject()
                .put("userId", userId)
                .put("planCode", plan.getCode());

        MomoClient momoClient = new MomoClient(config);
        try {
            MomoCreatePaymentResult result = momoClient.createPayment(plan, orderId, plan.getDescription(), extraData.toString());
            MomoPaymentDAO dao = new MomoPaymentDAO();
            MomoPayment payment = dao.createPending(userId, plan.getAmount(), plan.getCode(), orderId, result.getRequestId(),
                    result.getPayUrl(), result.getDeeplink());
            JSONObject json = new JSONObject()
                    .put("orderId", payment.getOrderId())
                    .put("requestId", payment.getRequestId())
                    .put("planCode", plan.getCode())
                    .put("amount", plan.getAmount())
                    .put("payUrl", payment.getPayUrl())
                    .put("deeplink", payment.getDeeplink())
                    .put("status", payment.getStatus())
                    .put("stubMode", config.isStubMode());
            resp.getWriter().print(json.toString());
        } catch (SQLException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DatabaseError", "Cannot persist payment");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Interrupted", "Payment creation interrupted");
        } catch (IOException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_GATEWAY, "MomoError", ex.getMessage());
        }
    }

    /**
     * Tra cứu trạng thái giao dịch MoMo dựa trên <code>orderId</code>.
     *
     * @param req  request query chứa orderId.
     * @param resp phản hồi JSON mô tả trạng thái.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        Long userId = resolveUserId(req);
        if (userId == null) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Missing or invalid token");
            return;
        }
        String orderId = req.getParameter("orderId");
        if (orderId == null || orderId.isBlank()) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "MissingOrderId", "orderId is required");
            return;
        }

        try {
            MomoPaymentDAO dao = new MomoPaymentDAO();
            MomoPayment payment = dao.findByOrderId(orderId);
            if (payment == null || payment.getUserId() != userId) {
                JsonRequestHelper.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "PaymentNotFound", "Payment not found");
                return;
            }
            JSONObject json = new JSONObject()
                    .put("orderId", payment.getOrderId())
                    .put("planCode", payment.getPlanCode())
                    .put("status", payment.getStatus())
                    .put("amount", payment.getAmount())
                    .put("payUrl", payment.getPayUrl())
                    .put("deeplink", payment.getDeeplink())
                    .put("resultCode", payment.getResultCode() != null ? payment.getResultCode() : JSONObject.NULL)
                    .put("message", payment.getMessage() != null ? payment.getMessage() : JSONObject.NULL);

            if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {
                User user = new UserDAO().findById(userId);
                if (user != null) {
                    json.put("vipActivated", true);
                    if (user.getVipExpiresAt() != null) {
                        json.put("vipExpiresAt", user.getVipExpiresAt().toString());
                    } else {
                        json.put("vipExpiresAt", JSONObject.NULL);
                    }
                }
            }
            resp.getWriter().print(json.toString());
        } catch (SQLException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DatabaseError", "Cannot load payment status");
        }
    }

    /**
     * Trích xuất user id từ header Authorization trong chế độ demo.
     */
    private Long resolveUserId(HttpServletRequest req) {
        String authorization = req.getHeader("Authorization");
        if (authorization == null || !authorization.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
            return null;
        }
        String token = authorization.substring("Bearer ".length());
        if (!token.startsWith("demo-")) {
            return null;
        }
        try {
            return Long.parseLong(token.substring("demo-".length()));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
