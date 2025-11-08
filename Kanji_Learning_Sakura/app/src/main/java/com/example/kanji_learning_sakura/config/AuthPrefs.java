package com.example.kanji_learning_sakura.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.kanji_learning_sakura.model.User;

public class AuthPrefs {
    private static final String FILE = "auth";
    private static final String K_TOKEN = "jwt";
    private static final String K_ROLE  = "roleId";
    private static final String K_USER_ID = "userId";
    private static final String K_USER_NAME = "userName";

    private final SharedPreferences sp;
    public AuthPrefs(Context ctx) { sp = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE); }

    /**
     * Lưu JWT trả về từ backend.
     */
    public void save(String jwt, int roleId) {
        sp.edit()
                .putString(K_TOKEN, jwt)
                .putInt(K_ROLE, roleId)
                .remove(K_USER_ID)
                .remove(K_USER_NAME)
                .apply();
    }

    /**
     * Lưu thông tin người dùng đăng nhập local (JDBC).
     */
    public void saveLocalUser(User user) {
        sp.edit()
                .remove(K_TOKEN)
                .putLong(K_USER_ID, user.getId())
                .putString(K_USER_NAME, user.getUserName())
                .putInt(K_ROLE, user.getRoleId())
                .apply();
    }

    public String token() { return sp.getString(K_TOKEN, null); }
    public int roleId() { return sp.getInt(K_ROLE, 2); }  // 2 = USER
    public long userId() { return sp.getLong(K_USER_ID, -1L); }
    public String userName() { return sp.getString(K_USER_NAME, null); }
    public void clear() { sp.edit().clear().apply(); }
}
