package com.example.fresh_picks;

import com.example.fresh_picks.classes.Product;

import java.util.List;

public class Cart {
    private int buyerId;
    private List<Product> productList;
    private double totalPrice;

    public Cart(int buyerId, List<Product> productList) {
        this.buyerId = buyerId;
        this.productList = productList;
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        totalPrice = productList.stream().mapToDouble(Product::getPrice).sum();
    }

    public void addProduct(Product product) {
        productList.add(product);
        calculateTotalPrice();
    }

    public void removeProduct(Product product) {
        productList.remove(product);
        calculateTotalPrice();
    }

    // Getters for cart details
}
