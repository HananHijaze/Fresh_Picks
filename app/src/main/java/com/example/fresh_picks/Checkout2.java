package com.example.fresh_picks;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserDao;
import com.example.fresh_picks.DAO.UserEntity;
import com.example.fresh_picks.classes.Order;
import com.example.fresh_picks.classes.Payment;
import com.example.fresh_picks.classes.Product;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

public class Checkout2 extends AppCompatActivity {

    private FirebaseFirestore db;
    private UserDao userDao;
    private String userId, selectedLocation, shippingMethod, paymentMethod;
    private double totalPrice;
    private Map<String, Integer> cartItems;
    private Button btnTrackOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable Edge-to-Edge UI
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_checkout2);

        // Adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        userDao = AppDatabase.getInstance(this).userDao();
        btnTrackOrder = findViewById(R.id.button);

        // Get payment details from intent
        selectedLocation = getIntent().getStringExtra("selectedLocation");
        shippingMethod = getIntent().getStringExtra("shippingMethod");
        paymentMethod = getIntent().getStringExtra("paymentMethod");
        totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);

        cartItems = new HashMap<>();

        // ✅ Fetch user ID from Room Database
        getCurrentUserId(userId -> {
            if (userId != null) {
                this.userId = userId;
                Log.d("Checkout2", "User ID retrieved: " + userId);
                fetchCartItemsAndProcessOrder();
            } else {
                Toast.makeText(this, "Error: User not found!", Toast.LENGTH_LONG).show();
            }
        });

        // 🔹 Prevent going back to the previous checkout page
        btnTrackOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrdersH.class); // Replace with your tracking activity
            startActivity(intent);
            finish();
        });
    }

    private void getCurrentUserId(java.util.function.Consumer<String> callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<UserEntity> users = userDao.getAllUsers();
            final String userId = (!users.isEmpty()) ? users.get(0).getId() : null;
            runOnUiThread(() -> callback.accept(userId));
        });
    }

    private void fetchCartItemsAndProcessOrder() {
        if (userId == null || userId.isEmpty()) {
            Log.e("Checkout2", "Error: User ID is null or empty!");
            Toast.makeText(this, "Error: User not found!", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d("Checkout2", "Fetching cart for user: " + userId);

        // ✅ First, retrieve cart ID from Firestore
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("cartId")) {
                        String cartId = documentSnapshot.getString("cartId");
                        if (cartId != null && !cartId.isEmpty()) {
                            Log.d("Checkout2", "User's Cart ID: " + cartId);
                            fetchCartItemsByCartId(cartId);
                        } else {
                            Log.e("Checkout2", "Cart ID is missing in Firestore!");
                            showCartEmptyError();
                        }
                    } else {
                        Log.e("Checkout2", "Cart ID not found for user in Firestore!");
                        showCartEmptyError();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Checkout2", "Error retrieving cart ID: " + e.getMessage());
                    showCartEmptyError();
                });
    }
    private void createOrderAndPayment() {
        String orderId = UUID.randomUUID().toString(); // Generate unique order ID
        Payment payment = new Payment(userId, paymentMethod, false, null);
        Order order = new Order(orderId, cartItems, payment);
        payment.setOrder(order);

        // ✅ Store Order in Firestore
        db.collection("orders").document(orderId)
                .set(order)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Checkout2", "Order created successfully!");

                    // ✅ Store Payment in Firestore
                    String paymentId = UUID.randomUUID().toString();
                    db.collection("payments").document(paymentId)
                            .set(payment)
                            .addOnSuccessListener(v -> {
                                Log.d("Checkout2", "Payment saved successfully!");

                                // ✅ Update user's order list
                                updateUserOrders(orderId, order);

                                // ✅ Update product stock
                                updateProductStock();

                                // ✅ Clear user cart after successful order
                                clearUserCart();
                            })
                            .addOnFailureListener(e -> Log.e("Checkout2", "Failed to save payment: " + e.getMessage()));
                })
                .addOnFailureListener(e -> Log.e("Checkout2", "Failed to create order: " + e.getMessage()));
    }


    private void updateUserOrders(String orderId, Order order) {
        db.collection("users").document(userId).collection("orders").document(orderId)
                .set(order)
                .addOnSuccessListener(v -> Log.d("Checkout2", "Order linked to user profile!"))
                .addOnFailureListener(e -> Log.e("Checkout2", "Failed to update user orders: " + e.getMessage()));
    }

    private void updateProductStock() {
        for (Map.Entry<String, Integer> entry : cartItems.entrySet()) {
            String productId = entry.getKey();
            int quantityPurchased = entry.getValue();

            DocumentReference productRef = db.collection("products").document(productId);
            productRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && documentSnapshot.contains("stockQuantity")) {
                    Long currentStock = documentSnapshot.getLong("stockQuantity");
                    if (currentStock != null && currentStock >= quantityPurchased) {
                        long newStock = currentStock - quantityPurchased;
                        productRef.update("stockQuantity", newStock)
                                .addOnSuccessListener(v -> Log.d("Checkout2", "Stock updated for product: " + productId))
                                .addOnFailureListener(e -> Log.e("Checkout2", "Failed to update stock: " + e.getMessage()));
                    } else {
                        Log.e("Checkout2", "Insufficient stock for product: " + productId);
                    }
                }
            }).addOnFailureListener(e -> Log.e("Checkout2", "Failed to fetch product: " + e.getMessage()));
        }
    }

    private void clearUserCart() {
        db.collection("users").document(userId).collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("Checkout2", "Cart is already empty. Skipping deletion.");
                        return;
                    }

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        db.collection("users").document(userId).collection("cart").document(document.getId()).delete()
                                .addOnSuccessListener(v -> Log.d("Checkout2", "Deleted item: " + document.getId()))
                                .addOnFailureListener(e -> Log.e("Checkout2", "Failed to delete item: " + e.getMessage()));
                    }

                    Toast.makeText(this, "Cart cleared after payment!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e("Checkout2", "Error clearing cart: " + e.getMessage()));
    }

    private void showCartEmptyError() {
        Toast.makeText(this, "Cart is empty. Cannot complete payment!", Toast.LENGTH_LONG).show();
    }
    private void fetchCartItemsByCartId(String cartId) {
        db.collection("carts").document(cartId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists() || !documentSnapshot.contains("items")) {
                        Log.e("Checkout2", "Cart is empty or missing items field!");
                        showCartEmptyError();
                        return;
                    }

                    Object itemsObject = documentSnapshot.get("items");
                    if (!(itemsObject instanceof Map)) {
                        Log.e("Checkout2", "Incorrect format in Firestore!");
                        showCartEmptyError();
                        return;
                    }

                    Map<String, Object> itemsMap = (Map<String, Object>) itemsObject;
                    cartItems.clear();

                    for (Map.Entry<String, Object> entry : itemsMap.entrySet()) {
                        if (!(entry.getValue() instanceof Map)) {
                            Log.e("Checkout2", "Invalid cart item format: " + entry.getValue());
                            continue;
                        }

                        String productId = entry.getKey();
                        Map<String, Object> productData = (Map<String, Object>) entry.getValue();
                        Long quantity = (Long) productData.get("quantity");

                        if (quantity != null) {
                            cartItems.put(productId, quantity.intValue());
                        }
                    }

                    if (cartItems.isEmpty()) {
                        Log.e("Checkout2", "Cart is still empty after fetching!");
                        showCartEmptyError();
                        return;
                    }

                    Log.d("Checkout2", "Cart loaded successfully. Proceeding to create order.");
                    createOrderAndPayment();
                })
                .addOnFailureListener(e -> {
                    Log.e("Checkout2", "Error fetching cart: " + e.getMessage());
                    showCartEmptyError();
                });
    }

    private void validateStockAndCreateOrder() {
        for (Map.Entry<String, Integer> entry : cartItems.entrySet()) {
            String productId = entry.getKey();
            int quantityPurchased = entry.getValue();

            db.collection("products").document(productId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && documentSnapshot.contains("stockQuantity")) {
                            Long currentStock = documentSnapshot.getLong("stockQuantity");

                            if (currentStock != null && currentStock >= quantityPurchased) {
                                Log.d("Checkout2", "Stock is available for product: " + productId);
                            } else {
                                Log.e("Checkout2", "Insufficient stock for product: " + productId);
                                Toast.makeText(this, "Insufficient stock for some items!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Checkout2", "Failed to fetch product stock: " + e.getMessage()));
        }

        createOrderAndPayment();
    }

}
