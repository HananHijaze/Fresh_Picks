package com.example.fresh_picks.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Payment {
    private String userId; // Associate payment with a user
    private String paymentMethod; // "Cash" or "CreditCard"
    private boolean isPaid;
    private Order order; // ✅ Associates Payment with an Order
    private Coupon appliedCoupon; // Optional Coupon field

    private String transactionId; // ✅ Unique transaction ID for each payment

    // Credit Card Details
    private String cardNumber;
    private String cardHolderName;
    private String expirationDate; // Format: MM/YY
    private String cvv;

    // 🔹 Constructor for Cash Payment
    public Payment(String userId, String paymentMethod, boolean isPaid, Order order) {
        validateUserId(userId);
        validatePaymentMethod(paymentMethod);

        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.isPaid = isPaid;
        this.order = order;
        this.transactionId = UUID.randomUUID().toString(); // ✅ Generate unique transaction ID
    }

    // 🔹 Constructor for Credit Card Payment
    public Payment(String userId, String paymentMethod, boolean isPaid, Order order,
                   String cardNumber, String cardHolderName, String expirationDate, String cvv) {
        validateUserId(userId);
        validatePaymentMethod(paymentMethod);

        if (!"CreditCard".equalsIgnoreCase(paymentMethod)) {
            throw new IllegalArgumentException("This constructor is only for CreditCard payments.");
        }

        validateCardDetails(cardNumber, expirationDate, cvv);

        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.isPaid = isPaid;
        this.order = order;
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expirationDate = expirationDate;
        this.cvv = cvv;
        this.transactionId = UUID.randomUUID().toString(); // ✅ Generate unique transaction ID
    }

    public Payment() {
        this.transactionId = UUID.randomUUID().toString(); // Ensure transactionId is always set
    }

    // 🔹 Validate user ID
    private void validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }
    }

    // 🔹 Validate payment method
    private void validatePaymentMethod(String paymentMethod) {
        if (!"Cash".equalsIgnoreCase(paymentMethod) && !"CreditCard".equalsIgnoreCase(paymentMethod)) {
            throw new IllegalArgumentException("Payment method must be 'Cash' or 'CreditCard'.");
        }
    }

    // 🔹 Validate credit card details
    private void validateCardDetails(String cardNumber, String expirationDate, String cvv) {
        if (cardNumber == null || cardNumber.length() != 16 || !cardNumber.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid card number. It must be 16 digits.");
        }
        if (expirationDate == null || !expirationDate.matches("^(0[1-9]|1[0-2])/\\d{2}$")) {
            throw new IllegalArgumentException("Invalid expiration date. Use MM/YY format.");
        }
        if (cvv == null || cvv.length() != 3 || !cvv.matches("\\d{3}")) {
            throw new IllegalArgumentException("Invalid CVV. It must be 3 digits.");
        }
    }

    // ✅ Getter for transaction ID
    public String getTransactionId() {
        return transactionId;
    }

    // 🔹 Getter and Setter for `userId`
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        validateUserId(userId);
        this.userId = userId;
    }

    // 🔹 Getter and Setter for Order
    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    // 🔹 Getter and Setter for Coupon
    public Coupon getAppliedCoupon() {
        return appliedCoupon;
    }

    public void setAppliedCoupon(Coupon appliedCoupon) {
        this.appliedCoupon = appliedCoupon;
    }

    // 🔹 Getter and Setter for Payment Method
    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        validatePaymentMethod(paymentMethod);
        this.paymentMethod = paymentMethod;
    }

    // 🔹 Getter and Setter for isPaid
    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    // 🔹 Credit Card Getters and Setters
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        if (!"CreditCard".equalsIgnoreCase(paymentMethod)) {
            throw new UnsupportedOperationException("Card details can only be set for CreditCard payments.");
        }
        validateCardDetails(cardNumber, expirationDate, cvv);
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
        validateCardDetails(cardNumber, expirationDate, cvv);
        this.expirationDate = expirationDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        if (!"CreditCard".equalsIgnoreCase(paymentMethod)) {
            throw new UnsupportedOperationException("Card details can only be set for CreditCard payments.");
        }
        validateCardDetails(cardNumber, expirationDate, cvv);
        this.cvv = cvv;
    }

    public double calculateTotalAmount(Map<String, Product> productMap) {
        if (order == null) {
            throw new IllegalStateException("Order is not set.");
        }

        double total = order.getTotalPrice();

        // ✅ Convert product ID → Product list for coupon validation
        List<Product> cartProductList = getCartProductList(productMap);

        // ✅ Check if a coupon is applied and valid
        if (appliedCoupon != null && appliedCoupon.isValid(total, cartProductList)) {
            total -= appliedCoupon.calculateDiscount(total, cartProductList);
        }

        return Math.max(total, 0); // Ensure total is not negative
    }

    // 🔹 Helper method: Convert product IDs into a List<Product>
    private List<Product> getCartProductList(Map<String, Product> productMap) {
        List<Product> productList = new ArrayList<>();
        for (String productId : order.getProductQuantities().keySet()) {
            if (productMap.containsKey(productId)) {
                productList.add(productMap.get(productId));
            }
        }
        return productList;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "userId='" + userId + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", isPaid=" + isPaid +
                ", transactionId='" + transactionId + '\'' + // ✅ Now included in the toString() method
                ", order=" + order +
                ", appliedCoupon=" + appliedCoupon +
                '}';
    }
}
