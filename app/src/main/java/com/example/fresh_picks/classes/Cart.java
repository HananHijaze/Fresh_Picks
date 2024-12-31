package com.example.fresh_picks.classes;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<CartItem> items;
    //gettter and setter for items
    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
    //constructor
    public Cart(List<CartItem> items) {
        this.items = items;
    }

    public Cart() {
        this.items = new ArrayList<>();
    }

    // Methods to add, remove, and clear items
    public void addItem(CartItem item) {
        items.add(item);
    }

    public void removeItem(CartItem item) {
        items.remove(item);
    }

    public void clearCart() {
        items.clear();
    }

    // Getters and a method to calculate total price
}
