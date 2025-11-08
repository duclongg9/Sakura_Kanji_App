/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package app;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 *
 * @author Dell-PC
 */
@WebServlet(name="AuthLoginServlet", urlPatterns={"/api/auth/login"})
public class AuthLoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        try (BufferedReader br = req.getReader(); PrintWriter out = resp.getWriter()) {
            String body = br.lines().reduce("", (a,b) -> a + b);
            JSONObject json = new JSONObject(body);
            String emailOrUser = json.optString("email", "");
            String password    = json.optString("password", "");

            if (emailOrUser.isBlank() || password.isBlank()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(new JSONObject().put("error","Missing email/password"));
                return;
            }

            UserDAO dao = new UserDAO();
            User u = dao.login(emailOrUser, password);

            if (u == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print(new JSONObject().put("error","Invalid credentials"));
                return;
            }

            // (tuỳ chọn) sinh JWT. Tạm thời trả token giả cho app lưu
            String fakeToken = "demo-" + u.getId();

            JSONObject ok = new JSONObject()
                    .put("token", fakeToken)
                    .put("roleId", u.getRoleId())
                    .put("userName", u.getUserName());

            out.print(ok.toString());
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(new JSONObject().put("error","Server error").toString());
            ex.printStackTrace();
        }
    }
}
