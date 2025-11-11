package app.api.admin;

import app.api.support.JsonRequestHelper;
import app.dao.AccountUpgradeRequestDAO;
import app.dao.UserDAO;
import app.model.AccountUpgradeRequest;
import app.model.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import org.json.JSONObject;

/**
 * API cho phép quản trị viên cập nhật mã giao dịch cho yêu cầu nâng cấp VIP.
 */
@WebServlet(name = "AdminAccountUpgradeTransactionServlet",
        urlPatterns = "/api/admin/upgrade-requests/transaction-code")
public class AdminAccountUpgradeTransactionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        Long adminId = resolveUserId(req);
        if (adminId == null) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Missing or invalid token");
            return;
        }

        UserDAO userDAO = new UserDAO();
        try {
            User admin = userDAO.findById(adminId);
            if (admin == null || admin.getRoleId() != 1) {
                JsonRequestHelper.writeError(resp, HttpServletResponse.SC_FORBIDDEN, "Forbidden", "Chỉ quản trị viên mới được cập nhật mã giao dịch");
                return;
            }

            JSONObject payload;
            try {
                payload = JsonRequestHelper.readJsonObject(req);
            } catch (IllegalArgumentException ex) {
                JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "InvalidPayload", ex.getMessage());
                return;
            }

            long requestId = payload.optLong("requestId", -1);
            if (requestId <= 0) {
                JSONObject details = new JSONObject().put("requestId", "RequestId must be a positive number");
                JsonRequestHelper.writeValidationErrors(resp, details);
                return;
            }
            String transactionCode = payload.optString("transactionCode", null);
            if (transactionCode != null) {
                transactionCode = transactionCode.trim();
                if (transactionCode.isEmpty()) {
                    transactionCode = null;
                }
            }

            AccountUpgradeRequestDAO requestDAO = new AccountUpgradeRequestDAO();
            AccountUpgradeRequest request = requestDAO.findById(requestId);
            if (request == null) {
                JsonRequestHelper.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "NotFound", "Không tìm thấy yêu cầu nâng cấp");
                return;
            }
            if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
                JsonRequestHelper.writeError(resp, HttpServletResponse.SC_CONFLICT, "InvalidState", "Yêu cầu đã được xử lý, không thể chỉnh sửa mã giao dịch");
                return;
            }

            AccountUpgradeRequest updated = requestDAO.updateTransactionCode(requestId, transactionCode);
            if (updated == null) {
                JsonRequestHelper.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "NotFound", "Không tìm thấy yêu cầu nâng cấp");
                return;
            }

            resp.getWriter().print(toJson(req, updated).toString());
        } catch (SQLException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DatabaseError", "Không thể cập nhật mã giao dịch: " + ex.getMessage());
        }
    }

    private Long resolveUserId(HttpServletRequest req) {
        String authorization = req.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer demo-")) {
            return null;
        }
        try {
            return Long.parseLong(authorization.substring("Bearer demo-".length()));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private JSONObject toJson(HttpServletRequest req, AccountUpgradeRequest request) {
        JSONObject json = new JSONObject()
                .put("requestId", request.getRequestId())
                .put("userId", request.getUserId())
                .put("status", request.getStatus());
        json.put("note", request.getNote() != null ? request.getNote() : JSONObject.NULL);
        json.put("createdAt", request.getCreatedAt() != null ? request.getCreatedAt().toString() : JSONObject.NULL);
        json.put("processedAt", request.getProcessedAt() != null ? request.getProcessedAt().toString() : JSONObject.NULL);
        if (request.getReceiptImagePath() != null) {
            json.put("receiptImageUrl", req.getContextPath() + "/uploads/upgrade-receipts/" + request.getReceiptImagePath());
        } else {
            json.put("receiptImageUrl", JSONObject.NULL);
        }
        if (request.getTransactionCode() != null) {
            json.put("transactionCode", request.getTransactionCode());
        } else {
            json.put("transactionCode", JSONObject.NULL);
        }
        return json;
    }
}
