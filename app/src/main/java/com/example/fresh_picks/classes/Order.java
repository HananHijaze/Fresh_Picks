package com.example.fresh_picks.classes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Order {
    private String id;
    private Map<String, Integer> productQuantities; // Product IDs → Quantity
    private double totalPrice;
    private Payment payment;
    private String status; // "Pending", "Ready for Pickup", "Delivered", "Canceled"
    private boolean isReadyForPickup;
    private boolean isDelivered;

    private final String createdAt; // Timestamp in String format

    // ✅ Constructor with parameters
    public Order(String id, Map<String, Integer> productQuantities, Payment payment) {
        this.id = id;
        this.productQuantities = new HashMap<>(productQuantities);
        this.payment = payment;
        this.totalPrice = calculateTotalPrice();
        this.status = "Pending";
        this.isReadyForPickup = false;
        this.isDelivered = false;

        // ✅ Always initialize createdAt
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        this.createdAt = dateFormat.format(new Date());
    }

    // ✅ No-argument constructor (Firestore needs this)
    public Order() {
        this.productQuantities = new HashMap<>();
        this.status = "Pending";
        this.isReadyForPickup = false;
        this.isDelivered = false;

        // ✅ Always initialize createdAt
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        this.createdAt = dateFormat.format(new Date());
    }

    // Getters
    public String getId() {
        return id;
    }

    public Map<String, Integer> getProductQuantities() {
        return new HashMap<>(productQuantities);
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public Payment getPayment() {
        return payment;
    }

    public String getStatus() {
        return status;
    }

    public boolean isReadyForPickup() {
        return isReadyForPickup;
    }

    public boolean isDelivered() {
        return isDelivered;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setStatus(String status) {
        this.status = status;
    }

    public void setReadyForPickup(boolean readyForPickup) {
        isReadyForPickup = readyForPickup;
        if (readyForPickup) {
            this.status = "Ready for Pickup";
        }
    }

    public void setDelivered(boolean delivered) {
        isDelivered = delivered;
        if (delivered) {
            this.status = "Delivered";
        }
    }

    // Calculate total price (You need to update this with actual product prices)
    private double calculateTotalPrice() {
        double total = 0;
        for (int quantity : productQuantities.values()) {
            total += quantity; // Update this logic with actual product price fetching
        }
        return total;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", productQuantities=" + productQuantities +
                ", totalPrice=" + totalPrice +
                ", payment=" + payment +
                ", status='" + status + '\'' +
                ", isReadyForPickup=" + isReadyForPickup +
                ", isDelivered=" + isDelivered +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
