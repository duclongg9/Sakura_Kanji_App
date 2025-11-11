package app.model;

import java.time.LocalDateTime;

/**
 * Thực thể biểu diễn yêu cầu nâng cấp tài khoản VIP.
 */
public class AccountUpgradeRequest {

    private long requestId;
    private long userId;
    private int currentRoleId;
    private int targetRoleId;
    private String note;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String receiptImagePath;
    private String transactionCode;

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getCurrentRoleId() {
        return currentRoleId;
    }

    public void setCurrentRoleId(int currentRoleId) {
        this.currentRoleId = currentRoleId;
    }

    public int getTargetRoleId() {
        return targetRoleId;
    }

    public void setTargetRoleId(int targetRoleId) {
        this.targetRoleId = targetRoleId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    /**
     * Đường dẫn ảnh chứng từ chuyển khoản được lưu trong hệ thống.
     *
     * @return tên file ảnh hoặc {@code null} nếu người dùng không tải lên.
     */
    public String getReceiptImagePath() {
        return receiptImagePath;
    }

    /**
     * Cập nhật đường dẫn ảnh chứng từ chuyển khoản.
     *
     * @param receiptImagePath tên file ảnh (không bao gồm thư mục).
     */
    public void setReceiptImagePath(String receiptImagePath) {
        this.receiptImagePath = receiptImagePath;
    }

    /**
     * Lấy mã giao dịch do quản trị viên cập nhật để đối chiếu thanh toán.
     *
     * @return mã giao dịch hoặc {@code null} nếu chưa được thiết lập.
     */
    public String getTransactionCode() {
        return transactionCode;
    }

    /**
     * Thiết lập mã giao dịch do quản trị viên nhập.
     *
     * @param transactionCode chuỗi mã giao dịch.
     */
    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }
}
