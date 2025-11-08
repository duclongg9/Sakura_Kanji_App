package app.api.lessons;

import app.dao.LessonDAO;
import app.model.Lesson;
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
 * API quản lý lesson trong level.
 */
@WebServlet(name = "LessonServlet", urlPatterns = "/api/lessons")
public class LessonServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String levelIdParam = req.getParameter("levelId");
        if (levelIdParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(new JSONObject().put("error", "Missing levelId").toString());
            return;
        }
        try {
            int levelId = Integer.parseInt(levelIdParam);
            List<Lesson> lessons = new LessonDAO().findByLevel(levelId);
            JSONArray array = new JSONArray();
            for (Lesson lesson : lessons) {
                array.put(toJson(lesson));
            }
            resp.getWriter().print(array.toString());
        } catch (NumberFormatException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(new JSONObject().put("error", "Invalid levelId").toString());
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
            int levelId = payload.optInt("levelId", -1);
            String title = payload.optString("title");
            String overview = payload.optString("overview", null);
            int orderIndex = payload.optInt("orderIndex", 0);

            if (levelId <= 0 || title.isBlank()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(new JSONObject().put("error", "Invalid payload"));
                return;
            }

            Lesson lesson = new Lesson();
            lesson.setLevelId(levelId);
            lesson.setTitle(title);
            lesson.setOverview(overview);
            lesson.setOrderIndex(orderIndex);

            LessonDAO dao = new LessonDAO();
            dao.insert(lesson);
            out.print(toJson(lesson).toString());
        } catch (SQLException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(new JSONObject().put("error", "Database error").toString());
        }
    }

    private JSONObject toJson(Lesson lesson) {
        return new JSONObject()
                .put("lessonId", lesson.getId())
                .put("levelId", lesson.getLevelId())
                .put("title", lesson.getTitle())
                .put("overview", lesson.getOverview())
                .put("orderIndex", lesson.getOrderIndex());
    }
}
