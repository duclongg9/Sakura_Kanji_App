package app.model;

import java.time.LocalDateTime;

/**
 * Thông tin hội viên phục vụ trang quản trị.
 */
public class AdminMember {

    private long id;
    private String userName;
    private String email;
    private String avatarUrl;
    private String accountTier;
    private LocalDateTime vipExpiresAt;
    private Long requestId;
    private String requestStatus;
    private String requestNote;
    private LocalDateTime requestCreatedAt;
    private String requestReceiptImagePath;
    private String requestTransactionCode;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAccountTier() {
        return accountTier;
    }

    public void setAccountTier(String accountTier) {
        this.accountTier = accountTier;
    }

    public LocalDateTime getVipExpiresAt() {
        return vipExpiresAt;
    }

    public void setVipExpiresAt(LocalDateTime vipExpiresAt) {
        this.vipExpiresAt = vipExpiresAt;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getRequestNote() {
        return requestNote;
    }

    public void setRequestNote(String requestNote) {
        this.requestNote = requestNote;
    }

    public LocalDateTime getRequestCreatedAt() {
        return requestCreatedAt;
    }

    public void setRequestCreatedAt(LocalDateTime requestCreatedAt) {
        this.requestCreatedAt = requestCreatedAt;
    }

    /**
     * Lấy tên file ảnh chứng từ đính kèm yêu cầu nâng cấp.
     *
     * @return tên file ảnh hoặc {@code null} nếu không có.
     */
    public String getRequestReceiptImagePath() {
        return requestReceiptImagePath;
    }

    /**
     * Cập nhật tên file ảnh chứng từ cho yêu cầu nâng cấp.
     *
     * @param requestReceiptImagePath tên file ảnh.
     */
    public void setRequestReceiptImagePath(String requestReceiptImagePath) {
        this.requestReceiptImagePath = requestReceiptImagePath;
    }

    /**
     * Lấy mã giao dịch do quản trị viên lưu lại.
     *
     * @return chuỗi mã giao dịch hoặc {@code null}.
     */
    public String getRequestTransactionCode() {
        return requestTransactionCode;
    }

    /**
     * Thiết lập mã giao dịch do quản trị viên thêm vào yêu cầu nâng cấp.
     *
     * @param requestTransactionCode chuỗi mã giao dịch.
     */
    public void setRequestTransactionCode(String requestTransactionCode) {
        this.requestTransactionCode = requestTransactionCode;
    }
}
