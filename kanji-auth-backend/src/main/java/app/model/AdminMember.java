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
}
