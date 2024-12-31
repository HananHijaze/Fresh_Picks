package com.example.fresh_picks.classes;

public class Payment {
    private String paymentMethod; // e.g., "Credit Card", "PayPal"
    private boolean isPaid;

    // Constructors
    public Payment(String paymentMethod, boolean isPaid) {
        this.paymentMethod = paymentMethod;
        this.isPaid = isPaid;
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
}
