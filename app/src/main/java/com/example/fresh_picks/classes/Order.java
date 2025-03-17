package com.example.fresh_picks.classes;

import java.util.HashMap;
import java.util.Map;

public class Order {
    private String id;
    private Map<String, Integer> productQuantities;
    private boolean isDelivered;
    private String createdAt;
    private double totalPrice;
    private String paymentMethod;
    private String shippingMethod;

    // ✅ Updated constructor
    public Order(String id, Map<String, Integer> productQuantities, boolean isDelivered, String createdAt, double totalPrice, String paymentMethod, String shippingMethod) {
        this.id = id;
        this.productQuantities = productQuantities;
        this.isDelivered = isDelivered;
        this.createdAt = createdAt;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.shippingMethod = shippingMethod;
    }
    public Order() {}

    // ✅ Add a getter for the ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setProductQuantities(Map<String, Integer> productQuantities) {
        this.productQuantities = productQuantities;
    }

    public void setDelivered(boolean delivered) {
        isDelivered = delivered;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public Map<String, Integer> getProductQuantities() {
        return productQuantities;
    }

    public Boolean getIsDelivered() {  // 🔹 Change from `isDelivered()` to `getIsDelivered()`
        return isDelivered;
    }

    public void setIsDelivered(Boolean isDelivered) {
        this.isDelivered = isDelivered;
    }


    public String getCreatedAt() {
        return createdAt;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }
}
