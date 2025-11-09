package com.example.kanji_learning_sakura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.config.AuthPrefs;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

/**
 * Trang chủ sau khi đăng nhập.
 */
public class MainActivity extends AppCompatActivity {

    private AuthPrefs authPrefs;
    private TextView tvGreeting;
    private TextView tvRole;
    private TextView tvBalance;
    private ShapeableImageView imgAvatar;

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

        tvGreeting = findViewById(R.id.tvGreeting);
        tvRole = findViewById(R.id.tvRole);
        tvBalance = findViewById(R.id.tvDashboardBalance);
        MaterialButton btnViewProfile = findViewById(R.id.btnViewProfile);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        imgAvatar = findViewById(R.id.imgDashboardAvatar);
        MaterialCardView cardProfile = findViewById(R.id.cardProfile);
        MaterialCardView cardLearn = findViewById(R.id.cardLearn);
        MaterialCardView cardQuiz = findViewById(R.id.cardQuiz);
        MaterialCardView cardAdmin = findViewById(R.id.cardAdmin);

        refreshDashboard();

        btnLogout.setOnClickListener(v -> {
            authPrefs.clear();
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        });

        View.OnClickListener goProfile = v -> startActivity(new Intent(this, ProfileActivity.class));
        btnViewProfile.setOnClickListener(goProfile);
        cardProfile.setOnClickListener(goProfile);

        cardLearn.setOnClickListener(v -> startActivity(new Intent(this, KanjiBrowserActivity.class)));
        cardQuiz.setOnClickListener(v -> startActivity(new Intent(this, QuizSetupActivity.class)));

        if (authPrefs.roleId() == 1) {
            cardAdmin.setVisibility(View.VISIBLE);
            cardAdmin.setOnClickListener(v -> startActivity(new Intent(this, AdminKanjiActivity.class)));
        } else {
            cardAdmin.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (authPrefs != null && authPrefs.isLoggedIn()) {
            refreshDashboard();
        }
    }

    private void refreshDashboard() {
        String name = authPrefs.userName() != null ? authPrefs.userName() : "";
        tvGreeting.setText(getString(R.string.dashboard_greeting, name));
        tvRole.setText(roleName(authPrefs.roleId(), authPrefs.accountTier()));
        tvBalance.setText(getString(R.string.dashboard_balance_label, authPrefs.accountBalance()));

        String avatar = authPrefs.avatarUrl();
        if (avatar != null && !avatar.isEmpty()) {
            Picasso.get().load(avatar).placeholder(R.drawable.ic_launcher_foreground).into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    private String roleName(int roleId, String accountTier) {
        switch (roleId) {
            case 1:
                return getString(R.string.profile_role_admin);
            case 3:
                return getString(R.string.profile_role_vip);
            default:
                if ("VIP".equalsIgnoreCase(accountTier)) {
                    return getString(R.string.profile_role_vip);
                }
                return getString(R.string.profile_role_free);
        }
    }
}
