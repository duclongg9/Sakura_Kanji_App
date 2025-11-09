package com.example.kanji_learning_sakura.model;

/**
 * DTO phản hồi trạng thái thanh toán MoMo.
 */
public class MomoPaymentStatusDto {
    private String orderId;
    private String planCode;
    private String status;
    private double amount;
    private String payUrl;
    private String deeplink;
    private Integer resultCode;
    private String message;
    private boolean vipActivated;
    private String vipExpiresAt;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isVipActivated() {
        return vipActivated;
    }

    public void setVipActivated(boolean vipActivated) {
        this.vipActivated = vipActivated;
    }

    public String getVipExpiresAt() {
        return vipExpiresAt;
    }

    public void setVipExpiresAt(String vipExpiresAt) {
        this.vipExpiresAt = vipExpiresAt;
    }
}
