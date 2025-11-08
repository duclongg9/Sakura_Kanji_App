package com.example.kanji_learning_sakura.core;

import android.content.Context;
import com.example.kanji_learning_sakura.config.AuthPrefs;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor tự động gắn header Authorization vào mỗi request nếu người dùng đã đăng nhập.
 */
public class AuthInterceptor implements Interceptor {
    private final AuthPrefs prefs;

    /**
     * @param ctx context dùng để khởi tạo SharedPreferences lưu token.
     */
    public AuthInterceptor(Context ctx) { this.prefs = new AuthPrefs(ctx); }

    @Override public Response intercept(Chain chain) throws IOException {
        String jwt = prefs.token();
        Request origin = chain.request();
        if (jwt == null) return chain.proceed(origin);
        Request withAuth = origin.newBuilder().header("Authorization", "Bearer " + jwt).build();
        return chain.proceed(withAuth);
    }
}
