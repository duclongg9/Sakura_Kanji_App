package com.example.kanji_learning_sakura.model;

/**
 * DTO phục vụ màn quản trị hội viên.
 */
public class AdminMemberDto {
    private long id;
    private String userName;
    private String email;
    private String avatarUrl;
    private String accountTier;
    private String vipExpiresAt;
    private boolean hasPendingRequest;
    private String requestStatus;
    private String requestNote;
    private String requestCreatedAt;

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

    public String getVipExpiresAt() {
        return vipExpiresAt;
    }

    public void setVipExpiresAt(String vipExpiresAt) {
        this.vipExpiresAt = vipExpiresAt;
    }

    public boolean isHasPendingRequest() {
        return hasPendingRequest;
    }

    public void setHasPendingRequest(boolean hasPendingRequest) {
        this.hasPendingRequest = hasPendingRequest;
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

    public String getRequestCreatedAt() {
        return requestCreatedAt;
    }

    public void setRequestCreatedAt(String requestCreatedAt) {
        this.requestCreatedAt = requestCreatedAt;
    }
}
