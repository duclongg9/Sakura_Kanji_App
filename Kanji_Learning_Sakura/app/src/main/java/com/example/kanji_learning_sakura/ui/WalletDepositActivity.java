package com.example.kanji_learning_sakura.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.config.AuthPrefs;
import com.example.kanji_learning_sakura.core.KanjiService;
import com.example.kanji_learning_sakura.model.WalletDepositDto;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình tạo mã QR nạp tiền cho ví Sakura.
 */
public class WalletDepositActivity extends AppCompatActivity {

    private static final double MIN_DEPOSIT = 20000d;

    private TextInputLayout layoutAmount;
    private TextInputEditText edtAmount;
    private ImageView imgQr;
    private TextView tvStatus;
    private TextView tvMeta;
    private CircularProgressIndicator progressBar;
    private MaterialButton btnGenerate;
    private ChipGroup chipGroup;
    private KanjiService service;
    private AuthPrefs authPrefs;
    private CharSequence previousStatus;
    private CharSequence previousMeta;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authPrefs = new AuthPrefs(this);
        if (!authPrefs.isLoggedIn()) {
            finish();
            return;
        }
        setContentView(R.layout.activity_wallet_deposit);

        service = new KanjiService(this);
        MaterialToolbar toolbar = findViewById(R.id.depositToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        layoutAmount = findViewById(R.id.layoutAmount);
        edtAmount = findViewById(R.id.edtAmount);
        imgQr = findViewById(R.id.imgQr);
        tvStatus = findViewById(R.id.tvDepositStatus);
        tvMeta = findViewById(R.id.tvDepositMeta);
        progressBar = findViewById(R.id.depositProgress);
        btnGenerate = findViewById(R.id.btnGenerateQr);
        chipGroup = findViewById(R.id.groupQuickAmount);

        setupQuickAmounts();

        btnGenerate.setOnClickListener(v -> generateDeposit());
    }

    private void generateDeposit() {
        if (layoutAmount != null) {
            layoutAmount.setError(null);
        }
        String raw = edtAmount.getText() != null ? edtAmount.getText().toString().trim() : "";
        if (TextUtils.isEmpty(raw)) {
            if (layoutAmount != null) {
                layoutAmount.setError(getString(R.string.wallet_error_required));
            }
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(raw);
        } catch (NumberFormatException ex) {
            if (layoutAmount != null) {
                layoutAmount.setError(getString(R.string.wallet_error_invalid));
            }
            return;
        }
        if (amount < MIN_DEPOSIT) {
            if (layoutAmount != null) {
                layoutAmount.setError(getString(R.string.wallet_error_min_amount, (int) MIN_DEPOSIT));
            }
            return;
        }

        setLoading(true);
        new Thread(() -> {
            try {
                WalletDepositDto dto = service.createDeposit(amount);
                runOnUiThread(() -> {
                    renderDeposit(dto);
                    toast(getString(R.string.msg_wallet_success));
                });
            } catch (IllegalStateException ex) {
                runOnUiThread(() -> {
                    setLoading(false);
                    if (layoutAmount != null && ex.getMessage() != null && ex.getMessage().contains("20000")) {
                        layoutAmount.setError(getString(R.string.wallet_error_min_amount, (int) MIN_DEPOSIT));
                    }
                    toast(getString(R.string.msg_wallet_error, ex.getMessage()));
                    restorePreviousState();
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    setLoading(false);
                    toast(getString(R.string.msg_wallet_error, ex.getMessage()));
                    restorePreviousState();
                });
            }
        }).start();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnGenerate.setEnabled(!loading);
        edtAmount.setEnabled(!loading);
        if (layoutAmount != null) {
            layoutAmount.setEnabled(!loading);
        }
        setChipsEnabled(!loading);
        if (loading) {
            previousStatus = tvStatus.getText();
            previousMeta = tvMeta.getText();
            tvStatus.setText(getString(R.string.wallet_status_generating));
            tvMeta.setVisibility(View.GONE);
        }
    }

