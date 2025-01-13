package com.example.fresh_picks.classes;

import java.util.Date;
import java.util.List;

public class Coupon {
    private String code; // Unique identifier for the coupon
    private double discountValue; // Discount amount or percentage
    private boolean isPercentage; // True if discountValue is a percentage
    private Date expiryDate; // Expiration date of the coupon
    private boolean isRedeemed; // True if the coupon has already been used
    private List<Integer> applicableProductIds; // Products eligible for this coupon
    private List<Integer> applicableCategoryIds; // Categories eligible for this coupon
    private double minimumPurchaseAmount; // Minimum cart value to apply coupon

    // Constructor
    public Coupon(String code, double discountValue, boolean isPercentage, Date expiryDate,
                  List<Integer> applicableProductIds, List<Integer> applicableCategoryIds, double minimumPurchaseAmount) {
        this.code = code;
        this.discountValue = discountValue;
        this.isPercentage = isPercentage;
        this.expiryDate = expiryDate;
        this.isRedeemed = false; // By default, coupons are not redeemed
        this.applicableProductIds = applicableProductIds;
        this.applicableCategoryIds = applicableCategoryIds;
        this.minimumPurchaseAmount = minimumPurchaseAmount;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(double discountValue) {
        this.discountValue = discountValue;
    }

    public boolean isPercentage() {
        return isPercentage;
    }

    public void setPercentage(boolean percentage) {
        isPercentage = percentage;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isRedeemed() {
        return isRedeemed;
    }

    public void setRedeemed(boolean redeemed) {
        isRedeemed = redeemed;
    }

    public List<Integer> getApplicableProductIds() {
        return applicableProductIds;
    }

    public void setApplicableProductIds(List<Integer> applicableProductIds) {
        this.applicableProductIds = applicableProductIds;
    }

    public List<Integer> getApplicableCategoryIds() {
        return applicableCategoryIds;
    }

    public void setApplicableCategoryIds(List<Integer> applicableCategoryIds) {
        this.applicableCategoryIds = applicableCategoryIds;
    }

    public double getMinimumPurchaseAmount() {
        return minimumPurchaseAmount;
    }

    public void setMinimumPurchaseAmount(double minimumPurchaseAmount) {
        this.minimumPurchaseAmount = minimumPurchaseAmount;
    }

    // Method to check if the coupon is valid
    public boolean isValid(double cartTotal, List<Product> cartItems) {
        // Check expiration and redemption status
        if (isRedeemed || new Date().after(expiryDate)) {
            return false;
        }

        // Check minimum purchase amount
        if (cartTotal < minimumPurchaseAmount) {
            return false;
        }

        // Check product/category applicability
        boolean isApplicable = false;
        for (Product product : cartItems) {
            if (applicableProductIds != null && applicableProductIds.contains(product.getId())) {
                isApplicable = true;
                break;
            }
            if (applicableCategoryIds != null && product.getCategory() != null &&
                    applicableCategoryIds.contains(product.getCategory().getId())) {
                isApplicable = true;
                break;
            }
        }
        return isApplicable;
    }

    // Method to calculate discount amount
    public double calculateDiscount(double totalAmount, List<Product> cartItems) {
        if (!isValid(totalAmount, cartItems)) {
            return 0;
        }
        return isPercentage ? (totalAmount * (discountValue / 100)) : discountValue;
    }

    @Override
    public String toString() {
        return "Coupon{" +
                "code='" + code + '\'' +
                ", discountValue=" + discountValue +
                ", isPercentage=" + isPercentage +
                ", expiryDate=" + expiryDate +
                ", isRedeemed=" + isRedeemed +
                ", applicableProductIds=" + applicableProductIds +
                ", applicableCategoryIds=" + applicableCategoryIds +
                ", minimumPurchaseAmount=" + minimumPurchaseAmount +
                '}';
    }
}
