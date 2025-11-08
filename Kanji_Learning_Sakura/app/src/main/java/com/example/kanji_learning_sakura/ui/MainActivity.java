package com.example.kanji_learning_sakura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.config.AuthPrefs;
import com.google.android.material.card.MaterialCardView;

/**
 * Trang chủ sau khi đăng nhập.
 */
public class MainActivity extends AppCompatActivity {

    private AuthPrefs authPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authPrefs = new AuthPrefs(this);
        if (!authPrefs.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);

        TextView tvGreeting = findViewById(R.id.tvGreeting);
        TextView tvRole = findViewById(R.id.tvRole);
        Button btnLogout = findViewById(R.id.btnLogout);
        MaterialCardView cardLearn = findViewById(R.id.cardLearn);
        MaterialCardView cardQuiz = findViewById(R.id.cardQuiz);
        MaterialCardView cardAdmin = findViewById(R.id.cardAdmin);

        String name = authPrefs.userName() != null ? authPrefs.userName() : "";
        tvGreeting.setText(getString(R.string.dashboard_greeting, name));
        tvRole.setText(roleName(authPrefs.roleId()));

        btnLogout.setOnClickListener(v -> {
            authPrefs.clear();
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        });

        cardLearn.setOnClickListener(v -> startActivity(new Intent(this, KanjiBrowserActivity.class)));
        cardQuiz.setOnClickListener(v -> startActivity(new Intent(this, QuizSetupActivity.class)));

        if (authPrefs.roleId() == 1) {
            cardAdmin.setVisibility(View.VISIBLE);
            cardAdmin.setOnClickListener(v -> startActivity(new Intent(this, AdminKanjiActivity.class)));
        } else {
            cardAdmin.setVisibility(View.GONE);
        }
    }

    private String roleName(int roleId) {
        switch (roleId) {
            case 1:
                return "ADMIN";
            case 3:
                return "VIP";
            default:
                return "USER";
        }
    }
}
