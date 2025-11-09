package app.api.wallet;

import app.dao.UserDAO;
import app.dao.WalletDepositDAO;
import app.model.User;
import app.model.WalletDeposit;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 * Endpoint tạo giao dịch nạp tiền và trả về QR code.
 */
@WebServlet(name = "WalletDepositServlet", urlPatterns = "/api/wallet/deposits")
public class WalletDepositServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        Long userId = resolveUserId(req);
        if (userId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().print(new JSONObject().put("error", "Missing token").toString());
            return;
        }

        String body = readBody(req);
        JSONObject payload = new JSONObject(body.isEmpty() ? "{}" : body);
        double amountDouble = payload.optDouble("amount", 0);
        if (amountDouble < 20000) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(new JSONObject().put("error", "Amount must be at least 20000").toString());
            return;
        }

        BigDecimal amount = BigDecimal.valueOf(amountDouble).setScale(0, RoundingMode.HALF_UP);

        try {
            UserDAO userDAO = new UserDAO();
            User user = userDAO.findById(userId);
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().print(new JSONObject().put("error", "User not found").toString());
                return;
            }

            String qrData = String.format("KANJIAPP|UID=%d|AMOUNT=%s", userId, amount.toPlainString());
            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=256x256&data="
                    + URLEncoder.encode(qrData, StandardCharsets.UTF_8);

            WalletDepositDAO depositDAO = new WalletDepositDAO();
            WalletDeposit deposit = depositDAO.create(userId, amount, qrUrl);

            JSONObject json = new JSONObject()
                    .put("depositId", deposit.getDepositId())
                    .put("amount", deposit.getAmount())
                    .put("status", deposit.getStatus())
                    .put("qrCodeUrl", deposit.getQrCodeUrl())
                    .put("createdAt", deposit.getCreatedAt() != null ? deposit.getCreatedAt().toString() : JSONObject.NULL);
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
