package app.api.kanji;

import app.dao.KanjiDAO;
import app.model.Kanji;
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
 * API xem và thêm Kanji.
 */
@WebServlet(name = "KanjiServlet", urlPatterns = "/api/kanji")
public class KanjiServlet extends HttpServlet {

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
            List<Kanji> kanjiList = new KanjiDAO().findByLevel(levelId);
            JSONArray array = new JSONArray();
            for (Kanji kanji : kanjiList) {
                array.put(toJson(kanji));
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
            String character = payload.optString("character");
            String hanViet = payload.optString("hanViet", null);
            String onReading = payload.optString("onReading", null);
            String kunReading = payload.optString("kunReading", null);
            String description = payload.optString("description", null);
            Integer levelId = payload.has("levelId") ? payload.optInt("levelId") : null;
            if (payload.isNull("levelId")) {
                levelId = null;
            }

            if (character.isBlank()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(new JSONObject().put("error", "Character is required"));
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
            out.print(toJson(kanji).toString());
        } catch (SQLException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(new JSONObject().put("error", "Database error").toString());
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
