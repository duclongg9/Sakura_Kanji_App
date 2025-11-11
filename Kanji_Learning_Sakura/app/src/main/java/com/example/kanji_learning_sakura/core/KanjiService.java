package com.example.kanji_learning_sakura.core;

import android.content.Context;
import com.example.kanji_learning_sakura.model.AccountUpgradeRequestDto;
import com.example.kanji_learning_sakura.model.AdminMemberDto;
import com.example.kanji_learning_sakura.model.AuthResponseDto;
import com.example.kanji_learning_sakura.model.BulkImportReportDto;
import com.example.kanji_learning_sakura.model.BulkImportReportDto.RowError;
import com.example.kanji_learning_sakura.model.JlptLevelDto;
import com.example.kanji_learning_sakura.model.KanjiDto;
import com.example.kanji_learning_sakura.model.LessonDto;
import com.example.kanji_learning_sakura.model.LevelDto;
import com.example.kanji_learning_sakura.model.MomoPaymentDto;
import com.example.kanji_learning_sakura.model.MomoPaymentStatusDto;
import com.example.kanji_learning_sakura.model.ProfileDto;
import com.example.kanji_learning_sakura.model.QuizChoiceDto;
import com.example.kanji_learning_sakura.model.QuizQuestionDto;
import com.example.kanji_learning_sakura.model.VipPlanDto;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Lớp helper gọi HTTP tới backend và map dữ liệu thành DTO.
 */
public class KanjiService {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final String baseUrl;

    public KanjiService(Context context) {
        this.client = ApiClient.get(context);
        this.baseUrl = ApiClient.baseUrl(context);
    }

    /**
     * Đăng nhập tài khoản truyền thống bằng email/mật khẩu.
     *
     * @param email    địa chỉ email hoặc username.
     * @param password mật khẩu thuần.
     * @return thông tin phản hồi chứa token và role.
     * @throws Exception nếu request HTTP thất bại hoặc dữ liệu không hợp lệ.
     */
    public AuthResponseDto login(String email, String password) throws Exception {
        JSONObject payload = new JSONObject()
                .put("email", email)
                .put("password", password);
        Request request = new Request.Builder()
                .url(baseUrl + "/api/auth/login")
                .post(RequestBody.create(payload.toString(), JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalStateException("Đăng nhập thất bại (" + response.code() + ")");
            }
            JSONObject data = new JSONObject(response.body() != null ? response.body().string() : "{}");
            AuthResponseDto dto = new AuthResponseDto();
            dto.setToken(data.optString("token"));
            dto.setRoleId(data.optInt("roleId"));
            dto.setUserName(data.optString("userName"));
            dto.setUserId(data.optLong("userId"));
            dto.setEmail(data.optString("email", null));
            dto.setAvatarUrl(data.optString("imgUrl", null));
            dto.setAccountTier(data.optString("accountTier", "FREE"));
            dto.setAccountBalance(data.optDouble("accountBalance", 0));
            dto.setVipExpiresAt(data.isNull("vipExpiresAt") ? null : data.optString("vipExpiresAt"));
            dto.setBio(data.optString("bio", null));
            dto.setHasPendingUpgradeRequest(data.optBoolean("hasPendingUpgradeRequest", false));
            return dto;
        }
    }

