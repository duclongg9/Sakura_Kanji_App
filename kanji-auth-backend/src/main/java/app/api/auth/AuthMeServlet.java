package app.api.auth;

import app.dao.AccountUpgradeRequestDAO;
import app.dao.UserDAO;
import app.model.AccountUpgradeRequest;
import app.model.User;
import java.io.IOException;
import java.sql.SQLException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 * Trả thông tin người dùng từ token demo-<id>.
 */
@WebServlet(name = "AuthMeServlet", urlPatterns = "/api/auth/me")
public class AuthMeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String authorization = req.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer demo-")) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().print(new JSONObject().put("error", "Missing token").toString());
            return;
        }

        String idPart = authorization.substring("Bearer demo-".length());
        long userId;
        try {
            userId = Long.parseLong(idPart);
        } catch (NumberFormatException ex) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().print(new JSONObject().put("error", "Invalid token").toString());
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
            AccountUpgradeRequest pending = requestDAO.findLatestPendingByUser(userId);
            JSONObject json = new JSONObject()
                    .put("id", user.getId())
                    .put("userName", user.getUserName())
                    .put("email", user.getEmail())
                    .put("roleId", user.getRoleId())
                    .put("imgUrl", user.getImgUrl())
                    .put("accountTier", user.getAccountTier())
                    .put("accountBalance", user.getAccountBalance() != null ? user.getAccountBalance() : 0)
                    .put("vipExpiresAt", user.getVipExpiresAt() != null ? user.getVipExpiresAt().toString() : JSONObject.NULL)
                    .put("bio", user.getBio())
                    .put("hasPendingUpgradeRequest", pending != null);
            if (pending != null) {
                json.put("pendingUpgradeRequestId", pending.getRequestId());
                json.put("pendingUpgradeCreatedAt", pending.getCreatedAt() != null ? pending.getCreatedAt().toString() : JSONObject.NULL);
                json.put("pendingUpgradeNote", pending.getNote() != null ? pending.getNote() : JSONObject.NULL);
            }
            resp.getWriter().print(json.toString());
        } catch (SQLException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(new JSONObject().put("error", "Database error").toString());
        }
    }
}
