package com.example.fresh_picks.classes;

import android.util.Log;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Cart {
    private String cartId;
    private Map<String, Integer> items; // Store productId → quantity
    private FirebaseFirestore db; // Firestore instance

    // Constructor: Load existing cart if available, otherwise create a new one
    public Cart(String userId) {
        this.db = FirebaseFirestore.getInstance();
        this.items = new HashMap<>();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("cartId")) {
                        this.cartId = documentSnapshot.getString("cartId");
                        if (cartId != null) {
                            loadCartFromFirestore();
                        } else {
                            createNewCart(userId);
                        }
                    } else {
                        createNewCart(userId);
                    }
                })
                .addOnFailureListener(e -> Log.e("Cart", "Error fetching user cart ID: " + e.getMessage()));
    }

    // Create new cart and save to Firestore
    private void createNewCart(String userId) {
        this.cartId = UUID.randomUUID().toString();
        db.collection("users").document(userId)
                .update("cartId", cartId)
                .addOnSuccessListener(aVoid -> Log.d("Cart", "Cart ID linked to user: " + userId))
                .addOnFailureListener(e -> Log.e("Cart", "Error linking cart ID: " + e.getMessage()));

        updateCartInFirestore();
    }

    // Load cart data from Firestore
    private void loadCartFromFirestore() {
        db.collection("carts").document(cartId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("items")) {
                        this.items = (Map<String, Integer>) documentSnapshot.get("items");
                        if (this.items == null) {
                            this.items = new HashMap<>();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Cart", "Error loading cart: " + e.getMessage()));
    }

    // Get cart items
    public Map<String, Integer> getItems() {
        return items;
    }

    // Add item and update Firestore
    public void addItem(Product product, int quantity) {
        String productId = product.getId();
        items.put(productId, items.getOrDefault(productId, 0) + quantity);
        updateCartInFirestore();
    }

    // Remove item from cart
    public void removeItem(Product product) {
        items.remove(product.getId());
        updateCartInFirestore();
    }

    // Update item quantity
    public void updateQuantity(Product product, int quantity) {
        if (quantity > 0) {
            items.put(product.getId(), quantity);
        } else {
            items.remove(product.getId());
        }
        updateCartInFirestore();
    }

    // Clear cart
    public void clearCart() {
        items.clear();
        updateCartInFirestore();
    }

    // Calculate total price
    public double calculateTotalPrice(Map<String, Product> productMap) {
        double total = 0;
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            Product product = productMap.get(entry.getKey());
            if (product != null) {
                total += product.getPrice() * entry.getValue();
            }
        }
        return total;
    }

    // ✅ **Optimized Firestore Update**
    private void updateCartInFirestore() {
        if (cartId == null) {
            Log.e("Cart", "Cart ID is null, cannot update Firestore.");
            return;
        }

        Map<String, Object> cartMap = new HashMap<>();
        cartMap.put("items", items);

        db.collection("carts").document(cartId)
                .set(cartMap, SetOptions.merge()) // Use merge to avoid overwriting cart ID
                .addOnSuccessListener(aVoid -> Log.d("Cart", "Cart updated successfully!"))
                .addOnFailureListener(e -> Log.e("Cart", "Error updating cart: " + e.getMessage()));
    }
}
