package com.example.fresh_picks.classes;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Cart {
    private String cartId;
    private Map<String, Integer> items; // Store productId → quantity
    private FirebaseFirestore db; // Firestore instance

    // Constructor
    public Cart() {
        this.cartId = UUID.randomUUID().toString(); // Generate unique cart ID
        this.items = new HashMap<>();
        this.db = FirebaseFirestore.getInstance(); // Initialize Firestore
        updateCartInFirestore(); // Save cart immediately in Firestore
    }

    // Getter for cartId
    public String getCartId() {
        return cartId;
    }

    // Get all cart items
    public Map<String, Integer> getItems() {
        return items;
    }

    // Set cart items and update Firestore
    public void setItems(Map<String, Integer> items) {
        this.items = items;
        updateCartInFirestore(); // Update Firestore when setting new items
    }

    // Add item and update quantity in Firestore immediately
    public void addItem(Product product, int quantity) {
        String productId = product.getId();
        if (items.containsKey(productId)) {
            items.put(productId, items.get(productId) + quantity);
        } else {
            items.put(productId, quantity);
        }
        updateCartInFirestore(); // Instantly update Firestore
    }

    // Remove item and update Firestore immediately
    public void removeItem(Product product) {
        String productId = product.getId();
        if (items.containsKey(productId)) {
            items.remove(productId);
            updateCartInFirestore();
        }
    }

    // Update quantity of an item in Firestore
    public void updateQuantity(Product product, int quantity) {
        String productId = product.getId();
        if (items.containsKey(productId)) {
            if (quantity > 0) {
                items.put(productId, quantity);
            } else {
                items.remove(productId);
            }
            updateCartInFirestore();
        }
    }

    // Clear cart and update Firestore
    public void clearCart() {
        items.clear();
        updateCartInFirestore();
    }

    // Calculate total price
    public double calculateTotalPrice(Map<String, Product> productMap) {
        double total = 0;
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            Product product = productMap.get(entry.getKey()); // Fetch product details
            if (product != null) {
                total += product.getPrice() * entry.getValue();
            }
        }
        return total;
    }

    // **🔥 Immediate Firestore Update**
    private void updateCartInFirestore() {
        Map<String, Object> cartMap = new HashMap<>();
        cartMap.put("cartId", cartId);
        cartMap.put("items", items); // Store products list with quantities

        db.collection("carts")
                .document(cartId)
                .set(cartMap)
                .addOnSuccessListener(aVoid -> System.out.println("Cart updated successfully!"))
                .addOnFailureListener(e -> System.err.println("Error updating cart: " + e.getMessage()));
    }
}
