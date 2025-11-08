package com.example.kanji_learning_sakura.model;

import java.time.LocalDateTime;

/**
 * User rút gọn cho app: roleId = 1(Admin), 2(User), 3(VIP).
 * DB: User(id, userName, email, imgUrl, matKhau, roleId, createdAt, updatedAt)
 */
public class User {
    private long id;
    private String userName;
    private String email;
    private String imgUrl;         // nullable
    private String matKhau;        // nullable (nếu dùng OAuth)
    private int roleId = 2;        // 1=Admin, 2=User, 3=VIP
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {}

    public User(long id, String userName, String email, String imgUrl, String matKhau,
                int roleId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.imgUrl = imgUrl;
        this.matKhau = matKhau;
        this.roleId = roleId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // getters/setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getImgUrl() { return imgUrl; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }

    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override public String toString() {
        return "User{id=" + id + ", userName='" + userName + '\'' + ", roleId=" + roleId + "}";
    }
}
