package app.api.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Locale;
import org.json.JSONObject;

/**
 * Tiện ích dùng chung cho các servlet xử lý JSON giúp chuẩn hóa việc đọc body và trả lỗi.
 */
public final class JsonRequestHelper {

    private JsonRequestHelper() {
    }

    /**
     * Đọc toàn bộ body request và parse thành {@link JSONObject}. Phương thức đảm bảo request có
     * định dạng JSON hợp lệ và không rỗng.
     *
     * @param request đối tượng {@link HttpServletRequest} hiện tại.
     * @return {@link JSONObject} đại diện cho payload.
     * @throws IOException              khi gặp lỗi I/O trong quá trình đọc.
     * @throws IllegalArgumentException nếu body trống, sai định dạng hoặc Content-Type không phải JSON.
     */
    public static JSONObject readJsonObject(HttpServletRequest request) throws IOException {
        String contentType = request.getContentType();
        if (contentType != null && !contentType.toLowerCase(Locale.ROOT).contains("application/json")) {
            throw new IllegalArgumentException("Content-Type must be application/json");
        }
        request.setCharacterEncoding("UTF-8");
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        if (builder.length() == 0) {
            throw new IllegalArgumentException("Request body is empty");
        }
        try {
            return new JSONObject(builder.toString());
        } catch (org.json.JSONException ex) {
            throw new IllegalArgumentException("Invalid JSON payload", ex);
        }
    }

    /**
     * Gửi phản hồi lỗi JSON với cấu trúc bao gồm mã lỗi và thông điệp thân thiện cho client.
     *
     * @param response đối tượng response.
     * @param status   mã trạng thái HTTP cần trả về.
     * @param code     mã lỗi nội bộ (ví dụ ValidationError, NotFound,...).
     * @param message  thông điệp mô tả lỗi.
     * @throws IOException nếu không thể ghi dữ liệu.
     */
    public static void writeError(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        writeError(response, status, code, message, null);
    }

    /**
     * Gửi phản hồi lỗi JSON với cấu trúc bao gồm mã lỗi, thông điệp và chi tiết bổ sung.
     *
     * @param response đối tượng response.
     * @param status   mã trạng thái HTTP cần trả về.
     * @param code     mã lỗi nội bộ (ví dụ ValidationError, NotFound,...).
     * @param message  thông điệp mô tả lỗi.
     * @param details  thông tin chi tiết thêm về lỗi (có thể null).
     * @throws IOException nếu không thể ghi dữ liệu.
     */
    public static void writeError(HttpServletResponse response, int status, String code, String message, JSONObject details)
            throws IOException {
        response.setStatus(status);
        JSONObject payload = new JSONObject()
                .put("error", code)
                .put("message", message);
        if (details != null && details.length() > 0) {
            payload.put("details", details);
        }
        response.getWriter().print(payload.toString());
    }

    /**
     * Tiện ích gói cho các lỗi xác thực, mặc định trả về mã 400 cùng chi tiết từng field.
     *
     * @param response response HTTP.
     * @param details  danh sách lỗi chi tiết theo từng field.
     * @throws IOException nếu ghi dữ liệu thất bại.
     */
    public static void writeValidationErrors(HttpServletResponse response, JSONObject details) throws IOException {
        writeError(response, HttpServletResponse.SC_BAD_REQUEST, "ValidationError", "Payload validation failed", details);
    }
}
