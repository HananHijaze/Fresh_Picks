package com.example.fresh_picks.classes;

public class Payment {
    private String paymentMethod; // "Cash" or "CreditCard"
    private boolean isPaid;
    private Cart cart; // Link to the Cart

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

    // Getters and Setters
    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

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

    // Utility Method to calculate the total payment amount from the Cart
    public double calculateTotalAmount() {
        return cart.calculateTotalPrice();
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentMethod='" + paymentMethod + '\'' +
                ", isPaid=" + isPaid +
                ", totalAmount=" + calculateTotalAmount() +
                ", cart=" + cart +
                '}';
    }
}
