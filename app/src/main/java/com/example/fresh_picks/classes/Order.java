package com.example.fresh_picks.classes;

import java.util.List;

public class Order {
    private int orderId;
    private int buyerId;
    private int sellerId;
    private List<Product> productList;
    private double totalPrice;
    private String status; // e.g., "Pending", "Shipped", "Delivered"

    public Order(int orderId, int buyerId, int sellerId, List<Product> productList, double totalPrice, String status) {
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.productList = productList;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }

    // Getters and setters for order details
}
