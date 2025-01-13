package com.example.fresh_picks.classes;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<Product> items;

    // Constructor
    public Cart(List<Product> items) {
        this.items = items;
    }

    // Getters and Setters
    public List<Product> getItems() {
        return items;
    }

    public void setItems(List<Product> items) {
        this.items = items;
    }

    // Methods to add, remove, and clear items
    public void addItem(Product product) {
        if (!items.contains(product)) { // Avoid duplicates
            items.add(product);
        }
    }

    public void removeItem(Product product) {
        items.remove(product);
    }

    public void clearCart() {
        items.clear();
    }

    // Method to calculate total price
    public double calculateTotalPrice() {
        double total = 0;
        for (Product product : items) {
            total += product.getPrice(); // Assuming Product has a getPrice() method
        }
        return total;
    }
}
