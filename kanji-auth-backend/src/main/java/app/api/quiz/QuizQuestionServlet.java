package app.api.quiz;

import app.dao.QuizQuestionDAO;
import app.model.QuizChoice;
import app.model.QuizQuestion;
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
 * API trả danh sách câu hỏi trắc nghiệm theo bài học.
 */
@WebServlet(name = "QuizQuestionServlet", urlPatterns = "/api/quiz/questions")
public class QuizQuestionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String lessonIdParam = req.getParameter("lessonId");
        if (lessonIdParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(new JSONObject().put("error", "Missing lessonId").toString());
            return;
        }
        try {
            long lessonId = Long.parseLong(lessonIdParam);
            List<QuizQuestion> questions = new QuizQuestionDAO().findByLesson(lessonId);
            JSONArray array = new JSONArray();
            for (QuizQuestion question : questions) {
                JSONArray choices = new JSONArray();
                for (QuizChoice choice : question.getChoices()) {
                    choices.put(new JSONObject()
                            .put("choiceId", choice.getId())
                            .put("questionId", choice.getQuestionId())
                            .put("content", choice.getContent())
                            .put("isCorrect", choice.isCorrect()));
                }
                array.put(new JSONObject()
                        .put("questionId", question.getId())
                        .put("lessonId", question.getLessonId())
                        .put("prompt", question.getPrompt())
                        .put("explanation", question.getExplanation())
                        .put("orderIndex", question.getOrderIndex())
                        .put("choices", choices));
            }
            resp.getWriter().print(array.toString());
        } catch (NumberFormatException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(new JSONObject().put("error", "Invalid lessonId").toString());
        } catch (SQLException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(new JSONObject().put("error", "Database error").toString());
        }
    }
}
