package app.integration.momo;

import java.math.BigDecimal;
import java.time.Period;

/**
 * Danh sách gói VIP hỗ trợ thanh toán qua MoMo.
 */
public enum MomoUpgradePlan {
    VIP_MONTHLY("VIP_MONTHLY", BigDecimal.valueOf(99000), Period.ofMonths(1), "VIP 1 tháng"),
    VIP_QUARTERLY("VIP_QUARTERLY", BigDecimal.valueOf(279000), Period.ofMonths(3), "VIP 3 tháng"),
    VIP_YEARLY("VIP_YEARLY", BigDecimal.valueOf(999000), Period.ofYears(1), "VIP 12 tháng");

    private final String code;
    private final BigDecimal amount;
    private final Period duration;
    private final String description;

    MomoUpgradePlan(String code, BigDecimal amount, Period duration, String description) {
        this.code = code;
        this.amount = amount;
        this.duration = duration;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Period getDuration() {
        return duration;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Tìm gói theo code từ client gửi lên.
     *
     * @param code mã gói.
     * @return {@link MomoUpgradePlan} tương ứng.
     * @throws IllegalArgumentException nếu không hỗ trợ mã gói.
     */
    public static MomoUpgradePlan fromCode(String code) {
        if (code == null || code.isBlank()) {
            return VIP_MONTHLY;
        }
        for (MomoUpgradePlan plan : values()) {
            if (plan.code.equalsIgnoreCase(code)) {
                return plan;
            }
        }
        throw new IllegalArgumentException("Unsupported plan code: " + code);
    }
}
