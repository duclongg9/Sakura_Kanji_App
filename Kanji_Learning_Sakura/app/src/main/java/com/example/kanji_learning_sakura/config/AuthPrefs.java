package com.example.kanji_learning_sakura.config;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthPrefs {
    private static final String FILE = "auth";
    private static final String K_TOKEN = "jwt";
    private static final String K_ROLE  = "roleId";

    private final SharedPreferences sp;
    public AuthPrefs(Context ctx) { sp = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE); }

    public void save(String jwt, int roleId) { sp.edit().putString(K_TOKEN, jwt).putInt(K_ROLE, roleId).apply(); }
    public String token() { return sp.getString(K_TOKEN, null); }
    public int roleId() { return sp.getInt(K_ROLE, 2); }  // 2 = USER
    public void clear() { sp.edit().clear().apply(); }
}
