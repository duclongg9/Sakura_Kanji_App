package app.api.account;

import app.dao.AccountUpgradeRequestDAO;
import app.dao.UserDAO;
import app.model.AccountUpgradeRequest;
import app.model.User;
import app.storage.UpgradeReceiptStorage;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Endpoint cho phép người dùng gửi yêu cầu nâng cấp tài khoản lên VIP kèm ảnh chứng từ chuyển khoản.
 */
@WebServlet(name = "AccountUpgradeServlet", urlPatterns = "/api/account/upgrade-requests")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024)
public class AccountUpgradeServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
                JSONObject payload = toJson(req, pending);
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().print(payload
                        .put("error", "PendingRequest")
                        .put("message", "Bạn đã gửi yêu cầu và đang chờ duyệt").toString());
                return;
            }

            String note = null;
            String receiptFile = null;
            if (isMultipart(req)) {
                note = readPartValue(req.getPart("note"));
                Part receiptPart = req.getPart("receiptImage");
                if (receiptPart != null && receiptPart.getSize() > 0) {
                    try {
                        receiptFile = UpgradeReceiptStorage.store(receiptPart);
                    } catch (IllegalArgumentException ex) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().print(new JSONObject()
                                .put("error", "InvalidReceipt")
                                .put("message", ex.getMessage()).toString());
                        return;
                    } catch (IOException ex) {
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        resp.getWriter().print(new JSONObject()
                                .put("error", "ReceiptUploadFailed")
                                .put("message", "Không thể lưu ảnh chứng từ: " + ex.getMessage()).toString());
                        return;
                    }
                }
            } else {
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
            }

            AccountUpgradeRequest created = requestDAO.create(userId, user.getRoleId(), 3, note, receiptFile);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().print(toJson(req, created).toString());
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

    /**
     * Chuyển đối tượng yêu cầu nâng cấp thành JSON, bao gồm đường dẫn ảnh chứng từ.
     *
     * @param req     request hiện tại để xác định context path.
     * @param request yêu cầu nâng cấp cần chuyển đổi.
     * @return đối tượng JSON tương ứng.
     */
    private JSONObject toJson(HttpServletRequest req, AccountUpgradeRequest request) {
        JSONObject json = new JSONObject()
                .put("requestId", request.getRequestId())
                .put("userId", request.getUserId())
                .put("currentRoleId", request.getCurrentRoleId())
                .put("targetRoleId", request.getTargetRoleId())
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

    private boolean isMultipart(HttpServletRequest req) {
        String contentType = req.getContentType();
        return contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/");
    }

    private String readPartValue(Part part) throws IOException {
        if (part == null) {
            return null;
        }
        try (BufferedReader reader = part.getReader()) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String value = sb.toString().trim();
            return value.isEmpty() ? null : value;
        }
    }
}
