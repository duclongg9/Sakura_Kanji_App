package app.api.support;

import org.json.JSONObject;

/**
 * Bộ tiện ích hỗ trợ validate và chuyển đổi dữ liệu JSON cho các servlet quản trị.
 */
public final class JsonValidationUtils {

    private JsonValidationUtils() {
    }

    /**
     * Chuẩn hóa chuỗi thành dạng đã trim. Nếu chuỗi rỗng sau khi trim thì trả về {@code null}.
     *
     * @param value chuỗi đầu vào.
     * @return chuỗi đã trim hoặc {@code null} nếu trống/không có.
     */
    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Đọc một trường string bắt buộc từ JSON, tự động trim và validate rỗng.
     *
     * @param payload dữ liệu JSON.
     * @param key     khóa cần đọc.
     * @param errors  đối tượng chứa lỗi để cộng dồn.
     * @return chuỗi đã hợp lệ hoặc {@code null} nếu có lỗi.
     */
    public static String readRequiredString(JSONObject payload, String key, JSONObject errors) {
        if (!payload.has(key)) {
            errors.put(key, "is required");
            return null;
        }
        Object raw = payload.opt(key);
        if (raw == null || raw == JSONObject.NULL) {
            errors.put(key, "is required");
            return null;
        }
        String value = trimToNull(String.valueOf(raw));
        if (value == null) {
            errors.put(key, "must not be blank");
            return null;
        }
        return value;
    }

    /**
     * Đọc một trường string optional từ JSON, trim và chuyển thành {@code null} nếu trống.
     *
     * @param payload dữ liệu JSON.
     * @param key     khóa cần đọc.
     * @return chuỗi đã trim hoặc {@code null} nếu không tồn tại/không hợp lệ.
     */
    public static String readOptionalString(JSONObject payload, String key) {
        if (!payload.has(key)) {
            return null;
        }
        Object raw = payload.opt(key);
        if (raw == null || raw == JSONObject.NULL) {
            return null;
        }
        return trimToNull(String.valueOf(raw));
    }

    /**
     * Đọc một trường string optional nhưng bắt buộc không rỗng khi đã cung cấp.
     *
     * @param payload dữ liệu JSON.
     * @param key     khóa cần đọc.
     * @param errors  nơi ghi nhận lỗi.
     * @return chuỗi hợp lệ hoặc {@code null} nếu không tồn tại/không hợp lệ.
     */
    public static String readOptionalNonBlankString(JSONObject payload, String key, JSONObject errors) {
        if (!payload.has(key)) {
            return null;
        }
        Object raw = payload.opt(key);
        if (raw == null || raw == JSONObject.NULL) {
            errors.put(key, "must not be null");
            return null;
        }
        String value = trimToNull(String.valueOf(raw));
        if (value == null) {
            errors.put(key, "must not be blank");
            return null;
        }
        return value;
    }

    /**
     * Chuyển bất kỳ object sang chuỗi nullable, trim và trả null nếu rỗng.
     *
     * @param raw giá trị đọc được từ JSON.
     * @return chuỗi đã trim hoặc {@code null}.
     */
    public static String toNullableString(Object raw) {
        if (raw == null || raw == JSONObject.NULL) {
            return null;
        }
        return trimToNull(String.valueOf(raw));
    }

    /**
     * Đọc số nguyên dương (>0). Nếu trường bắt buộc thì báo lỗi khi thiếu/không hợp lệ.
     *
     * @param raw       giá trị đọc từ JSON (có thể null).
     * @param fieldName tên field dùng trong thông báo lỗi.
     * @param allowNull cho phép null hay không.
     * @param errors    nơi ghi nhận lỗi.
     * @return số nguyên dương hoặc {@code null} nếu không hợp lệ.
     */
    public static Integer readPositiveInt(Object raw, String fieldName, boolean allowNull, JSONObject errors) {
        if (raw == null || raw == JSONObject.NULL) {
            if (!allowNull) {
                errors.put(fieldName, "is required");
            }
            return null;
        }
        if (raw instanceof Number) {
            int value = ((Number) raw).intValue();
            if (value <= 0) {
                errors.put(fieldName, "must be greater than 0");
                return null;
            }
            return value;
        }
        errors.put(fieldName, "must be a number");
        return null;
    }

    /**
     * Đọc số nguyên không âm (>=0).
     *
     * @param raw       giá trị đọc từ JSON.
     * @param fieldName tên field dùng trong thông báo lỗi.
     * @param allowNull cho phép null hay không.
     * @param errors    nơi ghi nhận lỗi.
     * @return giá trị hợp lệ hoặc {@code null} nếu có lỗi.
     */
    public static Integer readNonNegativeInt(Object raw, String fieldName, boolean allowNull, JSONObject errors) {
        if (raw == null || raw == JSONObject.NULL) {
            if (!allowNull) {
                errors.put(fieldName, "is required");
            }
            return null;
        }
        if (raw instanceof Number) {
            int value = ((Number) raw).intValue();
            if (value < 0) {
                errors.put(fieldName, "must be zero or positive");
                return null;
            }
            return value;
        }
        errors.put(fieldName, "must be a number");
        return null;
    }

    /**
     * Đọc số nguyên dương dạng long (>0).
     *
     * @param raw       giá trị đọc từ JSON.
     * @param fieldName tên field dùng trong thông báo lỗi.
     * @param allowNull cho phép null hay không.
     * @param errors    nơi ghi nhận lỗi.
     * @return giá trị hợp lệ hoặc {@code null} nếu có lỗi.
     */
    public static Long readPositiveLong(Object raw, String fieldName, boolean allowNull, JSONObject errors) {
        if (raw == null || raw == JSONObject.NULL) {
            if (!allowNull) {
                errors.put(fieldName, "is required");
            }
            return null;
        }
        if (raw instanceof Number) {
            long value = ((Number) raw).longValue();
            if (value <= 0L) {
                errors.put(fieldName, "must be greater than 0");
                return null;
            }
            return value;
        }
        errors.put(fieldName, "must be a number");
        return null;
    }

    /**
     * Đọc giá trị boolean. Chấp nhận định dạng boolean, số (0/1) hoặc chuỗi "true"/"false".
     *
     * @param raw       giá trị đọc từ JSON.
     * @param fieldName tên field dùng trong thông báo lỗi.
     * @param allowNull cho phép null hay không.
     * @param errors    nơi ghi nhận lỗi.
     * @return giá trị boolean hợp lệ hoặc {@code null} nếu có lỗi.
     */
    public static Boolean readBoolean(Object raw, String fieldName, boolean allowNull, JSONObject errors) {
        if (raw == null || raw == JSONObject.NULL) {
            if (!allowNull) {
                errors.put(fieldName, "is required");
            }
            return null;
        }
        if (raw instanceof Boolean) {
            return (Boolean) raw;
        }
        if (raw instanceof Number) {
            return ((Number) raw).intValue() != 0;
        }
        if (raw instanceof String) {
            String value = ((String) raw).trim().toLowerCase();
            if ("true".equals(value)) {
                return Boolean.TRUE;
            }
            if ("false".equals(value)) {
                return Boolean.FALSE;
            }
        }
        errors.put(fieldName, "must be boolean");
        return null;
    }
}
