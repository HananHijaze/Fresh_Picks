package com.example.fresh_picks.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Payment {
    private String paymentMethod; // "Cash" or "CreditCard"
    private boolean isPaid;
    private Cart cart; // Link to the Cart
    private Coupon appliedCoupon; // Add Coupon field

    // Credit Card Details
    private String cardNumber;
    private String cardHolderName;
    private String expirationDate; // Format: MM/YY
    private String cvv;

    // Constructor for Cash Payment
    public Payment(String paymentMethod, boolean isPaid, Cart cart) {
        if (!"Cash".equalsIgnoreCase(paymentMethod) && !"CreditCard".equalsIgnoreCase(paymentMethod)) {
            throw new IllegalArgumentException("Payment method must be 'Cash' or 'CreditCard'.");
        }
        this.paymentMethod = paymentMethod;
        this.isPaid = isPaid;
        this.cart = cart;
    }

    // Constructor for Credit Card Payment
    public Payment(String paymentMethod, boolean isPaid, Cart cart, String cardNumber, String cardHolderName, String expirationDate, String cvv) {
        if (!"CreditCard".equalsIgnoreCase(paymentMethod)) {
            throw new IllegalArgumentException("This constructor is only for CreditCard payments.");
        }
        this.paymentMethod = paymentMethod;
        this.isPaid = isPaid;
        this.cart = cart;
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expirationDate = expirationDate;
        this.cvv = cvv;
    }

    // Getter and Setter for Coupon
    public Coupon getAppliedCoupon() {
        return appliedCoupon;
    }

    public void setAppliedCoupon(Coupon appliedCoupon) {
        this.appliedCoupon = appliedCoupon;
    }

    // Getter and Setter for Payment Method
    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // Getter and Setter for isPaid
    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    // Getter and Setter for Cart
    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    // Credit Card Getters and Setters
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        if (!"CreditCard".equalsIgnoreCase(paymentMethod)) {
            throw new UnsupportedOperationException("Card details can only be set for CreditCard payments.");
        }
        this.cardNumber = cardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        if (!"CreditCard".equalsIgnoreCase(paymentMethod)) {
            throw new UnsupportedOperationException("Card details can only be set for CreditCard payments.");
        }
        this.cardHolderName = cardHolderName;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        if (!"CreditCard".equalsIgnoreCase(paymentMethod)) {
            throw new UnsupportedOperationException("Card details can only be set for CreditCard payments.");
        }
        this.expirationDate = expirationDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        if (!"CreditCard".equalsIgnoreCase(paymentMethod)) {
            throw new UnsupportedOperationException("Card details can only be set for CreditCard payments.");
        }
        this.cvv = cvv;
    }

    // ✅ Calculate total amount with coupon
    public double calculateTotalAmount(Map<String, Product> productMap) {
        double total = cart.calculateTotalPrice(productMap); // ✅ Pass productMap

        // ✅ Convert cart items to a List<Product> for coupon validation
        List<Product> cartProductList = getCartProductList(productMap);

        // Check if a coupon is applied and valid
        if (appliedCoupon != null && appliedCoupon.isValid(total, cartProductList)) {
            // Subtract the discount amount calculated by the coupon
            total -= appliedCoupon.calculateDiscount(total, cartProductList);
        }

        return Math.max(total, 0); // Ensure the total is not negative
    }

    // ✅ Helper method to convert Map<String, Integer> to List<Product>
    private List<Product> getCartProductList(Map<String, Product> productMap) {
        List<Product> productList = new ArrayList<>();
        for (String productId : cart.getItems().keySet()) {
            Product product = productMap.get(productId);
            if (product != null) {
                productList.add(product);
            }
        }
        return productList;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentMethod='" + paymentMethod + '\'' +
                ", isPaid=" + isPaid +
                ", totalAmount=" + calculateTotalAmount(new HashMap<>()) + // Provide an empty productMap for now
                ", appliedCoupon=" + (appliedCoupon != null ? appliedCoupon.getCode() : "None") +
                ", cart=" + cart +
                '}';
    }
}
