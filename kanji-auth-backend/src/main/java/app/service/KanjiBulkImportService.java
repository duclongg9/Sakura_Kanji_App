package app.service;

import app.dao.BaseDAO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Dịch vụ xử lý import Kanji và câu hỏi trắc nghiệm từ tệp CSV.
 */
public class KanjiBulkImportService extends BaseDAO {

    /**
     * Thực hiện parse tệp CSV và lưu từng dòng vào cơ sở dữ liệu.
     * <p>
     * Định dạng CSV hỗ trợ các cột sau (không phân biệt hoa thường):
     * <ul>
     *     <li><b>character</b> – ký tự Kanji bắt buộc.</li>
     *     <li><b>hanViet</b>, <b>onReading</b>, <b>kunReading</b>, <b>description</b> – mô tả thêm cho Kanji.</li>
     *     <li><b>levelId</b> hoặc <b>levelName</b> – xác định level liên kết.</li>
     *     <li><b>lessonId</b> – id bài học dùng cho câu hỏi (bắt buộc nếu có prompt).</li>
     *     <li><b>questionPrompt</b>, <b>questionExplanation</b> – nội dung câu hỏi.</li>
     *     <li><b>correctChoice</b>, <b>wrongChoice1</b>.. <b>wrongChoice3</b> – đáp án trắc nghiệm.</li>
     *     <li><b>questionOrder</b> – thứ tự hiển thị câu hỏi (tùy chọn).</li>
     * </ul>
     * Nếu Kanji đã tồn tại (theo ký tự), hệ thống sẽ cập nhật nội dung thay vì tạo mới.
     *
     * @param inputStream stream dữ liệu CSV.
     * @return báo cáo chứa thống kê số bản ghi xử lý và lỗi từng dòng nếu có.
     * @throws IOException  nếu đọc stream thất bại.
     * @throws SQLException nếu thao tác ghi cơ sở dữ liệu gặp lỗi.
     */
    public ImportReport importCsv(InputStream inputStream) throws IOException, SQLException {
        List<KanjiCsvRow> rows = parseCsv(inputStream);
        ImportReport report = new ImportReport();
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement findKanji = connection.prepareStatement("SELECT id FROM Kanji WHERE kanji = ?");
                 PreparedStatement insertKanji = connection.prepareStatement(
                         "INSERT INTO Kanji (kanji, hanViet, amOn, amKun, moTa, levelId) VALUES (?,?,?,?,?,?)",
                         Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement updateKanji = connection.prepareStatement(
                         "UPDATE Kanji SET hanViet = ?, amOn = ?, amKun = ?, moTa = ?, levelId = ? WHERE kanji = ?");
                 PreparedStatement findLevelByName = connection.prepareStatement("SELECT id FROM Level WHERE name = ?");
                 PreparedStatement findLesson = connection.prepareStatement("SELECT lesson_id FROM lessons WHERE lesson_id = ?");
                 PreparedStatement nextOrder = connection.prepareStatement(
                         "SELECT COALESCE(MAX(order_index),0) + 1 FROM quiz_questions WHERE lesson_id = ?");
                 PreparedStatement insertQuestion = connection.prepareStatement(
                         "INSERT INTO quiz_questions (lesson_id, prompt, explanation, order_index) VALUES (?,?,?,?)",
                         Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement insertChoice = connection.prepareStatement(
                         "INSERT INTO quiz_choices (question_id, content, is_correct) VALUES (?,?,?)")) {

                for (KanjiCsvRow row : rows) {
                    if (!row.errors.isEmpty()) {
                        report.totalRows++;
                        report.errors.add(new ImportError(row.rowNumber, String.join("; ", row.errors)));
                        continue;
                    }
                    report.totalRows++;
                    try {
                        Integer levelId = resolveLevelId(row, findLevelByName, connection);
                        KanjiUpsertResult upsert = upsertKanji(row, levelId, findKanji, insertKanji, updateKanji);
                        if (upsert.created) {
                            report.kanjiInserted++;
                        } else {
                            report.kanjiUpdated++;
                        }

                        if (row.hasQuestion()) {
                            if (row.lessonId == null) {
                                throw new IllegalArgumentException("lessonId is required when questionPrompt is provided");
                            }
                            if (!lessonExists(row.lessonId, findLesson)) {
                                throw new IllegalArgumentException("lessonId=" + row.lessonId + " does not exist");
                            }
                            int orderIndex = row.questionOrder != null
                                    ? row.questionOrder
                                    : nextQuestionOrder(row.lessonId, nextOrder);
                            long questionId = insertQuestion(row, orderIndex, insertQuestion);
                            report.questionsCreated++;
                            insertChoice(questionId, row.correctChoice, true, insertChoice);
                            report.choicesCreated++;
                            for (String wrongChoice : row.wrongChoices) {
                                if (wrongChoice != null && !wrongChoice.isBlank()) {
                                    insertChoice(questionId, wrongChoice, false, insertChoice);
                                    report.choicesCreated++;
                                }
                            }
                        }

                        connection.commit();
                    } catch (Exception ex) {
                        connection.rollback();
                        report.errors.add(new ImportError(row.rowNumber, ex.getMessage()));
                    }
                }
            } finally {
                connection.setAutoCommit(true);
            }
        }
        return report;
    }

