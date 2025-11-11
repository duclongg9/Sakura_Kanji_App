package app.api.account;

import app.dao.AccountUpgradeRequestDAO;
import app.dao.UserDAO;
import app.model.AccountUpgradeRequest;
import app.model.User;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Endpoint cho phép người dùng gửi yêu cầu nâng cấp tài khoản lên VIP.
 */
@WebServlet(name = "AccountUpgradeServlet", urlPatterns = "/api/account/upgrade-requests")
public class AccountUpgradeServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        Long userId = resolveUserId(req);
        if (userId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().print(new JSONObject()
                    .put("error", "Unauthorized")
                    .put("message", "Missing or invalid token").toString());
            return;
        }

        UserDAO userDAO = new UserDAO();
        AccountUpgradeRequestDAO requestDAO = new AccountUpgradeRequestDAO();
        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().print(new JSONObject()
                        .put("error", "UserNotFound")
                        .put("message", "Không tìm thấy người dùng").toString());
                return;
            }
            if (user.getRoleId() == 1) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().print(new JSONObject()
                        .put("error", "Forbidden")
                        .put("message", "Quản trị viên không cần gửi yêu cầu VIP").toString());
                return;
            }
            if ("VIP".equalsIgnoreCase(user.getAccountTier()) || user.getRoleId() == 3) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().print(new JSONObject()
                        .put("error", "AlreadyVip")
                        .put("message", "Tài khoản đã là VIP").toString());
                return;
            }
            if (requestDAO.hasPendingRequest(userId)) {
                AccountUpgradeRequest pending = requestDAO.findLatestPendingByUser(userId);
                JSONObject payload = toJson(pending);
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().print(payload
                        .put("error", "PendingRequest")
                        .put("message", "Bạn đã gửi yêu cầu và đang chờ duyệt").toString());
                return;
            }

            String note = null;
            String body = readBody(req);
            if (!body.isBlank()) {
                try {
                    JSONObject json = new JSONObject(body);
                    if (!json.isNull("note")) {
                        note = json.optString("note", null);
                    }
                } catch (JSONException ex) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().print(new JSONObject()
                            .put("error", "InvalidPayload")
                            .put("message", "Dữ liệu yêu cầu không hợp lệ").toString());
                    return;
                }
            }

            AccountUpgradeRequest created = requestDAO.create(userId, user.getRoleId(), 3, note);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().print(toJson(created).toString());
        } catch (SQLException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(new JSONObject()
                    .put("error", "DatabaseError")
                    .put("message", "Không thể xử lý yêu cầu: " + ex.getMessage()).toString());
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

    private JSONObject toJson(AccountUpgradeRequest request) {
        JSONObject json = new JSONObject()
                .put("requestId", request.getRequestId())
                .put("userId", request.getUserId())
                .put("currentRoleId", request.getCurrentRoleId())
                .put("targetRoleId", request.getTargetRoleId())
                .put("status", request.getStatus());
        json.put("note", request.getNote() != null ? request.getNote() : JSONObject.NULL);
        json.put("createdAt", request.getCreatedAt() != null ? request.getCreatedAt().toString() : JSONObject.NULL);
        json.put("processedAt", request.getProcessedAt() != null ? request.getProcessedAt().toString() : JSONObject.NULL);
        return json;
    }
}
