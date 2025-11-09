package com.example.kanji_learning_sakura.model;

/**
 * DTO thông tin trang cá nhân hiển thị trong ứng dụng.
 */
public class ProfileDto {
    private long id;
    private String userName;
    private String email;
    private int roleId;
    private String avatarUrl;
    private String accountTier;
    private double accountBalance;
    private String vipExpiresAt;
    private String bio;

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

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
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
}
