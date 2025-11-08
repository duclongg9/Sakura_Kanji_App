package com.example.kanji_learning_sakura.core;

import android.content.Context;
import okhttp3.OkHttpClient;

public final class ApiClient {
    private ApiClient() {}
    private static OkHttpClient client;

    /** OkHttp có AuthInterceptor gắn JWT tự động */
    public static OkHttpClient get(Context ctx) {
        if (client == null) {
            // dùng applicationContext để tránh leak
            Context appCtx = ctx.getApplicationContext();
            client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(appCtx))
                    .build();
        }
        return client;
    }

    /** Lấy base URL từ strings.xml */
    public static String baseUrl(Context ctx) {
        int id = ctx.getResources().getIdentifier("backend_base_url","string", ctx.getPackageName());
        return ctx.getString(id);
    }
}
