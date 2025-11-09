package com.example.kanji_learning_sakura.core;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import com.example.kanji_learning_sakura.R;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Khởi tạo và cung cấp {@link OkHttpClient} dùng chung cho toàn ứng dụng.
 * <p>
 * Interceptor đăng nhập được gắn tại đây để mọi request đều tự động đính kèm JWT khi có.
 */
public final class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String FALLBACK_BASE_URL = "http://10.0.2.2:9999/kanji-auth-backend";
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
     * Đọc base URL backend từ cấu hình build hoặc resource. Ưu tiên BuildConfig để hỗ trợ
     * cấu hình môi trường động, đồng thời fallback sang string resource để giữ tương thích
     * với cấu hình hiện tại. Nếu cả hai đều thiếu thì ném lỗi có thông báo rõ ràng nhằm tránh
     * crash không rõ nguyên nhân khi admin mở màn hình quản trị.
     *
     * @param ctx context sử dụng để truy cập resource.
     * @return chuỗi URL backend.
     */
    public static String baseUrl(Context ctx) {
        // Ưu tiên giá trị từ BuildConfig nếu đã được cấu hình.

        Context appCtx = ctx.getApplicationContext();
        try {
            String resourceValue = appCtx.getString(R.string.backend_base_url);
            if (resourceValue != null && !resourceValue.trim().isEmpty()) {
                return resourceValue;
            }

            Log.w(TAG, "backend_base_url is empty, using fallback URL instead");
        } catch (Resources.NotFoundException ex) {
            Log.w(TAG, "Missing backend_base_url string resource, using fallback URL", ex);
        }
        return FALLBACK_BASE_URL;
    }
}
