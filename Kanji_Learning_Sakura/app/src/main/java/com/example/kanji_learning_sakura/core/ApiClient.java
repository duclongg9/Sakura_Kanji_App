package com.example.kanji_learning_sakura.core;

import android.content.Context;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public final class ApiClient {
    private ApiClient() {}
    private static OkHttpClient client;

    /** OkHttp có AuthInterceptor gắn JWT tự động */
    public static OkHttpClient get(Context ctx) {
        if (client == null) {
            synchronized (ApiClient.class) {
                if (client == null) {
                    // dùng applicationContext để tránh leak
                    Context appCtx = ctx.getApplicationContext();
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                    client = new OkHttpClient.Builder()
                            .addInterceptor(new AuthInterceptor(appCtx))
                            .addInterceptor(logging)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .build();
                }
            }
        }
        return client;
    }

    /** Lấy base URL từ strings.xml */
    public static String baseUrl(Context ctx) {
        int id = ctx.getResources().getIdentifier("backend_base_url","string", ctx.getPackageName());
        return ctx.getString(id);
    }
}
