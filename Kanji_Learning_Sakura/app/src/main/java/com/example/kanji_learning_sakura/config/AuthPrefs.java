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
    private static final String K_EMAIL = "email";
    private static final String K_AVATAR = "avatarUrl";
    private static final String K_TIER = "accountTier";
    private static final String K_BALANCE = "accountBalance";
    private static final String K_VIP_EXPIRES = "vipExpiresAt";
    private static final String K_BIO = "bio";

    private final SharedPreferences sp;

    /**
     * @param ctx context dùng để mở file SharedPreferences.
     */
    public AuthPrefs(Context ctx) { sp = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE); }

    /**
     * Lưu token và thông tin cơ bản của người dùng.
     */
    public void save(String jwt, int roleId, long userId, String userName,
                     String email, String avatarUrl, String accountTier, double balance,
                     String vipExpiresAt, String bio) {
        sp.edit()
                .putString(K_TOKEN, jwt)
                .putInt(K_ROLE, roleId)
                .putLong(K_USER_ID, userId)
                .putString(K_USER_NAME, userName)
                .putString(K_EMAIL, email)
                .putString(K_AVATAR, avatarUrl)
                .putString(K_TIER, accountTier)
                .putFloat(K_BALANCE, (float) balance)
                .putString(K_VIP_EXPIRES, vipExpiresAt)
                .putString(K_BIO, bio)
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

    /** @return email của người dùng. */
    public String email() { return sp.getString(K_EMAIL, null); }

    /** @return ảnh đại diện lưu gần nhất. */
    public String avatarUrl() { return sp.getString(K_AVATAR, null); }

    /** @return cấp độ tài khoản (FREE/VIP/ADMIN). */
    public String accountTier() { return sp.getString(K_TIER, "FREE"); }

    /** @return số dư ví hiện tại. */
    public double accountBalance() { return sp.getFloat(K_BALANCE, 0f); }

    /** @return thời điểm hết hạn VIP nếu có. */
    public String vipExpiresAt() { return sp.getString(K_VIP_EXPIRES, null); }

    /** @return ghi chú/bio của người dùng. */
    public String bio() { return sp.getString(K_BIO, null); }

    /** @return {@code true} nếu đã có token hợp lệ. */
    public boolean isLoggedIn() { return token() != null; }

    /** Xóa toàn bộ thông tin đăng nhập. */
    public void clear() { sp.edit().clear().apply(); }
}
