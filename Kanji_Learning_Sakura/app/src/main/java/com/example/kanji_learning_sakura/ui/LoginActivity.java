package com.example.kanji_learning_sakura.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.auth.GoogleAuthHelper;
import com.example.kanji_learning_sakura.config.AuthPrefs;
import com.example.kanji_learning_sakura.core.ApiClient;
import com.example.kanji_learning_sakura.data.dao.UserDao;
import com.example.kanji_learning_sakura.model.User;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.sql.SQLException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private GoogleAuthHelper helper;
    private UserDao userDao;
    private AuthPrefs authPrefs;
    private TextInputEditText edtIdentifier;
    private TextInputEditText edtPassword;
    private Button btnLocalLogin;
    private Button btnGoogle;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login);
        helper = new GoogleAuthHelper(this);
        userDao = new UserDao();
        authPrefs = new AuthPrefs(this);

        edtIdentifier = findViewById(R.id.edtIdentifier);
        edtPassword = findViewById(R.id.edtPassword);
        btnLocalLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);

        btnLocalLogin.setOnClickListener(v -> attemptLocalLogin());

        btnGoogle.setOnClickListener(v -> {
            String idToken = helper.getIdToken();
            if (idToken == null) {
                toast("Đăng nhập Google bị hủy/lỗi");
                return;
            }
            signInWithBackend(idToken);
        });
    }

    /**
     * Đăng nhập tài khoản local từ MySQL bằng JDBC.
     */
    private void attemptLocalLogin() {
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
        new Thread(() -> {
            try {
                User user = userDao.authenticate(identifier, password);
                runOnUiThread(() -> {
                    setLoadingState(false);
                    if (user == null) {
                        toast(getString(R.string.msg_login_fail));
                    } else {
                        authPrefs.saveLocalUser(user);
                        toast(getString(R.string.msg_login_success, user.getUserName()));
                    }
                });
            } catch (ClassNotFoundException e) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    toast(getString(R.string.msg_driver_missing));
                });
            } catch (SQLException e) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    toast(getString(R.string.msg_db_error, e.getMessage()));
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
     * Gọi API backend để xác thực Google Sign-In và nhận JWT.
     */
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

                    authPrefs.save(jwt, roleId);

                    runOnUiThread(() -> toast("Đăng nhập thành công! role=" + roleId));
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> toast("Lỗi mạng/JSON"));
            }
        }).start();
    }

    /**
     * Bật/tắt nút khi đang xử lý đăng nhập.
     */
    private void setLoadingState(boolean loading) {
        btnLocalLogin.setEnabled(!loading);
        btnGoogle.setEnabled(!loading);
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
