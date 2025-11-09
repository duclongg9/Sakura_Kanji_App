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
            resp.getWriter().print(new JSONObject().put("error", "Missing token").toString());
            return;
        }

        try {
            UserDAO userDAO = new UserDAO();
            User user = userDAO.findById(userId);
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().print(new JSONObject().put("error", "User not found").toString());
                return;
            }

            AccountUpgradeRequestDAO requestDAO = new AccountUpgradeRequestDAO();
            if (requestDAO.hasPendingRequest(userId)) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().print(new JSONObject().put("error", "Request already pending").toString());
                return;
            }

            String body = readBody(req);
            JSONObject payload = new JSONObject(body.isEmpty() ? "{}" : body);
            String note = payload.optString("note", null);
            int targetRole = payload.optInt("targetRoleId", 3);
            if (targetRole != 3) {
                targetRole = 3; // hiện chỉ hỗ trợ nâng cấp lên VIP
            }

            AccountUpgradeRequest request = requestDAO.create(userId, user.getRoleId(), targetRole, note);

            JSONObject json = new JSONObject()
                    .put("requestId", request.getRequestId())
                    .put("status", request.getStatus())
                    .put("note", request.getNote())
                    .put("targetRoleId", request.getTargetRoleId())
                    .put("createdAt", request.getCreatedAt() != null ? request.getCreatedAt().toString() : JSONObject.NULL);
            resp.getWriter().print(json.toString());
        } catch (SQLException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(new JSONObject().put("error", "Database error").toString());
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
}
