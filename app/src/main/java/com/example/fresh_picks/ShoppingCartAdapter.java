package com.example.fresh_picks;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserDao;
import com.example.fresh_picks.DAO.UserEntity;
import com.example.fresh_picks.classes.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder> {

    private final List<Product> cartItems;
    private final Context context;
    private final FirebaseFirestore db;
    private final CartF cartFragment;
    private final UserDao userDao;

    public ShoppingCartAdapter(List<Product> cartItems, Context context, CartF cartFragment) {
        this.cartItems = cartItems;
        this.context = context;
        this.cartFragment = cartFragment;
        this.db = FirebaseFirestore.getInstance();
        this.userDao = AppDatabase.getInstance(context).userDao();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.shoppingcartcard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = cartItems.get(position);

        Log.d("ShoppingCartAdapter", "Binding product: " + product.getName() + " with quantity: " + product.getStockQuantity());

        // 🔹 Retrieve saved language preference
        String appLanguage = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                .getString("language", Locale.getDefault().getLanguage());

        // 🔹 Fetch localized name from Firestore
        db.collection("products").document(product.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nameEn = documentSnapshot.getString("name");
                        String nameAr = documentSnapshot.getString("nameAr");

                        // ✅ Set product name based on the selected language
                        String productName = "ar".equals(appLanguage) && nameAr != null && !nameAr.trim().isEmpty()
                                ? nameAr
                                : nameEn;

                        holder.productName.setText(productName != null ? productName : context.getString(R.string.product_not_found));
                    }
                })
                .addOnFailureListener(e -> Log.e("ShoppingCartAdapter", "Error fetching product name: " + e.getMessage()));

        // 🔹 Fetch and update unit
        if (product.getUnit() == null || product.getUnit().isEmpty()) {
            db.collection("products").document(product.getId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fetchedUnit = documentSnapshot.contains("unit") ? documentSnapshot.getString("unit") : product.getPackSize();
                            product.setUnit(fetchedUnit != null ? fetchedUnit.trim() : "unit");

                            // ✅ Update selling price and quantity UI
                            String formattedPrice = String.format(context.getString(R.string.total_price_format), product.getPrice(), product.getUnit());
                            holder.sellingPrice.setText(formattedPrice);
                            holder.quantity.setText(product.getStockQuantity() + " " + product.getUnit());
                        }
                    })
                    .addOnFailureListener(e -> Log.e("ShoppingCartAdapter", "Error fetching product unit: " + e.getMessage()));
        }

        // ✅ **Instant UI update before waiting for Firestore response**
        holder.quantity.setText(product.getStockQuantity() + " " + (product.getUnit() != null ? product.getUnit() : "unit"));
        holder.sellingPrice.setText(String.format(context.getString(R.string.total_price_format), product.getPrice(), product.getUnit()));

        // 🔹 Load product image using Glide
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.noconnection3) // ✅ Placeholder for loading
                    .error(R.drawable.logo) // ✅ Default image on error
                    .fitCenter() // ✅ Ensures proper image fit
                    .into(holder.productImage);
        } else {
            holder.productImage.setImageResource(R.drawable.logo);
        }

        // ✅ **Optimized Increment Button**
        holder.buttonIncrement.setOnClickListener(v -> {
            int currentQuantity = product.getStockQuantity();
            int newQuantity = currentQuantity + 1;

            // 🔹 **Update UI Immediately**
            product.setStockQuantity(newQuantity);
            holder.quantity.setText(newQuantity + " " + product.getUnit());

            // 🔹 **Sync with Firestore in the background**
            updateCartQuantity(product, newQuantity);
        });

        // ✅ **Optimized Decrement Button**
        holder.buttonDecrement.setOnClickListener(v -> {
            int currentQuantity = product.getStockQuantity();
            if (currentQuantity > 1) {
                int newQuantity = currentQuantity - 1;

                // 🔹 **Update UI Immediately**
                product.setStockQuantity(newQuantity);
                holder.quantity.setText(newQuantity + " " + product.getUnit());

                // 🔹 **Sync with Firestore in the background**
                updateCartQuantity(product, newQuantity);
            } else {
                showDeleteConfirmationDialog(product, holder);
            }
        });

        // ✅ **Delete Item when Trash Can is Clicked**
        holder.buttonClose.setOnClickListener(v -> showDeleteConfirmationDialog(product, holder));
    }



    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, quantity, sellingPrice;
        ImageButton buttonClose, buttonIncrement, buttonDecrement;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            quantity = itemView.findViewById(R.id.textQuantity);
            sellingPrice = itemView.findViewById(R.id.sellingPrice);
            buttonClose = itemView.findViewById(R.id.buttonClose);
            buttonIncrement = itemView.findViewById(R.id.buttonIncrement);
            buttonDecrement = itemView.findViewById(R.id.buttonDecrement);
        }
    }

    // ✅ **Fix: Implement `getCartIdAndProceed` method**
    private void getCartIdAndProceed(Product product, CartIdCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<UserEntity> users = userDao.getAllUsers();
            if (!users.isEmpty()) {
                String userId = users.get(0).getId();
                db.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists() && documentSnapshot.contains("cartId")) {
                                callback.onSuccess(documentSnapshot.getString("cartId"));
                            } else {
                                callback.onFailure("Cart ID not found!");
                            }
                        })
                        .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
            } else {
                callback.onFailure("No user found in local database!");
            }
        });
    }

    // ✅ **Fix: Implement `CartIdCallback` interface**
    public interface CartIdCallback {
        void onSuccess(String cartId);
        void onFailure(String error);
    }

    // ✅ **Show Stock Limit Dialog**
    private void showStockLimitDialog(Product product, int availableStock) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.stock_limit))
                .setMessage(String.format(context.getString(R.string.stock_message), availableStock, product.getUnit(), product.getName()))
                .setPositiveButton(context.getString(R.string.ok), (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void showDeleteConfirmationDialog(Product product, ViewHolder holder) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.remove_item))
                .setMessage(String.format(context.getString(R.string.remove_message), product.getName()))
                .setPositiveButton(context.getString(R.string.yes), (dialog, which) -> removeItemFromCart(product))
                .setNegativeButton(context.getString(R.string.cancel), (dialog, which) -> holder.quantity.setText("1"))
                .show();
    }

    // ✅ **Remove an Item from the Cart**
    private void removeItemFromCart(Product product) {
        getCartIdAndProceed(product, new CartIdCallback() {
            @Override
            public void onSuccess(String cartId) {
                db.collection("carts").document(cartId)
                        .update("items." + product.getId(), null)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Removed from cart", Toast.LENGTH_SHORT).show();
                            cartItems.remove(product);
                            notifyDataSetChanged();
                            cartFragment.loadCartItems();
                        })
                        .addOnFailureListener(e -> Log.e("ShoppingCartAdapter", "Error removing item: " + e.getMessage()));
            }

            @Override
            public void onFailure(String error) {
                Log.e("ShoppingCartAdapter", "Failed to get cart ID: " + error);
            }
        });
    }
    private void updateCartQuantity(Product product, int newQuantity) {
        getCartIdAndProceed(product, new CartIdCallback() {
            @Override
            public void onSuccess(String cartId) {
                db.collection("carts").document(cartId)
                        .update("items." + product.getId() + ".quantity", newQuantity)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("ShoppingCartAdapter", "Cart quantity updated in Firestore: " + newQuantity);
                        })
                        .addOnFailureListener(e -> Log.e("ShoppingCartAdapter", "Error updating quantity: " + e.getMessage()));
            }

            @Override
            public void onFailure(String error) {
                Log.e("ShoppingCartAdapter", "Failed to get cart ID: " + error);
            }
        });

        // ✅ **Update UI instantly**
        product.setStockQuantity(newQuantity);
        notifyDataSetChanged();

        // 🔹 **Calculate Total Price & Pass It**
        double totalPrice = 0.0;
        for (Product item : cartItems) {
            totalPrice += item.getPrice() * item.getStockQuantity();
        }

        cartFragment.updateTotalPrice(totalPrice); // ✅ Correct: Pass the total price
    }



}