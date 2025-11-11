package com.example.kanji_learning_sakura.model;

/**
 * DTO đại diện một gói VIP hiển thị ở màn quản trị.
 */
public class VipPlanDto {
    private String code;
    private String description;
    private double amount;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
