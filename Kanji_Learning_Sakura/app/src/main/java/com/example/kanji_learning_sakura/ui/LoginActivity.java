package com.example.kanji_learning_sakura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.config.AuthPrefs;
import com.example.kanji_learning_sakura.core.ApiClient;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private AuthPrefs authPrefs;
    private TextInputEditText edtIdentifier;
    private TextInputEditText edtPassword;
    private Button btnLocalLogin;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login);
        authPrefs = new AuthPrefs(this);

        edtIdentifier = findViewById(R.id.edtIdentifier);
        edtPassword = findViewById(R.id.edtPassword);
        btnLocalLogin = findViewById(R.id.btnLogin);

        btnLocalLogin.setOnClickListener(v -> attemptBackendLogin());
    }

    /**
     * Gửi yêu cầu đăng nhập tới backend qua HTTP.
     */
    private void attemptBackendLogin() {
        String identifier = textOf(edtIdentifier);
        String password = textOf(edtPassword);

        edtIdentifier.setError(null);
        edtPassword.setError(null);

        if (identifier.isEmpty()) {
            edtIdentifier.setError(getString(R.string.error_required_identifier));
            edtIdentifier.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            edtPassword.setError(getString(R.string.error_required_password));
            edtPassword.requestFocus();
            return;
        }

        setLoadingState(true);
        String baseUrl = ApiClient.baseUrl(this);
        new Thread(() -> {
            try {
                OkHttpClient client = ApiClient.get(getApplicationContext());
                JSONObject jsonBody = new JSONObject()
                        .put("email", identifier)
                        .put("password", password);

                Request request = new Request.Builder()
                        .url(baseUrl + "/api/auth/login")
                        .post(RequestBody.create(jsonBody.toString(), JSON))
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    handleLoginResponse(identifier, response);
                }
            } catch (IOException ioe) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    toast(getString(R.string.msg_network_error));
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    toast(getString(R.string.msg_unexpected_error));
                });
            }
        }).start();
    }

    /**
     * Phản hồi kết quả đăng nhập từ backend.
     */
    private void handleLoginResponse(String identifier, Response response) throws Exception {
        if (!response.isSuccessful()) {
            String body = response.body() != null ? response.body().string() : "";
            String detail = body.isEmpty() ? getString(R.string.msg_login_fail_unknown) : body;
            runOnUiThread(() -> {
                setLoadingState(false);
                if (response.code() == 401) {
                    toast(getString(R.string.msg_login_fail));
                } else {
                    toast(getString(R.string.msg_login_fail_http, response.code(), detail));
                }
            });
            return;
        }

        JSONObject payload = new JSONObject(response.body() != null ? response.body().string() : "{}");
        String token = payload.optString("token", null);
        int roleId = payload.optInt("roleId", 2);

        authPrefs.save(token, roleId);

        runOnUiThread(() -> {
            setLoadingState(false);
            toast(getString(R.string.msg_login_success, identifier));
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    /**
     * Bật/tắt nút khi đang xử lý đăng nhập.
     */
    private void setLoadingState(boolean loading) {
        btnLocalLogin.setEnabled(!loading);
    }

    /**
     * Lấy text đã trim từ {@link TextInputEditText}.
     */
    private String textOf(TextInputEditText editText) {
        CharSequence text = editText.getText();
        return text == null ? "" : text.toString().trim();
    }

    /**
     * Hiển thị thông báo nhanh cho người dùng.
     */
    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}
