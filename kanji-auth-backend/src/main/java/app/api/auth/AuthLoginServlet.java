package app.api.auth;

import app.dao.AccountUpgradeRequestDAO;
import app.dao.UserDAO;
import app.model.AccountUpgradeRequest;
import app.model.User;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 * Xử lý đăng nhập tài khoản cục bộ.
 */
@WebServlet(name = "AuthLoginServlet", urlPatterns = "/api/auth/login")
public class AuthLoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try (BufferedReader reader = req.getReader(); PrintWriter out = resp.getWriter()) {
            String body = reader.lines().reduce("", (acc, line) -> acc + line);
            JSONObject payload = new JSONObject(body);
            String identifier = payload.optString("email", "");
            String password = payload.optString("password", "");

            if (identifier.isBlank() || password.isBlank()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(new JSONObject().put("error", "Missing email/password"));
                return;
            }

            UserDAO userDAO = new UserDAO();
            User user = userDAO.login(identifier, password);
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print(new JSONObject().put("error", "Invalid credentials"));
                return;
            }

            String fakeToken = "demo-" + user.getId();
            AccountUpgradeRequestDAO requestDAO = new AccountUpgradeRequestDAO();
            AccountUpgradeRequest pending = requestDAO.findLatestPendingByUser(user.getId());
            JSONObject responseJson = new JSONObject()
                    .put("token", fakeToken)
                    .put("roleId", user.getRoleId())
                    .put("userName", user.getUserName())
                    .put("email", user.getEmail())
                    .put("userId", user.getId())
                    .put("imgUrl", user.getImgUrl())
                    .put("accountTier", user.getAccountTier())
                    .put("accountBalance", user.getAccountBalance() != null ? user.getAccountBalance() : 0)
                    .put("vipExpiresAt", user.getVipExpiresAt() != null ? user.getVipExpiresAt().toString() : JSONObject.NULL)
                    .put("bio", user.getBio())
                    .put("hasPendingUpgradeRequest", pending != null);
            if (pending != null) {
                responseJson.put("pendingUpgradeRequestId", pending.getRequestId());
                responseJson.put("pendingUpgradeCreatedAt", pending.getCreatedAt() != null ? pending.getCreatedAt().toString() : JSONObject.NULL);
                responseJson.put("pendingUpgradeNote", pending.getNote() != null ? pending.getNote() : JSONObject.NULL);
            }
            out.print(responseJson.toString());
        } catch (SQLException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(new JSONObject().put("error", "Database error").toString());
        }
    }
}
