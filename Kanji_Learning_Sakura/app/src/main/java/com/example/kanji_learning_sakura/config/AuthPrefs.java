package com.example.kanji_learning_sakura.config;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Wrapper nhỏ gọn cho SharedPreferences để lưu JWT và thông tin người dùng đăng nhập.
 */
public class AuthPrefs {
    private static final String FILE = "auth";
    private static final String K_TOKEN = "jwt";
    private static final String K_ROLE  = "roleId";
    private static final String K_USER_ID = "userId";
    private static final String K_USER_NAME = "userName";

    private final SharedPreferences sp;

    /**
     * @param ctx context dùng để mở file SharedPreferences.
     */
    public AuthPrefs(Context ctx) { sp = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE); }

    /**
     * Lưu token và thông tin cơ bản của người dùng.
     */
    public void save(String jwt, int roleId, long userId, String userName) {
        sp.edit()
                .putString(K_TOKEN, jwt)
                .putInt(K_ROLE, roleId)
                .putLong(K_USER_ID, userId)
                .putString(K_USER_NAME, userName)
                .apply();
    }

    /** @return JWT hiện lưu hoặc {@code null} nếu chưa đăng nhập. */
    public String token() { return sp.getString(K_TOKEN, null); }

    /** @return roleId đã lưu, mặc định 2 = USER. */
    public int roleId() { return sp.getInt(K_ROLE, 2); }

    /** @return id người dùng đã lưu hoặc -1 nếu chưa có. */
    public long userId() { return sp.getLong(K_USER_ID, -1L); }

    /** @return tên hiển thị của người dùng hiện tại. */
    public String userName() { return sp.getString(K_USER_NAME, null); }

    /** @return {@code true} nếu đã có token hợp lệ. */
    public boolean isLoggedIn() { return token() != null; }

    /** Xóa toàn bộ thông tin đăng nhập. */
    public void clear() { sp.edit().clear().apply(); }
}
