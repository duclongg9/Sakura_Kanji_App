package com.example.kanji_learning_sakura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.config.AuthPrefs;
import com.example.kanji_learning_sakura.core.KanjiService;
import com.example.kanji_learning_sakura.model.ProfileDto;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

/**
 * Màn hình hiển thị hồ sơ người dùng cùng các hành động nâng cấp, nạp tiền.
 */
public class ProfileActivity extends AppCompatActivity {

    private KanjiService service;
    private AuthPrefs authPrefs;
    private volatile boolean isLoading = false;
    private boolean hasLoadedOnce = false;
    private ShapeableImageView imgAvatar;
    private TextView tvName;
    private TextView tvEmail;
    private TextView tvTier;
    private TextView tvBalance;
    private TextView tvVipExpire;
    private TextView tvBio;
    private ProgressBar progressBar;
    private View content;
    private MaterialButton btnUpgrade;
    private MaterialButton btnDeposit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authPrefs = new AuthPrefs(this);
        if (!authPrefs.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_profile);

        service = new KanjiService(this);
        MaterialToolbar toolbar = findViewById(R.id.profileToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        imgAvatar = findViewById(R.id.imgAvatar);
        tvName = findViewById(R.id.tvProfileName);
        tvEmail = findViewById(R.id.tvProfileEmail);
        tvTier = findViewById(R.id.tvProfileTier);
        tvBalance = findViewById(R.id.tvProfileBalance);
        tvVipExpire = findViewById(R.id.tvVipExpire);
        tvBio = findViewById(R.id.tvProfileBio);
        progressBar = findViewById(R.id.profileProgress);
        content = findViewById(R.id.profileScroll);

        btnUpgrade = findViewById(R.id.btnUpgrade);
        btnDeposit = findViewById(R.id.btnDeposit);
        MaterialButton btnLogout = findViewById(R.id.btnLogoutProfile);

        btnUpgrade.setOnClickListener(v -> showUpgradeDialog());
        btnDeposit.setOnClickListener(v -> startActivity(new Intent(this, WalletDepositActivity.class)));
        btnLogout.setOnClickListener(v -> {
            authPrefs.clear();
            startActivity(new Intent(this, WelcomeActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finishAffinity();
        });

        bindCachedInfo();
        loadProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasLoadedOnce && !isLoading) {
            loadProfile();
        }
    }

    private void bindCachedInfo() {
        tvName.setText(authPrefs.userName());
        String cachedEmail = authPrefs.email() != null ? authPrefs.email() : "—";
        tvEmail.setText(getString(R.string.profile_email, cachedEmail));
        String avatar = authPrefs.avatarUrl();
        if (avatar != null && !avatar.isEmpty()) {
            Picasso.get().load(avatar).placeholder(R.drawable.ic_launcher_foreground).into(imgAvatar);
        }
        updateTierBadge(authPrefs.accountTier(), authPrefs.roleId());
        tvBalance.setText(getString(R.string.profile_balance, authPrefs.accountBalance()));
        tvVipExpire.setText(getString(R.string.profile_vip_unset));
        tvBio.setText("—");
    }

    private void loadProfile() {
        if (isLoading) {
            return;
        }
        isLoading = true;
        setLoading(true);
        new Thread(() -> {
            try {
                ProfileDto profile = service.getProfile();
                authPrefs.save(authPrefs.token(), profile.getRoleId(), profile.getId(), profile.getUserName(),
                        profile.getEmail(), profile.getAvatarUrl(), profile.getAccountTier(), profile.getAccountBalance(),
                        profile.getVipExpiresAt(), profile.getBio());
                runOnUiThread(() -> renderProfile(profile));
            } catch (IllegalStateException ex) {
                runOnUiThread(() -> {
                    setLoading(false);
                    toast(ex.getMessage());
                    isLoading = false;
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    setLoading(false);
                    toast(getString(R.string.msg_network_error));
                    isLoading = false;
                });
            }
        }).start();
    }

    private void renderProfile(ProfileDto profile) {
        setLoading(false);
        isLoading = false;
        hasLoadedOnce = true;
        tvName.setText(profile.getUserName());
        String email = profile.getEmail() != null ? profile.getEmail() : "—";
        tvEmail.setText(getString(R.string.profile_email, email));
        updateTierBadge(profile.getAccountTier(), profile.getRoleId());
        tvBalance.setText(getString(R.string.profile_balance, profile.getAccountBalance()));
        if (profile.getVipExpiresAt() != null) {
            tvVipExpire.setText(getString(R.string.profile_vip_exp, profile.getVipExpiresAt()));
        } else {
            tvVipExpire.setText(getString(R.string.profile_vip_unset));
        }
        if (profile.getBio() != null && !profile.getBio().isEmpty()) {
            tvBio.setText(profile.getBio());
        } else {
            tvBio.setText("—");
        }
        if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isEmpty()) {
            Picasso.get().load(profile.getAvatarUrl()).placeholder(R.drawable.ic_launcher_foreground).into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.ic_launcher_foreground);
        }
        boolean isVip = "VIP".equalsIgnoreCase(profile.getAccountTier()) || profile.getRoleId() == 3;
        btnUpgrade.setEnabled(!isVip && profile.getRoleId() != 1);
        btnDeposit.setEnabled(true);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        content.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
    }

    private void updateTierBadge(String accountTier, int roleId) {
        String display;
        if (roleId == 1) {
            display = getString(R.string.profile_role_admin);
        } else if ("VIP".equalsIgnoreCase(accountTier) || roleId == 3) {
            display = getString(R.string.profile_role_vip);
        } else {
            display = getString(R.string.profile_role_free);
        }
        tvTier.setText(getString(R.string.profile_account_tier, display));
    }

    private void showUpgradeDialog() {
        TextInputLayout layout = (TextInputLayout) getLayoutInflater().inflate(R.layout.view_upgrade_note, null);
        TextInputEditText edtNote = layout.findViewById(R.id.edtUpgradeNote);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_upgrade_title)
                .setView(layout)
                .setNegativeButton(R.string.label_cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.label_send, (dialog, which) -> {
                    String note = edtNote.getText() != null ? edtNote.getText().toString().trim() : null;
                    sendUpgradeRequest(note);
                })
                .show();
    }

    private void sendUpgradeRequest(String note) {
        btnUpgrade.setEnabled(false);
        setLoading(true);
        new Thread(() -> {
            try {
                service.createUpgradeRequest(note);
                runOnUiThread(() -> {
                    setLoading(false);
                    toast(getString(R.string.msg_upgrade_success));
                });
            } catch (IllegalStateException ex) {
                runOnUiThread(() -> {
                    setLoading(false);
                    if (ex.getMessage() != null && ex.getMessage().contains("409")) {
                        toast(getString(R.string.msg_upgrade_pending));
                    } else {
                        toast(getString(R.string.msg_upgrade_error, ex.getMessage()));
                        btnUpgrade.setEnabled(true);
                    }
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    setLoading(false);
                    toast(getString(R.string.msg_upgrade_error, ex.getMessage()));
                    btnUpgrade.setEnabled(true);
                });
            }
        }).start();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
