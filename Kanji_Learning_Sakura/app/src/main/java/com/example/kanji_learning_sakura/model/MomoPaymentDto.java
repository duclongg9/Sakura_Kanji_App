package com.example.kanji_learning_sakura.model;

/**
 * DTO trả về khi khởi tạo thanh toán MoMo.
 */
public class MomoPaymentDto {
    private String orderId;
    private String requestId;
    private String planCode;
    private double amount;
    private String payUrl;
    private String deeplink;
    private String status;
    private boolean stubMode;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPayUrl() {
        return payUrl;
    }

    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }

    public String getDeeplink() {
        return deeplink;
    }

    public void setDeeplink(String deeplink) {
        this.deeplink = deeplink;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isStubMode() {
        return stubMode;
    }

    public void setStubMode(boolean stubMode) {
        this.stubMode = stubMode;
    }
}
