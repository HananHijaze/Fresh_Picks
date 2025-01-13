package com.example.fresh_picks.classes;

import java.util.List;

public class Order {
    private int id;
    private List<Product> items;
    private double totalPrice;
    private Payment payment;
    private String status; // e.g., "Pending", "Ready for Pickup", "Delivered", "Canceled"
    private boolean isReadyForPickup;
    private boolean isDelivered;

    // Constructors
    public Order(int id, List<Product> items, Payment payment) {
        this.id = id;
        this.items = items;
        this.payment = payment;
        this.totalPrice = calculateTotalPrice();
        this.status = "Pending";
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

    public List<Product> getItems() {
        return items;
    }

    public void setItems(List<Product> items) {
        this.items = items;
        this.totalPrice = calculateTotalPrice(); // Recalculate total price when items change
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

    // Methods to change order status
    public void markReadyForPickup() {
        this.isReadyForPickup = true;
        this.status = "Ready for Pickup";
    }

    public void markDelivered() {
        this.isDelivered = true;
        this.status = "Delivered";
    }

    public void cancelOrder() {
        this.status = "Canceled";
        this.isReadyForPickup = false;
        this.isDelivered = false;
    }

    // Calculate total price
    private double calculateTotalPrice() {
        double total = 0;
        for (Product product : items) {
            total += product.getPrice();
        }
        return total;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", items=" + items +
                ", totalPrice=" + totalPrice +
                ", payment=" + payment +
                ", status='" + status + '\'' +
                ", isReadyForPickup=" + isReadyForPickup +
                ", isDelivered=" + isDelivered +
                '}';
    }
}
