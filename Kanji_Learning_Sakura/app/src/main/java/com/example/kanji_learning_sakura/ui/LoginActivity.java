package com.example.kanji_learning_sakura.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.config.AuthPrefs;
import com.example.kanji_learning_sakura.data.dao.UserDAO;
import com.example.kanji_learning_sakura.model.User;
import com.google.android.material.textfield.TextInputEditText;

import java.sql.SQLException;

public class LoginActivity extends AppCompatActivity {

    private UserDAO userDAO;
    private AuthPrefs authPrefs;
    private TextInputEditText edtIdentifier;
    private TextInputEditText edtPassword;
    private Button btnLocalLogin;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login);
        userDAO = new UserDAO();
        authPrefs = new AuthPrefs(this);

        edtIdentifier = findViewById(R.id.edtIdentifier);
        edtPassword = findViewById(R.id.edtPassword);
        btnLocalLogin = findViewById(R.id.btnLogin);

        btnLocalLogin.setOnClickListener(v -> attemptLocalLogin());
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
                User user = userDAO.authenticate(identifier, password);
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
