package com.example.kanji_learning_sakura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.config.AuthPrefs;
import com.example.kanji_learning_sakura.core.KanjiService;
import com.example.kanji_learning_sakura.model.AuthResponseDto;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.material.textfield.TextInputEditText;
import java.io.IOException;

/**
 * Màn hình đăng nhập local hoặc qua Google.
 */
public class LoginActivity extends AppCompatActivity {

    private AuthPrefs authPrefs;
    private KanjiService service;
    private TextInputEditText edtIdentifier;
    private TextInputEditText edtPassword;
    private Button btnLocalLogin;
    private Button btnGoogle;
    private GoogleSignInClient googleClient;

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // Lưu lại dữ liệu trả về để xử lý thông báo phù hợp thay vì báo hủy mọi trường hợp.
                Intent data = result.getData();
                if (data == null) {
                    if (result.getResultCode() == RESULT_CANCELED) {
                        toast(getString(R.string.msg_google_cancel));
                    } else {
                        toast(getString(R.string.msg_google_fail,
                                "Missing intent data (code=" + result.getResultCode() + ")"));
                    }
                    return;
                }
                try {
                    GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                            .getResult(ApiException.class);
                    if (account != null) {
                        String idToken = account.getIdToken();
                        if (idToken == null) {
                            toast(getString(R.string.msg_google_fail, "Missing idToken"));
                            return;
                        }
                        handleGoogleLogin(idToken);
                    }
                } catch (ApiException ex) {
                    if (ex.getStatusCode() == CommonStatusCodes.CANCELED) {
                        toast(getString(R.string.msg_google_cancel));
                    } else {
                        toast(getString(R.string.msg_google_fail, ex.getMessage()));
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authPrefs = new AuthPrefs(this);
        if (authPrefs.isLoggedIn()) {
            goToMain();
            return;
        }
        setContentView(R.layout.activity_login);
        service = new KanjiService(this);

        edtIdentifier = findViewById(R.id.edtIdentifier);
        edtPassword = findViewById(R.id.edtPassword);
        btnLocalLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);

        btnLocalLogin.setOnClickListener(v -> attemptBackendLogin());
        btnGoogle.setOnClickListener(v -> startGoogleLogin());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.web_client_id))
                .build();
        googleClient = GoogleSignIn.getClient(this, gso);
    }

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
        new Thread(() -> {
            try {
                AuthResponseDto dto = service.login(identifier, password);
                authPrefs.save(dto.getToken(), dto.getRoleId(), dto.getUserId(), dto.getUserName(), dto.getEmail(),
                        dto.getAvatarUrl(), dto.getAccountTier(), dto.getAccountBalance(),
                        dto.getVipExpiresAt(), dto.getBio(), dto.isHasPendingUpgradeRequest());
                runOnUiThread(() -> {
                    setLoadingState(false);
                    toast(getString(R.string.msg_login_success, identifier));
                    goToMain();
                });
            } catch (IOException ioe) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    toast(getString(R.string.msg_network_error));
                });
            } catch (IllegalStateException ise) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    toast(ise.getMessage());
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    toast(getString(R.string.msg_login_fail_http, 500, ex.getMessage()));
                });
            }
        }).start();
    }

    private void handleGoogleLogin(String idToken) {
        setLoadingState(true);
        new Thread(() -> {
            try {
                AuthResponseDto dto = service.loginWithGoogle(idToken);
                authPrefs.save(dto.getToken(), dto.getRoleId(), dto.getUserId(), dto.getUserName(), dto.getEmail(),
                        dto.getAvatarUrl(), dto.getAccountTier(), dto.getAccountBalance(),
                        dto.getVipExpiresAt(), dto.getBio(), dto.isHasPendingUpgradeRequest());
                runOnUiThread(() -> {
                    setLoadingState(false);
                    toast(getString(R.string.msg_login_ok));
                    goToMain();
                });
            } catch (IOException ioe) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    toast(getString(R.string.msg_network_error));
                });
            } catch (IllegalStateException ise) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    toast(ise.getMessage());
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    toast(getString(R.string.msg_google_fail, ex.getMessage()));
                });
            }
        }).start();
    }

    private void startGoogleLogin() {
        // Clear any previous local selection first so chooser shows.
        googleClient.signOut()
                .addOnCompleteListener(this, task -> {
                    // Now launch the sign-in intent (will show chooser)
                    Intent intent = googleClient.getSignInIntent();
                    googleLauncher.launch(intent);
                })
                .addOnFailureListener(e -> {
                    // If signOut fails for some reason still try to show chooser
                    Intent intent = googleClient.getSignInIntent();
                    googleLauncher.launch(intent);
                });
    }

    private void setLoadingState(boolean loading) {
        btnLocalLogin.setEnabled(!loading);
        btnGoogle.setEnabled(!loading);
    }

    private String textOf(TextInputEditText editText) {
        CharSequence text = editText.getText();
        return text == null ? "" : text.toString().trim();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
