package com.example.kanji_learning_sakura.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.core.ApiClient;
import okhttp3.*;

public class MainActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        TextView tv = new TextView(this);
        tv.setText("Loading /api/auth/me ...");
        setContentView(tv);

        new Thread(() -> {
            try {
                OkHttpClient client = ApiClient.get(this);
                String base = ApiClient.baseUrl(this);
                Request req = new Request.Builder().url(base + "/api/auth/me").get().build();
                try (Response r = client.newCall(req).execute()) {
                    String s = r.body() != null ? r.body().string() : ("HTTP " + r.code());
                    runOnUiThread(() -> tv.setText(s));
                }
            } catch (Exception e) {
                runOnUiThread(() -> tv.setText("Lỗi: " + e.getMessage()));
            }
        }).start();
    }
}
