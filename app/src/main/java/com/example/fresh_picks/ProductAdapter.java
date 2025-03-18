package com.example.fresh_picks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserDao;
import com.example.fresh_picks.DAO.UserEntity;
import com.example.fresh_picks.R;
import com.example.fresh_picks.classes.Product;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

public class ProductAdapter extends ArrayAdapter<Product> {

    private final Context context;
    private final List<Product> products;
    private final UserDao userDao;
    private final String appLanguage;

    public ProductAdapter(Context context, List<Product> products) {
        super(context, 0, products);
        this.context = context;
        this.products = products;
        this.userDao = AppDatabase.getInstance(context).userDao();

        // 🔹 Get the app language preference
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        this.appLanguage = prefs.getString("language", Locale.getDefault().getLanguage());
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.listcard, parent, false);

        }

        // Get the product at the current position
        Product product = products.get(position);

        // Bind the product details to the UI components
        ImageView productImage = convertView.findViewById(R.id.product_image);
        TextView productPrice = convertView.findViewById(R.id.product_price);
        TextView productTitle = convertView.findViewById(R.id.product_title_text);

        // Set product data
        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.logo)
                .into(productImage);

        String productName = "ar".equals(appLanguage) && product.getNameAr() != null
                ? product.getNameAr()
                : product.getName();
        productTitle.setText(productName);
        productPrice.setText(String.format(context.getString(R.string.currency_format), product.getPrice()));

        // Set click listener to show the bottom sheet
        convertView.setOnClickListener(v -> showProductDetails(product));

        return convertView;
    }

    private void showProductDetails(Product product) {
        // Inflate the layout for the BottomSheetDialog
        View sheetView = LayoutInflater.from(context).inflate(R.layout.addtocart, null);

        // Initialize BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(sheetView);

        try {
            // Bind the views
            ImageView productImage = sheetView.findViewById(R.id.product_image);
            TextView productPrice = sheetView.findViewById(R.id.product_price);
            TextView productName = sheetView.findViewById(R.id.product_name);
            TextView quantityText = sheetView.findViewById(R.id.quantity_text);
            Button minusButton = sheetView.findViewById(R.id.quantity_minus);
            Button plusButton = sheetView.findViewById(R.id.quantity_plus);
            Button addToCartButton = sheetView.findViewById(R.id.add_to_cart_button);

            // Load product image
            Glide.with(context).load(product.getImageUrl()).into(productImage);

            // Set product details
            String localizedProductName = "ar".equals(appLanguage) && product.getNameAr() != null
                    ? product.getNameAr()
                    : product.getName();
            productName.setText(localizedProductName);
            productPrice.setText(String.format(context.getString(R.string.currency_format), product.getPrice()));

            // Handle quantity selection
            final int[] quantity = {1};
// Display quantity along with unit (e.g., "3 kg" or "2 pcs")
            final String unitText = (product.getUnit() != null) ? product.getUnit() : ""; // Handle null cases
            quantityText.setText(String.format(context.getString(R.string.quantity_format), quantity[0], unitText));
// Ensure unit is always displayed correctly

            minusButton.setOnClickListener(v -> {
                if (quantity[0] > 1) {
                    quantity[0]--;
                    quantityText.setText(quantity[0] + " " + unitText);
                }
            });

            plusButton.setOnClickListener(v -> {
                quantity[0]++;
                quantityText.setText(quantity[0] + " " + unitText);
            });


            // ✅ Fix: Properly set OnClickListener for addToCartButton
            addToCartButton.setOnClickListener(v -> {
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                getCurrentUserId(userId -> {
                    if (userId == null) {
                        Log.e("ProductAdapter", "User ID is null. Cannot proceed.");
                        Toast.makeText(context, context.getString(R.string.not_logged_in), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Retrieve Cart ID
                    getCartIdForUser(userId, new CartIdCallback() {
                        @Override
                        public void onSuccess(String cartId) {
                            int requestedQuantity = quantity[0]; // User-selected quantity

                            // Read the cart first to check if the product exists
                            db.collection("carts").document(cartId)
                                    .get()
                                    .addOnSuccessListener(cartSnapshot -> {
                                        if (cartSnapshot.exists()) {
                                            Map<String, Object> items = (Map<String, Object>) cartSnapshot.get("items");

                                            // If items exist in the cart
                                            if (items != null && items.containsKey(product.getId())) {
                                                Map<String, Object> existingProduct = (Map<String, Object>) items.get(product.getId());
                                                int currentQuantity = ((Long) existingProduct.get("quantity")).intValue();

                                                // Step 3: Check stock availability for new quantity
                                                int newTotalQuantity = currentQuantity + requestedQuantity;

                                                checkStockAvailability(product, newTotalQuantity, (isAvailable) -> {
                                                    if (isAvailable) {
                                                        // Update cart with new quantity
                                                        existingProduct.put("quantity", newTotalQuantity);
                                                        db.collection("carts").document(cartId)
                                                                .update("items." + product.getId(), existingProduct)
                                                                .addOnSuccessListener(aVoid -> {
                                                                    Log.d("ProductAdapter", "Cart updated successfully!");
                                                                    Toast.makeText(context, "Updated cart quantity!", Toast.LENGTH_SHORT).show();
                                                                    bottomSheetDialog.dismiss();
                                                                })
                                                                .addOnFailureListener(e -> Log.e("ProductAdapter", "Error updating cart: " + e.getMessage()));
                                                    } else {
                                                        Toast.makeText(context, context.getString(R.string.stock_limit), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                // Product does not exist in cart - Check stock before adding
                                                checkStockAvailability(product, requestedQuantity, (isAvailable) -> {
                                                    if (isAvailable) {
                                                        // Add new product to cart
                                                        Map<String, Object> cartItem = new HashMap<>();
                                                        cartItem.put("productId", product.getId());
                                                        cartItem.put("name", product.getName());
                                                        cartItem.put("price", product.getPrice());
                                                        cartItem.put("quantity", requestedQuantity);
                                                        cartItem.put("imageUrl", product.getImageUrl());

                                                        db.collection("carts").document(cartId)
                                                                .update("items." + product.getId(), cartItem)
                                                                .addOnSuccessListener(aVoid -> {
                                                                    Log.d("ProductAdapter", "Added to cart successfully!");
                                                                    Toast.makeText(context, context.getString(R.string.cart_added), Toast.LENGTH_SHORT).show();
                                                                    bottomSheetDialog.dismiss();
                                                                })
                                                                .addOnFailureListener(e -> Log.e("ProductAdapter", "Error adding to cart: " + e.getMessage()));
                                                    } else {
                                                        Toast.makeText(context, context.getString(R.string.stock_limit), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("ProductAdapter", "Error fetching cart: " + e.getMessage()));
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.e("ProductAdapter", "Error fetching cart ID: " + error);
                            Toast.makeText(context, "Error retrieving cart!", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });


            // Show the BottomSheetDialog
            bottomSheetDialog.show();

        } catch (ClassCastException e) {
            Log.e("ProductAdapter", "View casting error: " + e.getMessage());
            bottomSheetDialog.dismiss();
        }
    }

    private void getCurrentUserId(UserIdCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<UserEntity> users = userDao.getAllUsers(); // Fetch users in background
            String userId = (!users.isEmpty()) ? users.get(0).getId() : null;

            new Handler(Looper.getMainLooper()).post(() -> {
                callback.onSuccess(userId);
            });
        });
    }

    private void getCartIdForUser(String userId, CartIdCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("cartId")) {
                        String cartId = documentSnapshot.getString("cartId");
                        callback.onSuccess(cartId);
                    } else {
                        callback.onFailure("Cart ID not found!");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public interface UserIdCallback {
        void onSuccess(String userId);
    }

    public interface CartIdCallback {
        void onSuccess(String cartId);
        void onFailure(String error);
    }
    private void checkStockAvailability(Product product, int requestedQuantity, StockCheckCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("products").document(product.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("stockQuantity")) {
                        int stockQuantity = documentSnapshot.getLong("stockQuantity").intValue();
                        callback.onResult(stockQuantity >= requestedQuantity);
                    } else {
                        callback.onResult(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProductAdapter", "Error checking stock: " + e.getMessage());
                    callback.onResult(false);
                });
    }

    // Callback Interface
    public interface StockCheckCallback {
        void onResult(boolean isAvailable);
    }

}
