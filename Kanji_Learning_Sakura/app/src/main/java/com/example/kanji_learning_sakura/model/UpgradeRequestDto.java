package com.example.kanji_learning_sakura.model;

/**
 * DTO ghi nhận yêu cầu nâng cấp tài khoản của người dùng.
 */
public class UpgradeRequestDto {
    private long requestId;
    private String status;
    private String note;
    private int targetRoleId;
    private String createdAt;

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getTargetRoleId() {
        return targetRoleId;
    }

    public void setTargetRoleId(int targetRoleId) {
        this.targetRoleId = targetRoleId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
