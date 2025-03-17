package com.example.fresh_picks;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

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

        // ✅ Prevent going back to avoid duplicate actions
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish(); // Close activity when back is pressed
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

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists() || !documentSnapshot.contains("cartId")) {
                        showCartEmptyError();
                        return;
                    }

                    String cartId = documentSnapshot.getString("cartId");
                    if (cartId == null || cartId.isEmpty()) {
                        showCartEmptyError();
                        return;
                    }

                    db.collection("carts").document(cartId).get()
                            .addOnSuccessListener(cartSnapshot -> {
                                if (!cartSnapshot.exists() || !cartSnapshot.contains("items")) {
                                    showCartEmptyError();
                                    return;
                                }

                                Map<String, Object> firestoreItems = (Map<String, Object>) cartSnapshot.get("items");
                                if (firestoreItems == null || firestoreItems.isEmpty()) {
                                    showCartEmptyError();
                                    return;
                                }

                                cartItems.clear();
                                for (Map.Entry<String, Object> entry : firestoreItems.entrySet()) {
                                    Map<String, Object> productData = (Map<String, Object>) entry.getValue();
                                    String productId = (String) productData.get("productId");
                                    int quantity = ((Number) productData.getOrDefault("quantity", 1)).intValue();
                                    if (productId != null && quantity > 0) {
                                        cartItems.put(productId, quantity);
                                    }
                                }

                                if (cartItems.isEmpty()) {
                                    showCartEmptyError();
                                    return;
                                }

                                validateStockAndCreateOrder();
                            })
                            .addOnFailureListener(e -> showCartEmptyError());

                })
                .addOnFailureListener(e -> showCartEmptyError());
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
                                hasInsufficientStock.set(true);
                            }
                        }

                        if (hasInsufficientStock.get()) {
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Stock issue! Adjust cart.", Toast.LENGTH_SHORT).show();
                            });
                            return; // 🚀 Stops execution if stock is insufficient
                        }

                        if (counter.decrementAndGet() == 0) {
                            createOrderAndPayment();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Checkout2", "❌ Failed to fetch product stock: " + e.getMessage()));
        }
    }

    private void createOrderAndPayment() {
        Executors.newSingleThreadExecutor().execute(() -> {
            String orderId = UUID.randomUUID().toString();
            String createdAt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    .format(new java.util.Date());
            boolean isDelivered = false; // ✅ New orders are not delivered yet

            // ✅ Create Order Object
            Order order = new Order(orderId, cartItems, isDelivered, createdAt, totalPrice, paymentMethod, shippingMethod);

            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.getId());
            orderMap.put("productQuantities", order.getProductQuantities());
            orderMap.put("isDelivered", order.getIsDelivered());
            orderMap.put("createdAt", order.getCreatedAt());
            orderMap.put("totalPrice", order.getTotalPrice());
            orderMap.put("paymentMethod", order.getPaymentMethod());
            orderMap.put("shippingMethod", order.getShippingMethod());

            db.collection("orders").document(orderId)
                    .set(orderMap)
                    .addOnSuccessListener(aVoid -> {
                        updateUserOrders(orderId);
                        checkAndUpdateBulkSaleStatus();
                        clearUserCart();
                        runOnUiThread(() -> {
                            Toast.makeText(this, "🎉 Order placed successfully!", Toast.LENGTH_LONG).show();
                            redirectToMainActivity();
                        });
                    })
                    .addOnFailureListener(e -> runOnUiThread(this::showPaymentFailureDialog));
        });
    }


    private void updateUserOrders(String orderId) {
        db.collection("users").document(userId)
                .update("orders", FieldValue.arrayUnion(orderId))
                .addOnSuccessListener(v -> Log.d("Checkout2", "✅ Order linked to user profile!"))
                .addOnFailureListener(e -> Log.e("Checkout2", "❌ Failed to update user orders: " + e.getMessage()));
    }

    private void redirectToMainActivity() {
        Intent intent = new Intent(Checkout2.this, MainActivity2.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showCartEmptyError() {
        Toast.makeText(this, "Cart is empty. Cannot complete payment!", Toast.LENGTH_LONG).show();
    }

    private void showPaymentFailureDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Payment Failed")
                .setMessage("Your payment could not be processed. Please try again.")
                .setPositiveButton("Retry", (dialog, which) -> recreate())
                .show();
    }
    private void clearUserCart() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists() || !documentSnapshot.contains("cartId")) {
                        Log.e("Checkout2", "Cart ID not found for user!");
                        return;
                    }

                    String cartId = documentSnapshot.getString("cartId");
                    if (cartId == null || cartId.isEmpty()) {
                        Log.e("Checkout2", "Cart ID is empty.");
                        return;
                    }

                    // ✅ Check if cart exists before clearing
                    db.collection("carts").document(cartId).get()
                            .addOnSuccessListener(cartSnapshot -> {
                                if (!cartSnapshot.exists()) {
                                    Log.e("Checkout2", "Cart document does not exist!");
                                    return;
                                }

                                // ✅ Clear only if cart exists
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("items", new HashMap<>());

                                db.collection("carts").document(cartId)
                                        .set(updates, SetOptions.merge())
                                        .addOnSuccessListener(aVoid -> Log.d("Checkout2", "✅ Cart cleared successfully!"))
                                        .addOnFailureListener(e -> Log.e("Checkout2", "❌ Failed to clear cart!", e));
                            })
                            .addOnFailureListener(e -> Log.e("Checkout2", "❌ Error checking cart existence!", e));
                })
                .addOnFailureListener(e -> Log.e("Checkout2", "❌ Failed to retrieve user document!", e));
    }
    private void checkAndUpdateBulkSaleStatus() {
        AtomicBoolean hasSuperDealItem = new AtomicBoolean(false);
        AtomicInteger counter = new AtomicInteger(cartItems.size());

        // Step 1: First, check if the user is already a bulk sale buyer
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("bulkSaleBuyer")) {
                        boolean isAlreadyBulkBuyer = Boolean.TRUE.equals(documentSnapshot.getBoolean("bulkSaleBuyer"));
                        if (isAlreadyBulkBuyer) {
                            Log.d("Checkout2", "✅ User is already a bulk sale buyer. No update needed.");
                            return; // Exit early, no need to check further
                        }
                    }

                    // Step 2: Check if the user bought from 'super_deals' category
                    for (String productId : cartItems.keySet()) {
                        db.collection("products").document(productId).get()
                                .addOnSuccessListener(productSnapshot -> {
                                    if (productSnapshot.exists() && productSnapshot.contains("category")) {
                                        String category = productSnapshot.getString("category");
                                        if ("super_deals".equalsIgnoreCase(category)) {
                                            hasSuperDealItem.set(true);
                                        }
                                    }

                                    // Step 3: After checking all products, update Firestore if necessary
                                    if (counter.decrementAndGet() == 0 && hasSuperDealItem.get()) {
                                        db.collection("users").document(userId)
                                                .update("bulkSaleBuyer", true)
                                                .addOnSuccessListener(v -> Log.d("Checkout2", "✅ User is now a bulk sale buyer!"))
                                                .addOnFailureListener(e -> {
                                                    Log.e("Checkout2", "❌ Failed to update bulkSaleBuyer status: " + e.getMessage());
                                                    runOnUiThread(() ->
                                                            Toast.makeText(this, "Failed to update user status", Toast.LENGTH_SHORT).show()
                                                    );
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("Checkout2", "❌ Error fetching product category: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> Log.e("Checkout2", "❌ Failed to check bulkSaleBuyer status: " + e.getMessage()));
    }

}
