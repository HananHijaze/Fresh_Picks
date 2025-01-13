package com.example.fresh_picks.classes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Order {
    private int id;
    private Map<Product, Integer> productQuantities; // Products and their quantities
    private double totalPrice;
    private Payment payment;
    private String status; // e.g., "Pending", "Ready for Pickup", "Delivered", "Canceled"
    private boolean isReadyForPickup;
    private boolean isDelivered;

    // Timestamp fields
    private final Date createdAt; // Order creation timestamp (final since it won't change)

    // Constructors
    public Order(int id, Map<Product, Integer> productQuantities, Payment payment) {
        this.id = id;
        this.productQuantities = new HashMap<>(productQuantities); // Create a copy to ensure immutability
        this.payment = payment;
        this.totalPrice = calculateTotalPrice();
        this.status = "Pending";
        this.isReadyForPickup = false;
        this.isDelivered = false;
        this.createdAt = new Date(); // Set creation timestamp
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map<Product, Integer> getProductQuantities() {
        return new HashMap<>(productQuantities); // Return a copy to ensure immutability
    }

    public void setProductQuantities(Map<Product, Integer> productQuantities) {
        this.productQuantities = new HashMap<>(productQuantities); // Create a copy to ensure immutability
        this.totalPrice = calculateTotalPrice(); // Recalculate total price
    }

    public double getTotalPrice() {
        return totalPrice;
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

    public String getOrderDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(createdAt); // Return the date portion
    }

    public String getOrderHour() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(createdAt); // Return the hour in HH:mm format
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    // Calculate total price
    private double calculateTotalPrice() {
        double total = 0;
        for (Map.Entry<Product, Integer> entry : productQuantities.entrySet()) {
            total += entry.getKey().getPrice() * entry.getValue(); // Product price * quantity
        }
        return total;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", productQuantities=" + productQuantities +
                ", totalPrice=" + totalPrice +
                ", payment=" + payment +
                ", status='" + status + '\'' +
                ", isReadyForPickup=" + isReadyForPickup +
                ", isDelivered=" + isDelivered +
                ", createdAt=" + getOrderDate() + " " + getOrderHour() +
                '}';
    }
}
