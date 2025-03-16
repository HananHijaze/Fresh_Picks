package com.example.fresh_picks;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback; // Add this import

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserDao;
import com.example.fresh_picks.DAO.UserEntity;
import com.example.fresh_picks.classes.Order;
import com.example.fresh_picks.classes.Payment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
        ImageView gifImageView = findViewById(R.id.imageView2);
        Glide.with(this).asGif().load(R.drawable.gif).into(gifImageView);
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
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 🔹 Redirect user to OrdersH activity instead of going back to checkout
                Intent intent = new Intent(Checkout2.this, OrdersH.class);
                startActivity(intent);
                finish(); // Close Checkout2 so user can't return to it
            }
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
        Log.d("Checkout2", "🔍 Debug: Running fetchCartItemsAndProcessOrder()");

        if (userId == null || userId.isEmpty()) {
            Log.e("Checkout2", "Error: User ID is null or empty!");
            Toast.makeText(this, "Error: User not found!", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d("Checkout2", "Fetching cart for user: " + userId);

        // ✅ Retrieve cart ID from Firestore
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists() || !documentSnapshot.contains("cartId")) {
                        Log.e("Checkout2", "User document does not contain cartId");
                        showCartEmptyError();
                        return;
                    }

                    String cartId = documentSnapshot.getString("cartId");
                    if (cartId == null || cartId.isEmpty()) {
                        Log.e("Checkout2", "Cart ID is missing!");
                        showCartEmptyError();
                        return;
                    }

                    Log.d("Checkout2", "Retrieved Cart ID: " + cartId);

                    // ✅ Fetch the cart from Firestore
                    db.collection("carts").document(cartId).get()
                            .addOnSuccessListener(cartSnapshot -> {
                                if (!cartSnapshot.exists() || !cartSnapshot.contains("items")) {
                                    Log.e("Checkout2", "Cart is empty!");
                                    showCartEmptyError();
                                    return;
                                }

                                Object itemsObject = cartSnapshot.get("items");
                                if (!(itemsObject instanceof Map)) {
                                    Log.e("Checkout2", "Cart items field is missing or invalid!");
                                    showCartEmptyError();
                                    return;
                                }

                                Map<String, Object> firestoreItems = (Map<String, Object>) itemsObject;
                                if (firestoreItems.isEmpty()) {
                                    Log.e("Checkout2", "Cart is empty!");
                                    showCartEmptyError();
                                    return;
                                }

                                cartItems.clear();
                                for (Map.Entry<String, Object> entry : firestoreItems.entrySet()) {
                                    if (!(entry.getValue() instanceof Map)) {
                                        Log.e("Checkout2", "Invalid cart item structure.");
                                        continue;
                                    }

                                    Map<String, Object> productData = (Map<String, Object>) entry.getValue();
                                    String productId = (String) productData.get("productId");
                                    int quantity = productData.containsKey("quantity") && productData.get("quantity") != null
                                            ? ((Number) productData.get("quantity")).intValue()
                                            : 1;

                                    if (productId != null) {
                                        cartItems.put(productId, quantity);
                                        Log.d("Checkout2", "Product: " + productId + " - Quantity: " + quantity);
                                    }
                                }

                                if (cartItems.isEmpty()) {
                                    Log.e("Checkout2", "Cart is empty after filtering invalid items!");
                                    showCartEmptyError();
                                    return;
                                }

                                Log.d("Checkout2", "✅ Cart loaded successfully. Proceeding to order.");
                                validateStockAndCreateOrder();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Checkout2", "Error fetching cart: " + e.getMessage());
                                showCartEmptyError();
                            });

                })
                .addOnFailureListener(e -> {
                    Log.e("Checkout2", "Error fetching user document: " + e.getMessage());
                    showCartEmptyError();
                });
    }


    private void validateStockAndCreateOrder() {
        AtomicInteger counter = new AtomicInteger(cartItems.size());
        AtomicBoolean hasInsufficientStock = new AtomicBoolean(false);

        for (Map.Entry<String, Integer> entry : cartItems.entrySet()) {
            String productId = entry.getKey();
            int quantityPurchased = entry.getValue();

            db.collection("products").document(productId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && documentSnapshot.contains("stockQuantity")) {
                            Long currentStock = documentSnapshot.getLong("stockQuantity");

                            if (currentStock == null || currentStock < quantityPurchased) {
                                Log.e("Checkout2", "❌ Insufficient stock for product: " + productId);
                                hasInsufficientStock.set(true);
                            }
                        }

                        // ✅ Prevent order creation if stock is insufficient
                        if (hasInsufficientStock.get()) {
                            Toast.makeText(this, "Stock issue! Adjust cart.", Toast.LENGTH_SHORT).show();
                            return; // 🔹 Stop processing immediately
                        }

                        if (counter.decrementAndGet() == 0) {
                            createOrderAndPayment();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Checkout2", "❌ Failed to fetch product stock: " + e.getMessage()));
        }
    }


    private void createOrderAndPayment() {
        String orderId = UUID.randomUUID().toString();

        Payment payment = new Payment(userId, paymentMethod, false, null);
        Order order = new Order(orderId, cartItems, payment);

        // 🔹 Convert payment to a map before storing in Firestore
        Map<String, Object> paymentMap = new HashMap<>();
        paymentMap.put("userId", payment.getUserId());
        paymentMap.put("paymentMethod", payment.getPaymentMethod());
        paymentMap.put("isPaid", payment.isPaid());
        paymentMap.put("transactionId", payment.getTransactionId());

        // 🔹 Convert order to a map before storing
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("orderId", order.getId());
        orderMap.put("cartItems", order.getProductQuantities());
        orderMap.put("payment", paymentMap);  // ✅ Store payment as a map

        // ✅ Store Order in Firestore
        db.collection("orders").document(orderId)
                .set(orderMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Checkout2", "✅ Order created successfully!");

                    // ✅ Store Payment in Firestore
                    String paymentId = UUID.randomUUID().toString();
                    db.collection("payments").document(paymentId)
                            .set(paymentMap)
                            .addOnSuccessListener(v -> {
                                Log.d("Checkout2", "✅ Payment saved successfully!");
                                updateUserOrders(orderId, orderMap);
                                updateProductStock();
                                clearUserCart();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Checkout2", "❌ Failed to save payment: " + e.getMessage());
                                // 🔹 Show AlertDialog for payment failure
                                showPaymentFailureDialog();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("Checkout2", "❌ Failed to create order: " + e.getMessage());
                    // 🔹 Show AlertDialog for order failure
                    showPaymentFailureDialog();
                });

    }


    private void updateUserOrders(String orderId, Map<String, Object> orderMap) {
        db.collection("users").document(userId).collection("orders").document(orderId)
                .set(orderMap)  // 🔹 Use orderMap instead of order
                .addOnSuccessListener(v -> Log.d("Checkout2", "✅ Order linked to user profile!"))
                .addOnFailureListener(e -> Log.e("Checkout2", "❌ Failed to update user orders: " + e.getMessage()));
    }

    private void showCartEmptyError() {
        Toast.makeText(this, "Cart is empty. Cannot complete payment!", Toast.LENGTH_LONG).show();
    }
    private void clearUserCart() {
        Log.d("Checkout2", "Attempting to clear cart for user: " + userId);

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists() || !documentSnapshot.contains("cartId")) {
                        Log.d("Checkout2", "❌ Cart ID not found. Nothing to clear.");
                        return;
                    }

                    String cartId = documentSnapshot.getString("cartId");
                    if (cartId == null || cartId.isEmpty()) {
                        Log.d("Checkout2", "❌ Cart ID is missing. Nothing to clear.");
                        return;
                    }

                    // ✅ Safely clear cart without failing due to missing fields
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("items", new HashMap<>()); // Set items to an empty map

                    db.collection("carts").document(cartId)
                            .set(updates, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(v -> {
                                Log.d("Checkout2", "✅ Cart cleared successfully.");
                            })
                            .addOnFailureListener(e -> Log.e("Checkout2", "❌ Failed to clear cart: " + e.getMessage()));
                })
                .addOnFailureListener(e -> Log.e("Checkout2", "❌ Error retrieving cart ID: " + e.getMessage()));
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
    // ✅ **New Function: Show Alert Dialog if Payment Fails**
    private void showPaymentFailureDialog() {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Checkout2.this);
            builder.setTitle("Payment Failed")
                    .setMessage("Your payment could not be processed. Please try again or check your payment method.")
                    .setCancelable(false) // Prevents user from dismissing it accidentally
                    .setPositiveButton("Retry", (dialog, which) -> {
                        // 🔄 Reload Checkout2 Activity to retry payment
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    })
                    .setNegativeButton("Go Back", (dialog, which) -> {
                        // ⬅️ Redirect to Checkout page so they can change payment details
                        Intent intent = new Intent(Checkout2.this, Checkout.class);
                        startActivity(intent);
                        finish();
                    });

            AlertDialog alert = builder.create();
            alert.show();
        });
    }
}
