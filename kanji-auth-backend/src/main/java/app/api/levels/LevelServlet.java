package app.api.levels;

import app.dao.LevelDAO;
import app.model.Level;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * API quản lý level theo JLPT.
 */
@WebServlet(name = "LevelServlet", urlPatterns = "/api/levels")
public class LevelServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String jlptIdParam = req.getParameter("jlptId");
        if (jlptIdParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(new JSONObject().put("error", "Missing jlptId").toString());
            return;
        }
        try {
            int jlptId = Integer.parseInt(jlptIdParam);
            List<Level> levels = new LevelDAO().findByJlpt(jlptId);
            JSONArray array = new JSONArray();
            for (Level level : levels) {
                array.put(toJson(level));
            }
            resp.getWriter().print(array.toString());
        } catch (NumberFormatException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(new JSONObject().put("error", "Invalid jlptId").toString());
        } catch (SQLException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(new JSONObject().put("error", "Database error").toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try (BufferedReader reader = req.getReader(); PrintWriter out = resp.getWriter()) {
            String body = reader.lines().reduce("", (acc, line) -> acc + line);
            JSONObject payload = new JSONObject(body);
            String name = payload.optString("name");
            int jlptId = payload.optInt("jlptLevelId", -1);
            String description = payload.optString("description", null);
            String accessTier = payload.optString("accessTier", "FREE");
            boolean isActive = payload.optBoolean("active", true);

            if (name.isBlank() || jlptId <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(new JSONObject().put("error", "Invalid payload"));
                return;
            }

            Level level = new Level();
            level.setName(name);
            level.setJlptLevelId(jlptId);
            level.setDescription(description);
            level.setAccessTier(accessTier);
            level.setActive(isActive);

            LevelDAO dao = new LevelDAO();
            dao.insert(level);
            out.print(toJson(level).toString());
        } catch (SQLException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(new JSONObject().put("error", "Database error").toString());
        }
    }

    private JSONObject toJson(Level level) {
        return new JSONObject()
                .put("id", level.getId())
                .put("name", level.getName())
                .put("jlptLevelId", level.getJlptLevelId())
                .put("description", level.getDescription())
                .put("accessTier", level.getAccessTier())
                .put("active", level.isActive());
    }
}
