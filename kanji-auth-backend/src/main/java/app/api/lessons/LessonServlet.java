package app.api.lessons;

import app.api.support.JsonRequestHelper;
import app.api.support.JsonValidationUtils;
import app.dao.LessonDAO;
import app.model.Lesson;
import java.io.IOException;
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
        String idParam = req.getParameter("id");
        String lessonIdParam = req.getParameter("lessonId");
        String levelIdParam = req.getParameter("levelId");
        LessonDAO dao = new LessonDAO();
        try {
            String singleId = idParam != null ? idParam : lessonIdParam;
            if (singleId != null) {
                long id = Long.parseLong(singleId);
                Lesson lesson = dao.findById(id);
                if (lesson == null) {
                    JsonRequestHelper.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "LessonNotFound", "Lesson not found");
                } else {
                    resp.getWriter().print(toJson(lesson).toString());
                }
                return;
            }

            if (levelIdParam == null) {
                JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "MissingParameter", "Missing levelId");
                return;
            }
            int levelId = Integer.parseInt(levelIdParam);
            List<Lesson> lessons = dao.findByLevel(levelId);
            JSONArray array = new JSONArray();
            for (Lesson lesson : lessons) {
                array.put(toJson(lesson));
            }
            resp.getWriter().print(array.toString());
        } catch (NumberFormatException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "InvalidId", "id must be a number");
        } catch (SQLException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DatabaseError", "Database error occurred");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            JSONObject payload = JsonRequestHelper.readJsonObject(req);
            JSONObject errors = new JSONObject();
            Integer levelId = JsonValidationUtils.readPositiveInt(payload.opt("levelId"), "levelId", false, errors);
            String title = JsonValidationUtils.readRequiredString(payload, "title", errors);
            String overview = JsonValidationUtils.readOptionalString(payload, "overview");
            Integer orderIndex = JsonValidationUtils.readNonNegativeInt(payload.opt("orderIndex"), "orderIndex", true, errors);

            if (errors.length() > 0) {
                JsonRequestHelper.writeValidationErrors(resp, errors);
                return;
            }

            Lesson lesson = new Lesson();
            lesson.setLevelId(levelId);
            lesson.setTitle(title);
            lesson.setOverview(overview);
            lesson.setOrderIndex(orderIndex != null ? orderIndex : 0);

            LessonDAO dao = new LessonDAO();
            dao.insert(lesson);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().print(toJson(lesson).toString());
        } catch (IllegalArgumentException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "InvalidRequest", ex.getMessage());
        } catch (SQLException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DatabaseError", "Database error occurred");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            JSONObject payload = JsonRequestHelper.readJsonObject(req);
            JSONObject errors = new JSONObject();
            Object rawId = payload.has("lessonId") ? payload.opt("lessonId") : payload.opt("id");
            Long id = JsonValidationUtils.readPositiveLong(rawId, "lessonId", false, errors);
            if (errors.length() > 0) {
                JsonRequestHelper.writeValidationErrors(resp, errors);
                return;
            }

            LessonDAO dao = new LessonDAO();
            Lesson existing = dao.findById(id);
            if (existing == null) {
                JsonRequestHelper.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "LessonNotFound", "Lesson not found");
                return;
            }

            boolean hasUpdates = false;
            if (payload.has("levelId")) {
                Integer levelId = JsonValidationUtils.readPositiveInt(payload.opt("levelId"), "levelId", false, errors);
                if (levelId != null) {
                    existing.setLevelId(levelId);
                    hasUpdates = true;
                }
            }
            if (payload.has("title")) {
                String title = JsonValidationUtils.readOptionalNonBlankString(payload, "title", errors);
                if (title != null) {
                    existing.setTitle(title);
                    hasUpdates = true;
                }
            }
            if (payload.has("overview")) {
                existing.setOverview(JsonValidationUtils.toNullableString(payload.opt("overview")));
                hasUpdates = true;
            }
            if (payload.has("orderIndex")) {
                Integer orderIndex = JsonValidationUtils.readNonNegativeInt(payload.opt("orderIndex"), "orderIndex", false, errors);
                if (orderIndex != null) {
                    existing.setOrderIndex(orderIndex);
                    hasUpdates = true;
                }
            }

            if (errors.length() > 0) {
                JsonRequestHelper.writeValidationErrors(resp, errors);
                return;
            }
            if (!hasUpdates) {
                JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "NoChanges", "No fields supplied for update");
                return;
            }
            dao.update(existing);
            resp.getWriter().print(toJson(existing).toString());
        } catch (IllegalArgumentException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "InvalidRequest", ex.getMessage());
        } catch (SQLException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DatabaseError", "Database error occurred");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String idParam = req.getParameter("id");
        String lessonIdParam = req.getParameter("lessonId");
        String targetId = idParam != null ? idParam : lessonIdParam;
        if (targetId == null) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "MissingParameter", "Missing lesson id");
            return;
        }
        try {
            long id = Long.parseLong(targetId);
            LessonDAO dao = new LessonDAO();
            if (!dao.delete(id)) {
                JsonRequestHelper.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "LessonNotFound", "Lesson not found");
                return;
            }
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NumberFormatException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "InvalidId", "lessonId must be a number");
        } catch (SQLException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DatabaseError", "Database error occurred");
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
