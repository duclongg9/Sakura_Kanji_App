package app.api.kanji;

import app.api.support.JsonRequestHelper;
import app.api.support.JsonValidationUtils;
import app.dao.KanjiDAO;
import app.model.Kanji;
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
 * API xem và thêm Kanji.
 */
@WebServlet(name = "KanjiServlet", urlPatterns = "/api/kanji")
public class KanjiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String idParam = req.getParameter("id");
        String levelIdParam = req.getParameter("levelId");
        KanjiDAO dao = new KanjiDAO();
        try {
            if (idParam != null) {
                long id = Long.parseLong(idParam);
                Kanji kanji = dao.findById(id);
                if (kanji == null) {
                    JsonRequestHelper.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "KanjiNotFound", "Kanji not found");
                } else {
                    resp.getWriter().print(toJson(kanji).toString());
                }
                return;
            }

            if (levelIdParam == null) {
                JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "MissingParameter", "Missing levelId");
                return;
            }

            int levelId = Integer.parseInt(levelIdParam);
            List<Kanji> kanjiList = dao.findByLevel(levelId);
            JSONArray array = new JSONArray();
            for (Kanji kanji : kanjiList) {
                array.put(toJson(kanji));
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
            String character = JsonValidationUtils.readRequiredString(payload, "character", errors);
            String hanViet = JsonValidationUtils.readOptionalString(payload, "hanViet");
            String onReading = JsonValidationUtils.readOptionalString(payload, "onReading");
            String kunReading = JsonValidationUtils.readOptionalString(payload, "kunReading");
            String description = JsonValidationUtils.readOptionalString(payload, "description");
            Integer levelId = null;
            if (payload.has("levelId")) {
                if (!payload.isNull("levelId")) {
                    levelId = JsonValidationUtils.readPositiveInt(payload.opt("levelId"), "levelId", true, errors);
                }
            }

            if (errors.length() > 0) {
                JsonRequestHelper.writeValidationErrors(resp, errors);
                return;
            }

            Kanji kanji = new Kanji();
            kanji.setCharacter(character);
            kanji.setHanViet(hanViet);
            kanji.setOnReading(onReading);
            kanji.setKunReading(kunReading);
            kanji.setDescription(description);
            kanji.setLevelId(levelId);

            KanjiDAO dao = new KanjiDAO();
            dao.insert(kanji);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().print(toJson(kanji).toString());
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
            Long id = JsonValidationUtils.readPositiveLong(payload.opt("id"), "id", false, errors);
            if (errors.length() > 0) {
                JsonRequestHelper.writeValidationErrors(resp, errors);
                return;
            }

            KanjiDAO dao = new KanjiDAO();
            Kanji existing = dao.findById(id);
            if (existing == null) {
                JsonRequestHelper.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "KanjiNotFound", "Kanji not found");
                return;
            }

            boolean hasUpdates = false;
            if (payload.has("character")) {
                String character = JsonValidationUtils.readOptionalNonBlankString(payload, "character", errors);
                if (character != null) {
                    existing.setCharacter(character);
                    hasUpdates = true;
                }
            }
            if (payload.has("hanViet")) {
                existing.setHanViet(JsonValidationUtils.toNullableString(payload.opt("hanViet")));
                hasUpdates = true;
            }
            if (payload.has("onReading")) {
                existing.setOnReading(JsonValidationUtils.toNullableString(payload.opt("onReading")));
                hasUpdates = true;
            }
            if (payload.has("kunReading")) {
                existing.setKunReading(JsonValidationUtils.toNullableString(payload.opt("kunReading")));
                hasUpdates = true;
            }
            if (payload.has("description")) {
                existing.setDescription(JsonValidationUtils.toNullableString(payload.opt("description")));
                hasUpdates = true;
            }
            if (payload.has("levelId")) {
                if (payload.isNull("levelId")) {
                    existing.setLevelId(null);
                    hasUpdates = true;
                } else {
                    Integer levelId = JsonValidationUtils.readPositiveInt(payload.opt("levelId"), "levelId", true, errors);
                    if (levelId != null) {
                        existing.setLevelId(levelId);
                        hasUpdates = true;
                    }
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
            long id = Long.parseLong(idParam);
            KanjiDAO dao = new KanjiDAO();
            if (!dao.delete(id)) {
                JsonRequestHelper.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "KanjiNotFound", "Kanji not found");
                return;
            }
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NumberFormatException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "InvalidId", "id must be a number");
        } catch (SQLException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DatabaseError", "Database error occurred");
        }
    }

    private JSONObject toJson(Kanji kanji) {
        return new JSONObject()
                .put("id", kanji.getId())
                .put("character", kanji.getCharacter())
                .put("hanViet", kanji.getHanViet())
                .put("onReading", kanji.getOnReading())
                .put("kunReading", kanji.getKunReading())
                .put("description", kanji.getDescription())
                .put("levelId", kanji.getLevelId());
    }

}
