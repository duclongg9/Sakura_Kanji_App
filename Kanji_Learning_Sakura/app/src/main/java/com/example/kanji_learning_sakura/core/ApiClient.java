package com.example.kanji_learning_sakura.core;

import android.content.Context;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Khởi tạo và cung cấp {@link OkHttpClient} dùng chung cho toàn ứng dụng.
 * <p>
 * Interceptor đăng nhập được gắn tại đây để mọi request đều tự động đính kèm JWT khi có.
 */
public final class ApiClient {
    private ApiClient() {}
    private static OkHttpClient client;

    /**
     * Lấy client HTTP có cấu hình interceptor và timeout mặc định.
     *
     * @param ctx context bất kỳ, phương thức sẽ tự động dùng applicationContext để tránh leak.
     * @return {@link OkHttpClient} singleton.
     */
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

    /**
     * Đọc base URL backend từ file strings.xml.
     *
     * @param ctx context sử dụng để truy cập resource.
     * @return chuỗi URL backend.
     */
    public static String baseUrl(Context ctx) {
        int id = ctx.getResources().getIdentifier("backend_base_url","string", ctx.getPackageName());
        return ctx.getString(id);
    }
}
