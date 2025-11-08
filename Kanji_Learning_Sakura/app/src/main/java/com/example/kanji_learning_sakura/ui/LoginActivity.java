package com.example.kanji_learning_sakura.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.auth.GoogleAuthHelper;
import com.example.kanji_learning_sakura.core.ApiClient;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private GoogleAuthHelper helper;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login);
        helper = new GoogleAuthHelper(this);

        Button btn = findViewById(R.id.btnGoogle);
        btn.setOnClickListener(v -> {
            String idToken = helper.getIdToken();
            if (idToken == null) {
                toast("Đăng nhập Google bị hủy/lỗi");
                return;
            }
            signInWithBackend(idToken);
        });
    }

    private void signInWithBackend(String idToken) {
        new Thread(() -> {
            try {
                OkHttpClient client = ApiClient.get(this);      // <-- SỬA: truyền Context
                String base = ApiClient.baseUrl(this);          // <-- SỬA: truyền Context

                String json = new JSONObject().put("idToken", idToken).toString();
                RequestBody body = RequestBody.create(
                        json, MediaType.parse("application/json; charset=utf-8"));

                Request req = new Request.Builder()
                        .url(base + "/api/auth/google")
                        .post(body)
                        .build();

                try (Response r = client.newCall(req).execute()) {
                    if (!r.isSuccessful()) {
                        runOnUiThread(() -> toast("Server trả lỗi: " + r.code()));
                        return;
                    }
                    String resp = r.body() != null ? r.body().string() : "{}";
                    JSONObject obj = new JSONObject(resp);
                    String jwt = obj.optString("token", null);
                    int roleId = obj.optInt("roleId", 2);

                    getSharedPreferences("auth", MODE_PRIVATE)
                            .edit()
                            .putString("jwt", jwt)
                            .putInt("roleId", roleId)
                            .apply();

                    runOnUiThread(() -> toast("Đăng nhập thành công! role=" + roleId));
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> toast("Lỗi mạng/JSON"));
            }
        }).start();
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}
