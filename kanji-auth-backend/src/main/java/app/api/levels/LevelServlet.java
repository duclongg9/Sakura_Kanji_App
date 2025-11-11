package app.api.levels;

import app.api.support.JsonRequestHelper;
import app.api.support.JsonValidationUtils;
import app.dao.LevelDAO;
import app.model.Level;
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
 * API quản lý level theo JLPT.
 */
@WebServlet(name = "LevelServlet", urlPatterns = "/api/levels")
public class LevelServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String idParam = req.getParameter("id");
        String jlptIdParam = req.getParameter("jlptId");
        boolean includeInactive = Boolean.parseBoolean(req.getParameter("includeInactive"));
        LevelDAO dao = new LevelDAO();
        try {
            if (idParam != null) {
                int id = Integer.parseInt(idParam);
                Level level = dao.findById(id);
                if (level == null) {
                    JsonRequestHelper.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "LevelNotFound", "Level not found");
                } else {
                    resp.getWriter().print(toJson(level).toString());
                }
                return;
            }

            if (jlptIdParam == null) {
                JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "MissingParameter", "Missing jlptId");
                return;
            }
            int jlptId = Integer.parseInt(jlptIdParam);
            List<Level> levels = dao.findByJlpt(jlptId, includeInactive);
            JSONArray array = new JSONArray();
            for (Level level : levels) {
                array.put(toJson(level));
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
            String name = JsonValidationUtils.readRequiredString(payload, "name", errors);
            Integer jlptId = JsonValidationUtils.readPositiveInt(payload.opt("jlptLevelId"), "jlptLevelId", false, errors);
            String description = JsonValidationUtils.readOptionalString(payload, "description");
            String tierCandidate = JsonValidationUtils.readOptionalNonBlankString(payload, "accessTier", errors);
            Boolean activeFlag = JsonValidationUtils.readBoolean(payload.opt("active"), "active", true, errors);

            if (errors.length() > 0) {
                JsonRequestHelper.writeValidationErrors(resp, errors);
                return;
            }

            String accessTier = tierCandidate != null ? tierCandidate : "FREE";
            boolean isActive = activeFlag != null ? activeFlag : true;
            Level level = new Level();
            level.setName(name);
            level.setJlptLevelId(jlptId);
            level.setDescription(description);
            level.setAccessTier(accessTier);
            level.setActive(isActive);

            LevelDAO dao = new LevelDAO();
            dao.insert(level);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().print(toJson(level).toString());
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
            Integer id = JsonValidationUtils.readPositiveInt(payload.opt("id"), "id", false, errors);
            if (errors.length() > 0) {
                JsonRequestHelper.writeValidationErrors(resp, errors);
                return;
            }

            LevelDAO dao = new LevelDAO();
            Level existing = dao.findById(id);
            if (existing == null) {
                JsonRequestHelper.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "LevelNotFound", "Level not found");
                return;
            }

            boolean hasUpdates = false;
            if (payload.has("name")) {
                String name = JsonValidationUtils.readOptionalNonBlankString(payload, "name", errors);
                if (name != null) {
                    existing.setName(name);
                    hasUpdates = true;
                }
            }
            if (payload.has("jlptLevelId")) {
                Integer jlptLevelId = JsonValidationUtils.readPositiveInt(payload.opt("jlptLevelId"), "jlptLevelId", false, errors);
                if (jlptLevelId != null) {
                    existing.setJlptLevelId(jlptLevelId);
                    hasUpdates = true;
                }
            }
            if (payload.has("description")) {
                existing.setDescription(JsonValidationUtils.toNullableString(payload.opt("description")));
                hasUpdates = true;
            }
            if (payload.has("accessTier")) {
                String accessTier = JsonValidationUtils.readOptionalNonBlankString(payload, "accessTier", errors);
                if (accessTier != null) {
                    existing.setAccessTier(accessTier);
                    hasUpdates = true;
                }
            }
            if (payload.has("active")) {
                Boolean active = JsonValidationUtils.readBoolean(payload.opt("active"), "active", false, errors);
                if (active != null) {
                    existing.setActive(active);
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
        if (idParam == null) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "MissingParameter", "Missing id");
            return;
        }
        try {
            int id = Integer.parseInt(idParam);
            LevelDAO dao = new LevelDAO();
            if (!dao.delete(id)) {
                JsonRequestHelper.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "LevelNotFound", "Level not found");
                return;
            }
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NumberFormatException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "InvalidId", "id must be a number");
        } catch (SQLException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DatabaseError", "Database error occurred");
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
