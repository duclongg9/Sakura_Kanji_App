package app.api.account;

import app.storage.UpgradeReceiptStorage;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Cung cấp ảnh chứng từ chuyển khoản đã lưu cho yêu cầu nâng cấp tài khoản.
 */
@WebServlet(name = "AccountUpgradeReceiptServlet", urlPatterns = "/uploads/upgrade-receipts/*")
public class AccountUpgradeReceiptServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String fileName = sanitizeFileName(pathInfo.substring(1));
        if (fileName == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file name");
            return;
        }

        try {
            Path file = UpgradeReceiptStorage.resolve(fileName);
            if (!Files.exists(file) || !Files.isRegularFile(file)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            String contentType = Files.probeContentType(file);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            resp.setContentType(contentType);
            resp.setHeader("Cache-Control", "public, max-age=604800");
            resp.setContentLengthLong(Files.size(file));
            try (InputStream inputStream = Files.newInputStream(file);
                 OutputStream outputStream = resp.getOutputStream()) {
                inputStream.transferTo(outputStream);
            }
        } catch (IOException ex) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot read receipt image");
        }
    }

    private String sanitizeFileName(String raw) {
        if (raw.contains("/") || raw.contains("\\") || raw.contains("..")) {
            return null;
        }
        return raw;
    }
}
