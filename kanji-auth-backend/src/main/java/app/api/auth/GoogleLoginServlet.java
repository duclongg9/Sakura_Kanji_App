package app.api.auth;

import app.dao.UserDAO;
import app.model.User;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 * Đăng nhập bằng Google OAuth, lưu user vào DB và trả token demo.
 */
@WebServlet(name = "GoogleLoginServlet", urlPatterns = "/api/auth/google")
public class GoogleLoginServlet extends HttpServlet {

    private static final String DEFAULT_WEB_CLIENT_ID =
            "748643708301-n2167jrvf5akg0pt79ilai54mslhgqaf.apps.googleusercontent.com";

    /**
     * Đọc Google OAuth client ID từ system property hoặc biến môi trường.
     * <p>
     * Khi triển khai thực tế nên truyền {@code -DKANJI_APP_GOOGLE_CLIENT=<client-id>} cho JVM hoặc
     * cấu hình biến môi trường cùng tên. Nếu không tồn tại, ứng dụng sẽ dùng giá trị mặc định dành
     * cho môi trường phát triển (trùng với chuỗi trong strings.xml của Android app).
     */
    private static final String WEB_CLIENT_ID = resolveWebClientId();

    private static String resolveWebClientId() {
        String clientId = System.getProperty("KANJI_APP_GOOGLE_CLIENT");
        if (clientId == null || clientId.isBlank()) {
            clientId = System.getenv("KANJI_APP_GOOGLE_CLIENT");
        }
        if (clientId == null || clientId.isBlank()) {
            clientId = DEFAULT_WEB_CLIENT_ID;
        }
        return clientId;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            String body = readBody(req);
            String token = new JSONObject(body).optString("idToken", null);
            if (token == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print(new JSONObject().put("error", "Missing token").toString());
                return;
            }

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(WEB_CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(token);
            if (idToken == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print(new JSONObject().put("error", "Invalid token").toString());
                return;
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            UserDAO userDAO = new UserDAO();
            User user = userDAO.ensureOAuthUser(email, name, picture);

            String fakeToken = "demo-" + user.getId();
            JSONObject response = new JSONObject()
                    .put("token", fakeToken)
                    .put("roleId", user.getRoleId())
                    .put("userName", user.getUserName())
                    .put("userId", user.getId());
            resp.getWriter().print(response.toString());
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(new JSONObject().put("error", "Server error").toString());
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
