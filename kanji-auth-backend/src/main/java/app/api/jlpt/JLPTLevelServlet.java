package app.api.jlpt;

import app.dao.JLPTLevelDAO;
import app.model.JLPTLevel;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * API trả danh sách cấp độ JLPT.
 */
@WebServlet(name = "JLPTLevelServlet", urlPatterns = "/api/jlpt-levels")
public class JLPTLevelServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            List<JLPTLevel> levels = new JLPTLevelDAO().findAll();
            JSONArray array = new JSONArray();
            for (JLPTLevel level : levels) {
                array.put(new JSONObject()
                        .put("id", level.getId())
                        .put("nameLevel", level.getNameLevel()));
            }
            resp.getWriter().print(array.toString());
        } catch (SQLException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(new JSONObject().put("error", "Database error").toString());
        }
    }
}
