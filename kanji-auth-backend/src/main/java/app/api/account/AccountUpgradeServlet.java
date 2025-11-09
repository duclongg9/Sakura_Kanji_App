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
        JSONObject json = new JSONObject()
                .put("error", "Deprecated")
                .put("message", "Vui lòng sử dụng thanh toán MoMo tại /api/payments/momo để nâng cấp VIP");
        resp.setStatus(HttpServletResponse.SC_GONE);
        resp.getWriter().print(json.toString());
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
