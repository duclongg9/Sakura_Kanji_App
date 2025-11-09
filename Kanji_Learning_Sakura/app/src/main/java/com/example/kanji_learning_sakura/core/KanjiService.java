package com.example.kanji_learning_sakura.core;

import android.content.Context;
import com.example.kanji_learning_sakura.model.AuthResponseDto;
import com.example.kanji_learning_sakura.model.JlptLevelDto;
import com.example.kanji_learning_sakura.model.KanjiDto;
import com.example.kanji_learning_sakura.model.LessonDto;
import com.example.kanji_learning_sakura.model.LevelDto;
import com.example.kanji_learning_sakura.model.ProfileDto;
import com.example.kanji_learning_sakura.model.QuizChoiceDto;
import com.example.kanji_learning_sakura.model.QuizQuestionDto;
import com.example.kanji_learning_sakura.model.UpgradeRequestDto;
import com.example.kanji_learning_sakura.model.WalletDepositDto;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
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
            return dto;
        }
    }

    /**
     * Gửi yêu cầu nâng cấp tài khoản lên VIP.
     *
     * @param note ghi chú của người dùng.
     * @return {@link UpgradeRequestDto} phản hồi từ backend.
     * @throws Exception nếu request thất bại.
     */
    public UpgradeRequestDto createUpgradeRequest(String note) throws Exception {
        JSONObject payload = new JSONObject();
        if (note != null) {
            payload.put("note", note);
        }
        Request request = new Request.Builder()
                .url(baseUrl + "/api/account/upgrade-requests")
                .post(RequestBody.create(payload.toString(), JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalStateException("HTTP " + response.code());
            }
            JSONObject obj = new JSONObject(response.body() != null ? response.body().string() : "{}");
            UpgradeRequestDto dto = new UpgradeRequestDto();
            dto.setRequestId(obj.optLong("requestId"));
            dto.setStatus(obj.optString("status"));
            dto.setNote(obj.optString("note", null));
            dto.setTargetRoleId(obj.optInt("targetRoleId"));
            dto.setCreatedAt(obj.optString("createdAt", null));
            return dto;
        }
    }

    /**
     * Khởi tạo giao dịch nạp tiền và nhận về QR code tương ứng.
     *
     * @param amount số tiền muốn nạp.
     * @return {@link WalletDepositDto} chứa thông tin QR.
     * @throws Exception nếu request thất bại.
     */
    public WalletDepositDto createDeposit(double amount) throws Exception {
        JSONObject payload = new JSONObject().put("amount", amount);
        Request request = new Request.Builder()
                .url(baseUrl + "/api/wallet/deposits")
                .post(RequestBody.create(payload.toString(), JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                String message = "HTTP " + response.code();
                if (body != null && !body.isEmpty()) {
                    try {
                        JSONObject error = new JSONObject(body);
                        if (error.has("error")) {
                            message = error.optString("error", message);
                        }
                    } catch (Exception ignored) {
                        message = body;
                    }
                }
                throw new IllegalStateException(message);
            }
            JSONObject obj = new JSONObject(body.isEmpty() ? "{}" : body);
            WalletDepositDto dto = new WalletDepositDto();
            dto.setDepositId(obj.optLong("depositId"));
            dto.setAmount(obj.optDouble("amount"));
            dto.setStatus(obj.optString("status"));
            dto.setQrCodeUrl(obj.optString("qrCodeUrl"));
            dto.setCreatedAt(obj.optString("createdAt", null));
            return dto;
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
        Request request = new Request.Builder()
                .url(baseUrl + "/api/levels?jlptId=" + jlptId)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalStateException("HTTP " + response.code());
            }
            JSONArray array = new JSONArray(response.body() != null ? response.body().string() : "[]");
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
            if (!response.isSuccessful()) {
                throw new IllegalStateException("HTTP " + response.code());
            }
            JSONObject obj = new JSONObject(response.body() != null ? response.body().string() : "{}");
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
            if (!response.isSuccessful()) {
                throw new IllegalStateException("HTTP " + response.code());
            }
            JSONObject obj = new JSONObject(response.body() != null ? response.body().string() : "{}");
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
            if (!response.isSuccessful()) {
                throw new IllegalStateException("HTTP " + response.code());
            }
            JSONArray array = new JSONArray(response.body() != null ? response.body().string() : "[]");
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
            if (!response.isSuccessful()) {
                throw new IllegalStateException("HTTP " + response.code());
            }
            JSONArray array = new JSONArray(response.body() != null ? response.body().string() : "[]");
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
            if (!response.isSuccessful()) {
                throw new IllegalStateException("HTTP " + response.code());
            }
            JSONObject obj = new JSONObject(response.body() != null ? response.body().string() : "{}");
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
            if (!response.isSuccessful()) {
                throw new IllegalStateException("HTTP " + response.code());
            }
            JSONArray array = new JSONArray(response.body() != null ? response.body().string() : "[]");
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
}
