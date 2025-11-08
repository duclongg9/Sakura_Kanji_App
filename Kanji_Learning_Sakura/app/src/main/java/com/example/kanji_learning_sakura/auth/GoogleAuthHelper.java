package com.example.kanji_learning_sakura.auth;

import android.app.Activity;
import android.os.CancellationSignal;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.credentials.CredentialManager;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.Executor;

public final class GoogleAuthHelper {
    private final Activity activity;
    private final CredentialManager cm;

    public GoogleAuthHelper(Activity activity) {
        this.activity = activity;
        this.cm = CredentialManager.create(activity);
    }

    @Nullable
    public String getIdToken() {
        // Lấy WEB_CLIENT_ID từ strings.xml
        int resId = activity.getResources()
                .getIdentifier("web_client_id", "string", activity.getPackageName());
        String webClientId = activity.getString(resId);

        GetGoogleIdOption option = new GetGoogleIdOption.Builder()
                .setServerClientId(webClientId)
                .setFilterByAuthorizedAccounts(true)
                .setAutoSelectEnabled(true)
                .build();

        GetCredentialRequest req = new GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build();

        // Dùng callback + chờ ngắn với CountDownLatch để trả về String
        final AtomicReference<String> tokenRef = new AtomicReference<>(null);
        final AtomicReference<Exception> errRef = new AtomicReference<>(null);
        final CountDownLatch latch = new CountDownLatch(1);

        CancellationSignal cancel = new CancellationSignal();
        Executor executor = ContextCompat.getMainExecutor(activity);

        cm.getCredentialAsync(
                activity,
                req,
                cancel,
                executor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse res) {
                        try {
                            if (res != null && res.getCredential() instanceof CustomCredential) {
                                CustomCredential c = (CustomCredential) res.getCredential();
                                if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(c.getType())) {
                                    GoogleIdTokenCredential g = GoogleIdTokenCredential.createFrom(c.getData());
                                    tokenRef.set(g.getIdToken());
                                }
                            }
                        } catch (Exception e) {
                            errRef.set(e);
                        } finally {
                            latch.countDown();
                        }
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        errRef.set(e);
                        latch.countDown();
                    }
                });

        try {
            // chờ tối đa 15s (tuỳ bạn)
            latch.await(15, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }

        // Có thể log errRef.get() nếu cần
        return tokenRef.get();
    }
}
