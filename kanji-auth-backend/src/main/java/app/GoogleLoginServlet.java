/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package app;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import org.json.JSONObject;

/**
 *
 * @author Dell-PC
 */
@WebServlet(urlPatterns = "/api/auth/google")
public class GoogleLoginServlet extends HttpServlet {

  private static final String WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com";

  private static String readBody(HttpServletRequest req) throws IOException {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader br = req.getReader()) {
      String line; while ((line = br.readLine()) != null) sb.append(line);
    }
    return sb.toString();
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("application/json; charset=UTF-8");
    try {
      String idTokenStr = new JSONObject(readBody(req)).optString("idToken", null);
      if (idTokenStr == null) { resp.setStatus(400); resp.getWriter().write("{\"error\":\"Missing token\"}"); return; }

      GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
              .setAudience(Collections.singletonList(WEB_CLIENT_ID))
              .build();

      GoogleIdToken idToken = verifier.verify(idTokenStr);
      if (idToken == null) { resp.setStatus(401); resp.getWriter().write("{\"error\":\"Invalid token\"}"); return; }

      GoogleIdToken.Payload p = idToken.getPayload();
      String email = p.getEmail();                 // đã verify
      String name  = (String) p.get("name");
      String pic   = (String) p.get("picture");

      // Lưu/tìm User trong MySQL
      int roleId = 2; // mặc định USER
      Long userId;

      try (Connection c = DB.get()) {
        // tìm sẵn
        try (PreparedStatement ps = c.prepareStatement("SELECT id, roleId FROM `User` WHERE email=? LIMIT 1")) {
          ps.setString(1, email);
          try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
              userId = rs.getLong("id");
              roleId = rs.getInt("roleId");
            } else {
              // tạo mới
              try (PreparedStatement ins = c.prepareStatement(
                  "INSERT INTO `User`(userName,email,imgUrl,roleId) VALUES(?,?,?,?)",
                  Statement.RETURN_GENERATED_KEYS)) {
                ins.setString(1, name != null ? name : email);
                ins.setString(2, email);
                ins.setString(3, pic);
                ins.setInt(4, roleId);
                ins.executeUpdate();
                try (ResultSet k = ins.getGeneratedKeys()) { k.next(); userId = k.getLong(1); }
              }
            }
          }
        }
      }

      // Trả “JWT giả” để app lưu (sau bạn tự ký JWT thật nếu muốn)
      String token = "dummy.jwt.for.user." + email;
      resp.getWriter().write(new JSONObject().put("token", token).put("roleId", roleId).toString());

    } catch (Exception e) {
      e.printStackTrace();
      resp.setStatus(500);
      resp.getWriter().write("{\"error\":\"Server error\"}");
    }
  }
}
