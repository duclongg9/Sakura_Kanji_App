package com.example.kanji_learning_sakura.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.config.AuthPrefs;
import com.example.kanji_learning_sakura.core.KanjiService;
import com.example.kanji_learning_sakura.model.MomoPaymentDto;
import com.example.kanji_learning_sakura.model.MomoPaymentStatusDto;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * Màn hình khởi tạo và theo dõi thanh toán MoMo để nâng cấp VIP.
 */
public class MomoUpgradeActivity extends AppCompatActivity {

    private KanjiService service;
    private MaterialButton btnPay;
    private MaterialButton btnCheck;
    private TextView tvStatus;
    private TextView tvCurrentOrder;
    private TextView tvStubNotice;
    private ProgressBar progressBar;
    private String selectedPlan = "VIP_MONTHLY";
    private String currentOrderId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthPrefs prefs = new AuthPrefs(this);
        if (!prefs.isLoggedIn()) {
            finish();
            return;
        }
        setContentView(R.layout.activity_momo_upgrade);
        service = new KanjiService(this);

        MaterialToolbar toolbar = findViewById(R.id.momoToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        btnPay = findViewById(R.id.btnPayMomo);
        btnCheck = findViewById(R.id.btnCheckStatus);
        tvStatus = findViewById(R.id.tvMomoStatus);
        tvCurrentOrder = findViewById(R.id.tvCurrentOrder);
        tvStubNotice = findViewById(R.id.tvStubNotice);
        progressBar = findViewById(R.id.momoProgress);
        RadioGroup radioGroup = findViewById(R.id.radioPlans);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioPlanQuarterly) {
                selectedPlan = "VIP_QUARTERLY";
            } else if (checkedId == R.id.radioPlanYearly) {
                selectedPlan = "VIP_YEARLY";
            } else {
                selectedPlan = "VIP_MONTHLY";
            }
        });

        btnPay.setOnClickListener(v -> startPayment());
        btnCheck.setOnClickListener(v -> {
            if (currentOrderId == null) {
                toast(getString(R.string.momo_no_order));
            } else {
                checkStatus(currentOrderId);
            }
        });
    }

    private void startPayment() {
        setLoading(true);
        new Thread(() -> {
            try {
                MomoPaymentDto payment = service.createMomoVipPayment(selectedPlan);
                currentOrderId = payment.getOrderId();
                runOnUiThread(() -> {
                    setLoading(false);
                    if (payment.isStubMode()) {
                        tvStubNotice.setVisibility(View.VISIBLE);
                    } else {
                        tvStubNotice.setVisibility(View.GONE);
                    }
                    tvCurrentOrder.setText(getString(R.string.momo_label_last_order, currentOrderId));
                    tvStatus.setText(getString(R.string.momo_status_waiting, currentOrderId));
                    if (payment.getPayUrl() != null && !payment.getPayUrl().isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payment.getPayUrl()));
                        startActivity(intent);
                    } else if (payment.getDeeplink() != null && !payment.getDeeplink().isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payment.getDeeplink()));
                        startActivity(intent);
                    } else {
                        toast(getString(R.string.msg_upgrade_error, "missing payUrl"));
                    }
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    setLoading(false);
                    toast(getString(R.string.msg_upgrade_error, ex.getMessage()));
                });
            }
        }).start();
    }

    private void checkStatus(String orderId) {
        setLoading(true);
        new Thread(() -> {
            try {
                MomoPaymentStatusDto status = service.getMomoPaymentStatus(orderId);
                runOnUiThread(() -> {
                    setLoading(false);
                    currentOrderId = status.getOrderId();
                    if (currentOrderId != null) {
                        tvCurrentOrder.setText(getString(R.string.momo_label_last_order, currentOrderId));
                    }
                    if (status.isVipActivated() && status.getVipExpiresAt() != null) {
                        tvStatus.setText(getString(R.string.momo_status_success, status.getVipExpiresAt()));
                        toast(getString(R.string.momo_status_success, status.getVipExpiresAt()));
                    } else if ("SUCCESS".equalsIgnoreCase(status.getStatus())) {
                        tvStatus.setText(getString(R.string.momo_status_success, status.getVipExpiresAt() != null
                                ? status.getVipExpiresAt() : "—"));
                    } else if ("FAILED".equalsIgnoreCase(status.getStatus())) {
                        String message = status.getMessage() != null ? status.getMessage() : "";
                        tvStatus.setText(getString(R.string.momo_status_error, message));
                        toast(getString(R.string.momo_status_error, message));
                    } else {
                        tvStatus.setText(getString(R.string.momo_status_waiting, orderId));
                    }
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    setLoading(false);
                    toast(getString(R.string.momo_status_error, ex.getMessage()));
                });
            }
        }).start();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnPay.setEnabled(!loading);
        btnCheck.setEnabled(!loading);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