    /**
     * Gắn sự kiện cho các chip chọn nhanh số tiền nạp và tự động điền vào ô nhập liệu.
     */
    private void setupQuickAmounts() {
        if (chipGroup == null) {
            return;
        }
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds == null || checkedIds.isEmpty()) {
                return;
            }
            Integer id = checkedIds.get(0);
            if (id == null) {
                return;
            }
            Chip chip = group.findViewById(id);
            if (chip != null && chip.getTag() != null) {
                String value = chip.getTag().toString();
                edtAmount.setText(value);
                edtAmount.setSelection(value.length());
            }
        });
    }

    /**
     * Hiển thị thông tin mã QR và trạng thái giao dịch trả về từ backend.
     *
     * @param dto dữ liệu giao dịch nạp tiền mới nhất.
     */
    private void renderDeposit(WalletDepositDto dto) {
        setLoading(false);
        if (dto.getQrCodeUrl() != null && !dto.getQrCodeUrl().isEmpty()) {
            Picasso.get().load(dto.getQrCodeUrl()).into(imgQr);
        } else {
            imgQr.setImageResource(R.mipmap.ic_launcher);
        }
        String status = statusLabel(dto.getStatus());
        String createdAt = formatTimestamp(dto.getCreatedAt());
        String amountText = formatCurrency(dto.getAmount());
        tvStatus.setText(getString(R.string.wallet_status_detail, dto.getDepositId(), amountText, status, createdAt));
        tvMeta.setVisibility(View.VISIBLE);
        tvMeta.setText(getString(R.string.wallet_meta_hint, amountText));
        previousStatus = tvStatus.getText();
        previousMeta = tvMeta.getText();
    }

    /**
     * Vô hiệu hóa hoặc kích hoạt lại các chip chọn nhanh khi đang thực hiện request.
     */
    private void setChipsEnabled(boolean enabled) {
        if (chipGroup == null) {
            return;
        }
        final int count = chipGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = chipGroup.getChildAt(i);
            if (child != null) {
                child.setEnabled(enabled);
            }
        }
    }

    /**
     * Định dạng số tiền theo chuẩn tiền tệ Việt Nam.
     */
    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        format.setMaximumFractionDigits(0);
        return format.format(amount);
    }

    /**
     * Chuyển mã trạng thái backend sang chuỗi hiển thị dễ hiểu cho người dùng.
     */
    private String statusLabel(String status) {
        if (status == null) {
            return getString(R.string.wallet_status_pending_label);
        }
        switch (status.toUpperCase(Locale.US)) {
            case "PAID":
                return getString(R.string.wallet_status_paid_label);
            case "CANCELLED":
                return getString(R.string.wallet_status_cancelled_label);
            default:
                return getString(R.string.wallet_status_pending_label);
        }
    }

    /**
     * Chuyển thời gian ISO từ backend sang định dạng thân thiện (ngôn ngữ Việt).
     */
    private String formatTimestamp(String raw) {
        if (raw == null || raw.isEmpty()) {
            return getString(R.string.wallet_status_time_unknown);
        }
        try {
            LocalDateTime time = LocalDateTime.parse(raw);
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    .withLocale(new Locale("vi", "VN"));
            return formatter.format(time);
        } catch (Exception ignored) {
            return raw;
        }
    }

    /**
     * Đưa giao diện về trạng thái trước khi request để người dùng tiếp tục quét mã cũ nếu cần.
     */
    private void restorePreviousState() {
        if (previousStatus != null && previousStatus.length() > 0) {
            tvStatus.setText(previousStatus);
        } else {
            tvStatus.setText(getString(R.string.wallet_empty_qr));
        }
        if (previousMeta != null && previousMeta.length() > 0) {
            tvMeta.setText(previousMeta);
            tvMeta.setVisibility(View.VISIBLE);
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