    /**
     * Đăng nhập bằng Google Sign-In (backend xác minh ID token).
     *
     * @param idToken ID token lấy từ GoogleSignInClient.
     * @return thông tin người dùng từ backend.
     * @throws Exception nếu request HTTP thất bại.
     */
    public AuthResponseDto loginWithGoogle(String idToken) throws Exception {
        JSONObject payload = new JSONObject().put("idToken", idToken);
        Request request = new Request.Builder()
                .url(baseUrl + "/api/auth/google")
                .post(RequestBody.create(payload.toString(), JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalStateException("Google login failed (" + response.code() + ")");
            }
            JSONObject data = new JSONObject(response.body() != null ? response.body().string() : "{}");
            AuthResponseDto dto = new AuthResponseDto();
            dto.setToken(data.optString("token"));
            dto.setRoleId(data.optInt("roleId"));
            dto.setUserName(data.optString("userName"));
            dto.setUserId(data.optLong("userId"));
            dto.setEmail(data.optString("email", null));
            dto.setAvatarUrl(data.optString("imgUrl", null));
            dto.setAccountTier(data.optString("accountTier", "FREE"));
            dto.setAccountBalance(data.optDouble("accountBalance", 0));
            dto.setVipExpiresAt(data.isNull("vipExpiresAt") ? null : data.optString("vipExpiresAt"));
            dto.setBio(data.optString("bio", null));
            dto.setHasPendingUpgradeRequest(data.optBoolean("hasPendingUpgradeRequest", false));
            return dto;
        }
    }

