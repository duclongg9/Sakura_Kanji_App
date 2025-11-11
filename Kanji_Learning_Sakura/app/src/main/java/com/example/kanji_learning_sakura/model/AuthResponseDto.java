package com.example.kanji_learning_sakura.model;

/**
 * DTO phản hồi đăng nhập.
 */
public class AuthResponseDto {
    private String token;
    private int roleId;
    private String userName;
    private long userId;
    private String email;
    private String avatarUrl;
    private String accountTier;
    private double accountBalance;
    private String vipExpiresAt;
    private String bio;
    private boolean hasPendingUpgradeRequest;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
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

    public double getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(double accountBalance) {
        this.accountBalance = accountBalance;
    }

    public String getVipExpiresAt() {
        return vipExpiresAt;
    }

    public void setVipExpiresAt(String vipExpiresAt) {
        this.vipExpiresAt = vipExpiresAt;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public boolean isHasPendingUpgradeRequest() {
        return hasPendingUpgradeRequest;
    }

    public void setHasPendingUpgradeRequest(boolean hasPendingUpgradeRequest) {
        this.hasPendingUpgradeRequest = hasPendingUpgradeRequest;
    }
}