    private List<KanjiCsvRow> parseCsv(InputStream inputStream) throws IOException {
        List<KanjiCsvRow> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return rows;
            }
            String[] headers = splitCsvLine(headerLine);
            Map<String, Integer> indexMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                String key = headers[i] != null ? headers[i].trim().toLowerCase(Locale.ROOT) : null;
                if (key != null && !key.isEmpty()) {
                    indexMap.put(key, i);
                }
            }

            String line;
            int rowNumber = 1;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] columns = splitCsvLine(line);
                KanjiCsvRow row = new KanjiCsvRow();
                row.rowNumber = rowNumber;
                row.character = read(columns, indexMap, "character");
                if (row.character == null || row.character.isBlank()) {
                    row.errors.add("Missing character value");
                }
                row.hanViet = read(columns, indexMap, "hanviet");
                row.onReading = read(columns, indexMap, "onreading");
                row.kunReading = read(columns, indexMap, "kunreading");
                row.description = read(columns, indexMap, "description");
                row.levelName = read(columns, indexMap, "levelname");
                String levelIdRaw = read(columns, indexMap, "levelid");
                if (levelIdRaw != null && !levelIdRaw.isBlank()) {
                    try {
                        row.levelId = Integer.parseInt(levelIdRaw.trim());
                    } catch (NumberFormatException ex) {
                        row.errors.add("Invalid levelId: " + levelIdRaw);
                    }
                }
                String lessonIdRaw = read(columns, indexMap, "lessonid");
                if (lessonIdRaw != null && !lessonIdRaw.isBlank()) {
                    try {
                        row.lessonId = Long.parseLong(lessonIdRaw.trim());
                    } catch (NumberFormatException ex) {
                        row.errors.add("Invalid lessonId: " + lessonIdRaw);
                    }
                }
                row.questionPrompt = read(columns, indexMap, "questionprompt");
                row.questionExplanation = read(columns, indexMap, "questionexplanation");
                row.correctChoice = read(columns, indexMap, "correctchoice");
                row.wrongChoices.add(read(columns, indexMap, "wrongchoice1"));
                row.wrongChoices.add(read(columns, indexMap, "wrongchoice2"));
                row.wrongChoices.add(read(columns, indexMap, "wrongchoice3"));
                String orderRaw = read(columns, indexMap, "questionorder");
                if (orderRaw != null && !orderRaw.isBlank()) {
                    try {
                        row.questionOrder = Integer.parseInt(orderRaw.trim());
                    } catch (NumberFormatException ex) {
                        row.errors.add("Invalid questionOrder: " + orderRaw);
                    }
                }

                rows.add(row);
            }
        }
        return rows;
    }

    private String[] splitCsvLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    current.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        values.add(current.toString());
        return values.toArray(new String[0]);
    }

    private String read(String[] columns, Map<String, Integer> indexMap, String key) {
        Integer index = indexMap.get(key);
        if (index == null || index >= columns.length) {
            return null;
        }
        String value = columns[index];
        return value != null ? value.trim() : null;
    }

    private Integer resolveLevelId(KanjiCsvRow row, PreparedStatement findLevelByName, Connection connection) throws SQLException {
        if (row.levelId != null) {
            return row.levelId;
        }
        if (row.levelName == null || row.levelName.isBlank()) {
            return null;
        }
        findLevelByName.clearParameters();
        findLevelByName.setString(1, row.levelName.trim());
        try (ResultSet rs = findLevelByName.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Level '" + row.levelName + "' not found");
    }

    private KanjiUpsertResult upsertKanji(KanjiCsvRow row, Integer levelId, PreparedStatement findKanji,
                             PreparedStatement insertKanji, PreparedStatement updateKanji) throws SQLException {
        if (row.character == null || row.character.isBlank()) {
            throw new IllegalArgumentException("character is required");
        }

        findKanji.clearParameters();
        findKanji.setString(1, row.character.trim());
        try (ResultSet rs = findKanji.executeQuery()) {
            if (rs.next()) {
                long existingId = rs.getLong(1);
                updateKanji.clearParameters();
                if (row.hanViet != null && !row.hanViet.isBlank()) {
                    updateKanji.setString(1, row.hanViet);
                } else {
                    updateKanji.setNull(1, java.sql.Types.VARCHAR);
                }
                if (row.onReading != null && !row.onReading.isBlank()) {
                    updateKanji.setString(2, row.onReading);
                } else {
                    updateKanji.setNull(2, java.sql.Types.VARCHAR);
                }
                if (row.kunReading != null && !row.kunReading.isBlank()) {
                    updateKanji.setString(3, row.kunReading);
                } else {
                    updateKanji.setNull(3, java.sql.Types.VARCHAR);
                }
                if (row.description != null && !row.description.isBlank()) {
                    updateKanji.setString(4, row.description);
                } else {
                    updateKanji.setNull(4, java.sql.Types.VARCHAR);
                }
                if (levelId == null) {
                    updateKanji.setNull(5, java.sql.Types.INTEGER);
                } else {
                    updateKanji.setInt(5, levelId);
                }
                updateKanji.setString(6, row.character.trim());
                updateKanji.executeUpdate();
                return new KanjiUpsertResult(existingId, false);
            }
        }

        insertKanji.clearParameters();
        insertKanji.setString(1, row.character.trim());
        insertKanji.setString(2, row.hanViet);
        insertKanji.setString(3, row.onReading);
        insertKanji.setString(4, row.kunReading);
        insertKanji.setString(5, row.description);
        if (levelId == null) {
            insertKanji.setNull(6, java.sql.Types.INTEGER);
        } else {
            insertKanji.setInt(6, levelId);
        }
        insertKanji.executeUpdate();
        try (ResultSet keys = insertKanji.getGeneratedKeys()) {
            if (keys.next()) {
                return new KanjiUpsertResult(keys.getLong(1), true);
            }
        }
        throw new SQLException("Cannot create Kanji for character " + row.character);
    }

    private boolean lessonExists(Long lessonId, PreparedStatement findLesson) throws SQLException {
        findLesson.clearParameters();
        findLesson.setLong(1, lessonId);
        try (ResultSet rs = findLesson.executeQuery()) {
            return rs.next();
        }
    }

    private int nextQuestionOrder(Long lessonId, PreparedStatement nextOrder) throws SQLException {
        nextOrder.clearParameters();
        nextOrder.setLong(1, lessonId);
        try (ResultSet rs = nextOrder.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 1;
    }

    private long insertQuestion(KanjiCsvRow row, int orderIndex, PreparedStatement insertQuestion) throws SQLException {
        if (row.questionPrompt == null || row.questionPrompt.isBlank()) {
            throw new IllegalArgumentException("questionPrompt must not be blank");
        }
        insertQuestion.clearParameters();
        insertQuestion.setLong(1, row.lessonId);
        insertQuestion.setString(2, row.questionPrompt);
        insertQuestion.setString(3, row.questionExplanation != null ? row.questionExplanation : "");
        insertQuestion.setInt(4, orderIndex);
        insertQuestion.executeUpdate();
        try (ResultSet keys = insertQuestion.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getLong(1);
            }
        }
        throw new SQLException("Cannot create quiz question for lesson " + row.lessonId);
    }

    private void insertChoice(long questionId, String content, boolean correct, PreparedStatement insertChoice)
            throws SQLException {
        if (content == null || content.isBlank()) {
            return;
        }
        insertChoice.clearParameters();
        insertChoice.setLong(1, questionId);
        insertChoice.setString(2, content);
        insertChoice.setBoolean(3, correct);
        insertChoice.executeUpdate();
    }

    /**
     * Báo cáo kết quả import trả về cho servlet hiển thị.
     */
    public static class ImportReport {
        public int totalRows;
        public int questionsCreated;
        public int choicesCreated;
        public int kanjiInserted;
        public int kanjiUpdated;
        public final List<ImportError> errors = new ArrayList<>();

        /**
         * @return số lượng Kanji mới tạo (bằng tổng dòng trừ số lỗi vì mỗi dòng tương ứng một Kanji mới hoặc cập nhật).
         */
        public int getKanjiProcessed() {
            return totalRows - errors.size();
        }
    }

    /**
     * Lỗi phát sinh trên từng dòng CSV.
     */
    public static class ImportError {
        public final int rowNumber;
        public final String message;

        public ImportError(int rowNumber, String message) {
            this.rowNumber = rowNumber;
            this.message = message;
        }
    }

    private static class KanjiCsvRow {
        int rowNumber;
        String character;
        String hanViet;
        String onReading;
        String kunReading;
        String description;
        Integer levelId;
        String levelName;
        Long lessonId;
        String questionPrompt;
        String questionExplanation;
        String correctChoice;
        final List<String> wrongChoices = new ArrayList<>();
        Integer questionOrder;
        final List<String> errors = new ArrayList<>();

        boolean hasQuestion() {
            return questionPrompt != null && !questionPrompt.isBlank();
        }
    }

    private static class KanjiUpsertResult {
        final long id;
        final boolean created;

        KanjiUpsertResult(long id, boolean created) {
            this.id = id;
            this.created = created;
        }
    }
}