    /**
     * Tải thông tin hồ sơ người dùng hiện tại.
     *
     * @return {@link ProfileDto} chứa thông tin cơ bản và quyền hạn.
     * @throws Exception nếu không thể gọi API.
     */
    public ProfileDto getProfile() throws Exception {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/auth/me")
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalStateException("HTTP " + response.code());
            }
            JSONObject obj = new JSONObject(response.body() != null ? response.body().string() : "{}");
            ProfileDto dto = new ProfileDto();
            dto.setId(obj.optLong("id"));
            dto.setUserName(obj.optString("userName"));
            dto.setEmail(obj.optString("email"));
            dto.setRoleId(obj.optInt("roleId"));
            dto.setAvatarUrl(obj.optString("imgUrl", null));
            dto.setAccountTier(obj.optString("accountTier", "FREE"));
            dto.setAccountBalance(obj.optDouble("accountBalance", 0));
            dto.setVipExpiresAt(obj.isNull("vipExpiresAt") ? null : obj.optString("vipExpiresAt"));
            dto.setBio(obj.optString("bio", null));
            dto.setHasPendingUpgradeRequest(obj.optBoolean("hasPendingUpgradeRequest", false));
            return dto;
        }
    }

    /**
     * Gửi yêu cầu nâng cấp VIP thủ công tới quản trị viên.
     *
     * @param note ghi chú tùy chọn từ người dùng.
     * @return {@link AccountUpgradeRequestDto} vừa tạo.
     * @throws Exception nếu backend phản hồi lỗi.
     */
    public AccountUpgradeRequestDto requestVipUpgrade(String note) throws Exception {
        JSONObject payload = new JSONObject();
        if (note != null) {
            String trimmed = note.trim();
            if (!trimmed.isEmpty()) {
                payload.put("note", trimmed);
            }
        }
        Request request = new Request.Builder()
                .url(baseUrl + "/api/account/upgrade-requests")
                .post(RequestBody.create(payload.toString(), JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = readBodyOrThrow(response);
            JSONObject obj = new JSONObject(body.isBlank() ? "{}" : body);
            return parseUpgradeRequest(obj);
        }
    }

    /**
     * Phê duyệt yêu cầu nâng cấp VIP từ phía quản trị viên.
     *
     * @param requestId id yêu cầu cần phê duyệt.
     * @return trạng thái mới của yêu cầu.
     * @throws Exception nếu backend trả lỗi hoặc request thất bại.
     */
    public AccountUpgradeRequestDto approveVipRequest(long requestId) throws Exception {
        JSONObject payload = new JSONObject().put("requestId", requestId);
        Request request = new Request.Builder()
                .url(baseUrl + "/api/admin/upgrade-requests/approve")
                .post(RequestBody.create(payload.toString(), JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = readBodyOrThrow(response);
            JSONObject obj = new JSONObject(body.isBlank() ? "{}" : body);
            return parseUpgradeRequest(obj);
        }
    }

    /**
     * Lấy danh sách JLPT level có trong hệ thống.
     *
     * @return danh sách DTO JLPT.
     * @throws Exception nếu không gọi được API.
     */
    public List<JlptLevelDto> getJlptLevels() throws Exception {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/jlpt-levels")
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalStateException("HTTP " + response.code());
            }
            JSONArray array = new JSONArray(response.body() != null ? response.body().string() : "[]");
            List<JlptLevelDto> result = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                JlptLevelDto dto = new JlptLevelDto();
                dto.setId(obj.optInt("id"));
                dto.setNameLevel(obj.optString("nameLevel"));
                result.add(dto);
            }
            return result;
        }
    }

    /**
     * Tải danh sách level thuộc một JLPT cụ thể.
     *
     * @param jlptId id JLPT đã chọn.
     * @return danh sách level.
     * @throws Exception nếu request thất bại.
     */
    public List<LevelDto> getLevels(int jlptId) throws Exception {
        return getLevels(jlptId, false);
    }

    /**
     * Tải danh sách level với tùy chọn bao gồm level đã ẩn.
     *
     * @param jlptId         id JLPT đã chọn.
     * @param includeInactive {@code true} nếu muốn nhận cả level đã bị tắt hiển thị.
     * @return danh sách level.
     * @throws Exception nếu request thất bại.
     */
    public List<LevelDto> getLevels(int jlptId, boolean includeInactive) throws Exception {
        String url = baseUrl + "/api/levels?jlptId=" + jlptId;
        if (includeInactive) {
            url += "&includeInactive=true";
        }
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = readBodyOrThrow(response);
            JSONArray array = new JSONArray(body.isBlank() ? "[]" : body);
            List<LevelDto> result = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                LevelDto dto = new LevelDto();
                dto.setId(obj.optInt("id"));
                dto.setName(obj.optString("name"));
                dto.setJlptLevelId(obj.optInt("jlptLevelId"));
                dto.setDescription(obj.optString("description", null));
                dto.setAccessTier(obj.optString("accessTier", "FREE"));
                dto.setActive(obj.optBoolean("active", true));
                result.add(dto);
            }
            return result;
        }
    }

    /**
     * Tạo level mới từ màn hình admin.
     *
     * @param name        tên level.
     * @param jlptId      cấp JLPT liên kết.
     * @param description mô tả chi tiết.
     * @param accessTier  quyền truy cập (FREE/PAID).
     * @param active      trạng thái hoạt động.
     * @return level được backend trả về.
     * @throws Exception nếu request thất bại.
     */
    public LevelDto createLevel(String name, int jlptId, String description, String accessTier, boolean active) throws Exception {
        JSONObject payload = new JSONObject()
                .put("name", name)
                .put("jlptLevelId", jlptId)
                .put("description", description)
                .put("accessTier", accessTier)
                .put("active", active);
        Request request = new Request.Builder()
                .url(baseUrl + "/api/levels")
                .post(RequestBody.create(payload.toString(), JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = readBodyOrThrow(response);
            JSONObject obj = new JSONObject(body.isBlank() ? "{}" : body);
            LevelDto dto = new LevelDto();
            dto.setId(obj.optInt("id"));
            dto.setName(obj.optString("name"));
            dto.setJlptLevelId(obj.optInt("jlptLevelId"));
            dto.setDescription(obj.optString("description", null));
            dto.setAccessTier(obj.optString("accessTier", "FREE"));
            dto.setActive(obj.optBoolean("active", true));
            return dto;
        }
    }

    /**
     * Cập nhật một level đã tồn tại.
     *
     * @param id          mã level cần chỉnh sửa.
     * @param name        tên mới của level.
     * @param jlptId      liên kết JLPT.
     * @param description mô tả (có thể null để xóa).
     * @param accessTier  quyền truy cập.
     * @param active      trạng thái hoạt động.
     * @return level sau khi cập nhật.
     * @throws Exception nếu request thất bại.
     */
    public LevelDto updateLevel(int id, String name, int jlptId, String description, String accessTier, boolean active) throws Exception {
        JSONObject payload = new JSONObject()
                .put("id", id)
                .put("name", name)
                .put("jlptLevelId", jlptId)
                .put("accessTier", accessTier)
                .put("active", active);
        if (description != null) {
            payload.put("description", description);
        } else {
            payload.put("description", JSONObject.NULL);
        }
        Request request = new Request.Builder()
                .url(baseUrl + "/api/levels")
                .put(RequestBody.create(payload.toString(), JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = readBodyOrThrow(response);
            JSONObject obj = new JSONObject(body.isBlank() ? "{}" : body);
            LevelDto dto = new LevelDto();
            dto.setId(obj.optInt("id"));
            dto.setName(obj.optString("name"));
            dto.setJlptLevelId(obj.optInt("jlptLevelId"));
            dto.setDescription(obj.isNull("description") ? null : obj.optString("description", null));
            dto.setAccessTier(obj.optString("accessTier", "FREE"));
            dto.setActive(obj.optBoolean("active", true));
            return dto;
        }
    }

    /**
     * Xóa level khỏi hệ thống.
     *
     * @param id khóa chính cần xóa.
     * @throws Exception nếu request thất bại.
     */
    public void deleteLevel(int id) throws Exception {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/levels?id=" + id)
                .delete()
                .build();
        try (Response response = client.newCall(request).execute()) {
            readBodyOrThrow(response);
        }
    }

    /**
     * Gửi yêu cầu thêm Kanji mới.
     *
     * @param character   ký tự Kanji.
     * @param hanViet     âm Hán Việt.
     * @param onReading   âm On.
     * @param kunReading  âm Kun.
     * @param description mô tả.
     * @param levelId     id level gắn với Kanji (có thể null).
     * @return Kanji mới được tạo.
     * @throws Exception nếu request thất bại.
     */
    public KanjiDto createKanji(String character, String hanViet, String onReading, String kunReading,
                                String description, Integer levelId) throws Exception {
        JSONObject payload = new JSONObject()
                .put("character", character)
                .put("hanViet", hanViet)
                .put("onReading", onReading)
                .put("kunReading", kunReading)
                .put("description", description);
        if (levelId != null) {
            payload.put("levelId", levelId);
        } else {
            payload.put("levelId", JSONObject.NULL);
        }
        Request request = new Request.Builder()
                .url(baseUrl + "/api/kanji")
                .post(RequestBody.create(payload.toString(), JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = readBodyOrThrow(response);
            JSONObject obj = new JSONObject(body.isBlank() ? "{}" : body);
            KanjiDto dto = new KanjiDto();
            dto.setId(obj.optLong("id"));
            dto.setCharacter(obj.optString("character"));
            dto.setHanViet(obj.optString("hanViet", null));
            dto.setOnReading(obj.optString("onReading", null));
            dto.setKunReading(obj.optString("kunReading", null));
            dto.setDescription(obj.optString("description", null));
            if (!obj.isNull("levelId")) {
                dto.setLevelId(obj.optInt("levelId"));
            }
            return dto;
        }
    }

    /**
     * Cập nhật Kanji đã tồn tại.
     *
     * @param id          khóa chính Kanji.
     * @param character   ký tự hiển thị.
     * @param hanViet     âm Hán Việt.
     * @param onReading   âm On.
     * @param kunReading  âm Kun.
     * @param description mô tả chi tiết.
     * @param levelId     level gắn với Kanji (có thể null).
     * @return DTO sau khi cập nhật.
     * @throws Exception nếu request thất bại.
     */
    public KanjiDto updateKanji(long id, String character, String hanViet, String onReading, String kunReading,
                                String description, Integer levelId) throws Exception {
        JSONObject payload = new JSONObject()
                .put("id", id)
                .put("character", character)
                .put("hanViet", hanViet)
                .put("onReading", onReading)
                .put("kunReading", kunReading)
                .put("description", description);
        if (levelId == null) {
            payload.put("levelId", JSONObject.NULL);
        } else {
            payload.put("levelId", levelId);
        }
        Request request = new Request.Builder()
                .url(baseUrl + "/api/kanji")
                .put(RequestBody.create(payload.toString(), JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = readBodyOrThrow(response);
            JSONObject obj = new JSONObject(body.isBlank() ? "{}" : body);
            KanjiDto dto = new KanjiDto();
            dto.setId(obj.optLong("id"));
            dto.setCharacter(obj.optString("character"));
            dto.setHanViet(obj.optString("hanViet", null));
            dto.setOnReading(obj.optString("onReading", null));
            dto.setKunReading(obj.optString("kunReading", null));
            dto.setDescription(obj.optString("description", null));
            if (!obj.isNull("levelId")) {
                dto.setLevelId(obj.optInt("levelId"));
            }
            return dto;
        }
    }

    /**
     * Xóa Kanji.
     *
     * @param id khóa chính cần xóa.
     * @throws Exception nếu request thất bại.
     */
    public void deleteKanji(long id) throws Exception {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/kanji?id=" + id)
                .delete()
                .build();
        try (Response response = client.newCall(request).execute()) {
            readBodyOrThrow(response);
        }
    }

    /**
     * Import hàng loạt Kanji và câu hỏi từ tệp CSV.
     *
     * @param data     byte array nội dung CSV.
     * @param fileName tên tệp hiển thị cho backend.
     * @return báo cáo chi tiết số bản ghi xử lý và lỗi.
     * @throws Exception nếu request thất bại.
     */
    public BulkImportReportDto importKanjiCsv(byte[] data, String fileName) throws Exception {
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName != null ? fileName : "kanji.csv",
                        RequestBody.create(data, MediaType.parse("text/csv")))
                .build();
        Request request = new Request.Builder()
                .url(baseUrl + "/api/kanji/import")
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = readBodyOrThrow(response);
            JSONObject obj = new JSONObject(body.isBlank() ? "{}" : body);
            BulkImportReportDto dto = new BulkImportReportDto();
            dto.setTotalRows(obj.optInt("totalRows"));
            dto.setKanjiInserted(obj.optInt("kanjiInserted"));
            dto.setKanjiUpdated(obj.optInt("kanjiUpdated"));
            dto.setQuestionsCreated(obj.optInt("questionsCreated"));
            dto.setChoicesCreated(obj.optInt("choicesCreated"));
            JSONArray errors = obj.optJSONArray("errors");
            if (errors != null) {
                for (int i = 0; i < errors.length(); i++) {
                    JSONObject e = errors.getJSONObject(i);
                    RowError error = new RowError();
                    error.setRowNumber(e.optInt("rowNumber"));
                    error.setMessage(e.optString("message"));
                    dto.getErrors().add(error);
                }
            }
            return dto;
        }
    }

    /**
     * Lấy danh sách Kanji theo level.
     *
     * @param levelId id level người dùng chọn.
     * @return danh sách Kanji tương ứng.
     * @throws Exception nếu request thất bại.
     */
    public List<KanjiDto> getKanjiByLevel(int levelId) throws Exception {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/kanji?levelId=" + levelId)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = readBodyOrThrow(response);
            JSONArray array = new JSONArray(body.isBlank() ? "[]" : body);
            List<KanjiDto> result = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                KanjiDto dto = new KanjiDto();
                dto.setId(obj.optLong("id"));
                dto.setCharacter(obj.optString("character"));
                dto.setHanViet(obj.optString("hanViet", null));
                dto.setOnReading(obj.optString("onReading", null));
                dto.setKunReading(obj.optString("kunReading", null));
                dto.setDescription(obj.optString("description", null));
                if (!obj.isNull("levelId")) {
                    dto.setLevelId(obj.optInt("levelId"));
                }
                result.add(dto);
            }
            return result;
        }
    }

    /**
     * Lấy danh sách bài học của một level.
     *
     * @param levelId id level.
     * @return danh sách bài học.
     * @throws Exception nếu request thất bại.
     */
    public List<LessonDto> getLessons(int levelId) throws Exception {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/lessons?levelId=" + levelId)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = readBodyOrThrow(response);
            JSONArray array = new JSONArray(body.isBlank() ? "[]" : body);
            List<LessonDto> result = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                LessonDto dto = new LessonDto();
                dto.setLessonId(obj.optLong("lessonId"));
                dto.setLevelId(obj.optInt("levelId"));
                dto.setTitle(obj.optString("title"));
                dto.setOverview(obj.optString("overview", null));
                dto.setOrderIndex(obj.optInt("orderIndex"));
                result.add(dto);
            }
            return result;
        }
    }

    /**
     * Tạo bài học mới.
     *
     * @param levelId    level chứa bài học.
     * @param title      tiêu đề bài học.
     * @param overview   tóm tắt nội dung.
     * @param orderIndex thứ tự hiển thị.
     * @return lesson vừa được tạo.
     * @throws Exception nếu request thất bại.
     */
    public LessonDto createLesson(int levelId, String title, String overview, int orderIndex) throws Exception {
        JSONObject payload = new JSONObject()
                .put("levelId", levelId)
                .put("title", title)
                .put("overview", overview)
                .put("orderIndex", orderIndex);
        Request request = new Request.Builder()
                .url(baseUrl + "/api/lessons")
                .post(RequestBody.create(payload.toString(), JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = readBodyOrThrow(response);
            JSONObject obj = new JSONObject(body.isBlank() ? "{}" : body);
            LessonDto dto = new LessonDto();
            dto.setLessonId(obj.optLong("lessonId"));
            dto.setLevelId(obj.optInt("levelId"));
            dto.setTitle(obj.optString("title"));
            dto.setOverview(obj.optString("overview", null));
            dto.setOrderIndex(obj.optInt("orderIndex"));
            return dto;
        }
    }

    /**
     * Cập nhật nội dung bài học.
     *
     * @param lessonId   khóa chính bài học.
     * @param levelId    level chứa bài học.
     * @param title      tiêu đề.
     * @param overview   mô tả tổng quan.
     * @param orderIndex thứ tự hiển thị.
     * @return {@link LessonDto} mới nhất.
     * @throws Exception nếu request thất bại.
     */
    public LessonDto updateLesson(long lessonId, int levelId, String title, String overview, int orderIndex) throws Exception {
        JSONObject payload = new JSONObject()
                .put("lessonId", lessonId)
                .put("levelId", levelId)
                .put("title", title)
                .put("overview", overview)
                .put("orderIndex", orderIndex);
        Request request = new Request.Builder()
                .url(baseUrl + "/api/lessons")
                .put(RequestBody.create(payload.toString(), JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = readBodyOrThrow(response);
            JSONObject obj = new JSONObject(body.isBlank() ? "{}" : body);
            LessonDto dto = new LessonDto();
            dto.setLessonId(obj.optLong("lessonId"));
            dto.setLevelId(obj.optInt("levelId"));
            dto.setTitle(obj.optString("title"));
            dto.setOverview(obj.optString("overview", null));
            dto.setOrderIndex(obj.optInt("orderIndex"));
            return dto;
        }
    }

    /**
     * Xóa bài học khỏi hệ thống.
     *
     * @param lessonId khóa chính cần xóa.
     * @throws Exception nếu request thất bại.
     */
    public void deleteLesson(long lessonId) throws Exception {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/lessons?lessonId=" + lessonId)
                .delete()
                .build();
        try (Response response = client.newCall(request).execute()) {
            readBodyOrThrow(response);
        }
    }

    /**
     * Tải danh sách câu hỏi luyện thi cho một bài học.
     *
     * @param lessonId bài học cần luyện.
     * @return danh sách câu hỏi cùng lựa chọn.
     * @throws Exception nếu request thất bại.
     */
    public List<QuizQuestionDto> getQuizByLesson(long lessonId) throws Exception {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/quiz/questions?lessonId=" + lessonId)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = readBodyOrThrow(response);
            JSONArray array = new JSONArray(body.isBlank() ? "[]" : body);
            List<QuizQuestionDto> result = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                QuizQuestionDto dto = new QuizQuestionDto();
                dto.setQuestionId(obj.optLong("questionId"));
                dto.setLessonId(obj.optLong("lessonId"));
                dto.setPrompt(obj.optString("prompt"));
                dto.setExplanation(obj.optString("explanation"));
                dto.setOrderIndex(obj.optInt("orderIndex"));

                JSONArray choices = obj.optJSONArray("choices");
                if (choices != null) {
                    for (int j = 0; j < choices.length(); j++) {
                        JSONObject c = choices.getJSONObject(j);
                        QuizChoiceDto choice = new QuizChoiceDto();
                        choice.setChoiceId(c.optLong("choiceId"));
                        choice.setQuestionId(c.optLong("questionId"));
                        choice.setContent(c.optString("content"));
                        choice.setCorrect(c.optBoolean("isCorrect"));
                        dto.getChoices().add(choice);
                    }
                }
                result.add(dto);
            }
            return result;
        }
    }

    /**
     * Tạo yêu cầu thanh toán MoMo cho gói VIP.
     *
     * @param planCode mã gói VIP (VIP_MONTHLY, VIP_QUARTERLY, VIP_YEARLY).
     * @return thông tin đơn hàng và URL thanh toán.
     * @throws Exception nếu request thất bại.
     */
    public MomoPaymentDto createMomoVipPayment(String planCode) throws Exception {
        JSONObject payload = new JSONObject();
        if (planCode != null && !planCode.isEmpty()) {
            payload.put("planCode", planCode);
        }
        Request request = new Request.Builder()
                .url(baseUrl + "/api/payments/momo")
                .post(RequestBody.create(payload.toString(), JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = readBodyOrThrow(response);
            JSONObject obj = new JSONObject(body.isBlank() ? "{}" : body);
            MomoPaymentDto dto = new MomoPaymentDto();
            dto.setOrderId(obj.optString("orderId"));
            dto.setRequestId(obj.optString("requestId"));
            dto.setPlanCode(obj.optString("planCode"));
            dto.setAmount(obj.optDouble("amount"));
            dto.setPayUrl(obj.optString("payUrl", null));
            dto.setDeeplink(obj.optString("deeplink", null));
            dto.setStatus(obj.optString("status", null));
            dto.setStubMode(obj.optBoolean("stubMode", false));
            return dto;
        }
    }

    /**
     * Kiểm tra trạng thái thanh toán MoMo.
     *
     * @param orderId mã đơn hàng trả về từ {@link #createMomoVipPayment(String)}.
     * @return trạng thái hiện tại của giao dịch.
     * @throws Exception nếu request thất bại.
     */
    public MomoPaymentStatusDto getMomoPaymentStatus(String orderId) throws Exception {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/payments/momo?orderId=" + orderId)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = readBodyOrThrow(response);
            JSONObject obj = new JSONObject(body.isBlank() ? "{}" : body);
            MomoPaymentStatusDto dto = new MomoPaymentStatusDto();
            dto.setOrderId(obj.optString("orderId"));
            dto.setPlanCode(obj.optString("planCode"));
            dto.setStatus(obj.optString("status"));
            dto.setAmount(obj.optDouble("amount"));
            dto.setPayUrl(obj.optString("payUrl", null));
            dto.setDeeplink(obj.optString("deeplink", null));
            if (!obj.isNull("resultCode")) {
                dto.setResultCode(obj.optInt("resultCode"));
            }
            dto.setMessage(obj.isNull("message") ? null : obj.optString("message", null));
            dto.setVipActivated(obj.optBoolean("vipActivated", false));
            dto.setVipExpiresAt(obj.isNull("vipExpiresAt") ? null : obj.optString("vipExpiresAt", null));
            return dto;
        }
    }

    /**
     * Lấy danh sách gói VIP và giá tương ứng để hiển thị trên dashboard admin.
     *
     * @return danh sách {@link VipPlanDto}.
     * @throws Exception nếu backend phản hồi lỗi.
     */
    public List<VipPlanDto> getVipPlans() throws Exception {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/admin/vip-plans")
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = readBodyOrThrow(response);
            JSONArray array = new JSONArray(body.isBlank() ? "[]" : body);
            List<VipPlanDto> plans = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                if (obj == null) {
                    continue;
                }
                VipPlanDto dto = new VipPlanDto();
                dto.setCode(obj.optString("code"));
                dto.setDescription(obj.optString("description"));
                dto.setAmount(obj.optDouble("amount", 0));
                plans.add(dto);
            }
            return plans;
        }
    }

    /**
     * Truy vấn danh sách hội viên phục vụ màn quản trị.
     *
     * @param filter bộ lọc cần áp dụng (null hoặc rỗng nghĩa là tất cả).
     * @return danh sách hội viên.
     * @throws Exception nếu request thất bại.
     */
    public List<AdminMemberDto> getAdminMembers(String filter) throws Exception {
        String url = baseUrl + "/api/admin/members";
        if (filter != null && !filter.isEmpty()) {
            url = url + "?filter=" + URLEncoder.encode(filter, StandardCharsets.UTF_8.name());
        }
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = readBodyOrThrow(response);
            JSONArray array = new JSONArray(body.isBlank() ? "[]" : body);
            List<AdminMemberDto> members = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                if (obj == null) {
                    continue;
                }
                AdminMemberDto dto = new AdminMemberDto();
                dto.setId(obj.optLong("id"));
                dto.setUserName(obj.optString("userName"));
                dto.setEmail(obj.optString("email", null));
                dto.setAvatarUrl(obj.optString("avatarUrl", null));
                dto.setAccountTier(obj.optString("accountTier", null));
                dto.setVipExpiresAt(obj.isNull("vipExpiresAt") ? null : obj.optString("vipExpiresAt"));
                dto.setHasPendingRequest(obj.optBoolean("hasPendingRequest", false));
                if (obj.isNull("requestId")) {
                    dto.setRequestId(null);
                } else {
                    dto.setRequestId(obj.optLong("requestId"));
                }
                dto.setRequestStatus(obj.optString("requestStatus", null));
                dto.setRequestNote(obj.optString("requestNote", null));
                dto.setRequestCreatedAt(obj.optString("requestCreatedAt", null));
                members.add(dto);
            }
            return members;
        }
    }

    private AccountUpgradeRequestDto parseUpgradeRequest(JSONObject obj) {
        AccountUpgradeRequestDto dto = new AccountUpgradeRequestDto();
        dto.setRequestId(obj.optLong("requestId"));
        dto.setUserId(obj.optLong("userId"));
        dto.setStatus(obj.optString("status", null));
        dto.setNote(obj.isNull("note") ? null : obj.optString("note", null));
        dto.setCreatedAt(obj.optString("createdAt", null));
        dto.setProcessedAt(obj.optString("processedAt", null));
        return dto;
    }

    private String readBodyOrThrow(Response response) throws IOException {
        String body = response.body() != null ? response.body().string() : "";
        if (!response.isSuccessful()) {
            throw new IllegalStateException(extractErrorMessage(response.code(), body));
        }
        return body;
    }

    private String extractErrorMessage(int statusCode, String body) {
        String defaultMessage = "HTTP " + statusCode;
        if (body == null || body.isBlank()) {
            return defaultMessage;
        }
        try {
            JSONObject json = new JSONObject(body);
            String message = json.optString("message", json.optString("error", defaultMessage));
            JSONObject details = json.optJSONObject("details");
            if (details != null && details.length() > 0) {
                message = message + " - " + details.toString();
            }
            return message;
        } catch (Exception ignored) {
            return body;
        }
    }
}
