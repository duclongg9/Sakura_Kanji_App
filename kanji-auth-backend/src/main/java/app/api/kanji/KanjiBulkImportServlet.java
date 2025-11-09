package app.api.kanji;

import app.api.support.JsonRequestHelper;
import app.service.KanjiBulkImportService;
import app.service.KanjiBulkImportService.ImportError;
import app.service.KanjiBulkImportService.ImportReport;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Endpoint cho phép quản trị viên import danh sách Kanji và câu hỏi qua tệp CSV.
 */
@WebServlet(name = "KanjiBulkImportServlet", urlPatterns = "/api/kanji/import")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024)
public class KanjiBulkImportServlet extends HttpServlet {

    /**
     * Nhận file CSV từ form multipart và chuyển cho {@link KanjiBulkImportService} xử lý.
     *
     * @param req  request multipart chứa trường <code>file</code>.
     * @param resp phản hồi JSON mô tả kết quả import hoặc lỗi.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        Part filePart = req.getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "MissingFile", "CSV file is required");
            return;
        }

        String fileName = extractFileName(filePart);
        if (fileName != null && !fileName.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "InvalidFile", "Only CSV files are supported");
            return;
        }

        KanjiBulkImportService service = new KanjiBulkImportService();
        try (InputStream inputStream = filePart.getInputStream()) {
            ImportReport report = service.importCsv(inputStream);
            JSONObject result = new JSONObject()
                    .put("totalRows", report.totalRows)
                    .put("kanjiInserted", report.kanjiInserted)
                    .put("kanjiUpdated", report.kanjiUpdated)
                    .put("questionsCreated", report.questionsCreated)
                    .put("choicesCreated", report.choicesCreated)
                    .put("fileName", fileName != null ? fileName : JSONObject.NULL);

            JSONArray errorArray = new JSONArray();
            for (ImportError error : report.errors) {
                errorArray.put(new JSONObject()
                        .put("rowNumber", error.rowNumber)
                        .put("message", error.message));
            }
            result.put("errors", errorArray);
            resp.getWriter().print(result.toString());
        } catch (IllegalArgumentException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "InvalidCsv", ex.getMessage());
        } catch (Exception ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ImportFailed", "Cannot import CSV: " + ex.getMessage());
        }
    }

    /**
     * Tách tên file từ header Content-Disposition.
     */
    private String extractFileName(Part part) {
        String contentDisposition = part.getHeader("Content-Disposition");
        if (contentDisposition == null) {
            return null;
        }
        for (String token : contentDisposition.split(";")) {
            String trimmed = token.trim();
            if (trimmed.startsWith("filename=")) {
                String name = trimmed.substring("filename=".length());
                return name.replace('"', ' ').trim();
            }
        }
        return null;
    }
}
