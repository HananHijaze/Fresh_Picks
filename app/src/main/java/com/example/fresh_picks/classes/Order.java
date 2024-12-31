package com.example.fresh_picks.classes;

import java.util.List;

public class Order {
    private int id;
    private List<CartItem> items;
    private double totalPrice;
    private Payment payment;
    private String status; // e.g., "Pending", "Ready for Pickup", "Delivered", "Picked Up"
    private boolean isReadyForPickup;
    private boolean isDelivered;

    // Constructors
    public Order(int id, List<CartItem> items, double totalPrice, Payment payment, String status) {
        this.id = id;
        this.items = items;
        this.totalPrice = totalPrice;
        this.payment = payment;
        this.status = status;
        this.isReadyForPickup = false;
        this.isDelivered = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isReadyForPickup() {
        return isReadyForPickup;
    }

    public void setReadyForPickup(boolean readyForPickup) {
        isReadyForPickup = readyForPickup;
        if (readyForPickup) {
            this.status = "Ready for Pickup";
        }
    }


    public boolean isDelivered() {
        return isDelivered;
    }

    public void setDelivered(boolean delivered) {
        isDelivered = delivered;
        if (delivered) {
            this.status = "Delivered";
        }
    }

    // Other Methods
    public void markReadyForPickup() {
        this.isReadyForPickup = true;
        this.status = "Ready for Pickup";
    }



    public void markDelivered() {
        this.isDelivered = true;
        this.status = "Delivered";
    }
}


