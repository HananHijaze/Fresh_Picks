package com.example.fresh_picks;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserDao;
import com.example.fresh_picks.DAO.UserEntity;
import com.example.fresh_picks.classes.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class CartF extends Fragment {

    private RecyclerView recyclerViewCart;
    private ShoppingCartAdapter cartAdapter;
    private List<Product> cartItems;
    private FirebaseFirestore db;
    private UserDao userDao;
    private TextView emptyCartText;
    private ImageView emptyCartImage;
    private Button buttonCheckout;
    private ConstraintLayout cartItemsLayout, checkoutLayout;

    public CartF() {
        // Required empty public constructor
    }

    private TextView textTotalPrice; // Add this as a class variable

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        // ✅ Initialize UI Elements
        textTotalPrice = view.findViewById(R.id.textTotalPrice); // Find Total Price TextView
        emptyCartImage = view.findViewById(R.id.my_cart_image);
        emptyCartText = view.findViewById(R.id.empty_cart_text);
        recyclerViewCart = view.findViewById(R.id.recyclerViewCart);
        cartItemsLayout = view.findViewById(R.id.constraintLayoutItems);
        checkoutLayout = view.findViewById(R.id.constraintLayoutTotal);
        buttonCheckout = view.findViewById(R.id.buttonCheckout); // Find Checkout Button

        recyclerViewCart.setLayoutManager(new LinearLayoutManager(getActivity()));
        cartItems = new ArrayList<>();
        cartAdapter = new ShoppingCartAdapter(cartItems, getActivity(), this);
        recyclerViewCart.setAdapter(cartAdapter);

        db = FirebaseFirestore.getInstance();
        userDao = AppDatabase.getInstance(getActivity()).userDao();

        // ✅ Load Cart Items
        loadCartItems();
        buttonCheckout.setOnClickListener(v -> {
            if (!cartItems.isEmpty()) {
                Intent intent = new Intent(getActivity(), Checkout.class);
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), getString(R.string.cart_empty_message), Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    private void fetchCartItems(String cartId) {
        db.collection("carts").document(cartId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Object itemsObject = documentSnapshot.get("items");
                        if (itemsObject == null) {
                            Log.d("CartF", "Cart is empty from Firestore (items field is null)");
                            showEmptyCart();
                            return;
                        }

                        Map<String, Map<String, Object>> firestoreItems =
                                (Map<String, Map<String, Object>>) itemsObject;

                        Log.d("CartF", "Retrieved cart data from Firestore: " + firestoreItems);

                        if (firestoreItems.isEmpty()) {
                            Log.d("CartF", "Cart is empty from Firestore");
                            showEmptyCart();
                            return;
                        }

                        cartItems.clear(); // Reset list before adding new items
                        double totalPrice = 0.0; // ✅ Use local variable instead of array

                        for (Map.Entry<String, Map<String, Object>> entry : firestoreItems.entrySet()) {
                            Map<String, Object> productData = entry.getValue();

                            if (productData == null) continue; // Avoid NullPointerException

                            String productId = (String) productData.get("productId");
                            String name = (String) productData.get("name");
                            double price = productData.containsKey("price") ? ((Number) productData.get("price")).doubleValue() : 0.0;
                            int quantity = productData.containsKey("quantity") && productData.get("quantity") != null
                                    ? ((Number) productData.get("quantity")).intValue()
                                    : 1;

                            String imageUrl = productData.containsKey("imageUrl") ? (String) productData.get("imageUrl") : null;
                            totalPrice += price * quantity; // ✅ Correct calculation
                            Log.d("CartF", "Product: " + name + " - Quantity: " + quantity + " - Price: " + price);

                            Product product = new Product(productId, name, null, null, price, null, quantity, null,
                                    null, null, false, false, null, null, imageUrl, 0);

                            cartItems.add(product);
                        }

                        Log.d("CartF", "Total products loaded: " + cartItems.size());
                        Log.d("CartF", "✅ Total price calculated correctly: ₪" + totalPrice);

                        // ✅ Update total price only once
                        updateTotalPrice(totalPrice);
                        cartAdapter.notifyDataSetChanged();
                        showCartItems();
                    } else {
                        Log.d("CartF", "No cart document found in Firestore");
                        showEmptyCart();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CartF", "Error loading cart", e);
                    showEmptyCart();
                    Toast.makeText(getActivity(), getString(R.string.cart_error_loading), Toast.LENGTH_SHORT).show();
                });
    }



    public void updateTotalPrice(double totalPrice) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() ->
                    textTotalPrice.setText(String.format(getString(R.string.total_price1), totalPrice))
            );
        }
    }




    // 🔹 **Fetch User's Cart Items from Firestore**
    public void loadCartItems() {
        getCurrentUserId(userId -> {
            if (userId == null) {
                showEmptyCart();
                return;
            }

            // Get user's cart ID from Firestore
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && documentSnapshot.contains("cartId")) {
                            String cartId = documentSnapshot.getString("cartId");
                            fetchCartItems(cartId);
                        } else {
                            showEmptyCart();
                        }
                    })
                    .addOnFailureListener(e -> {
                        showEmptyCart();
                        Toast.makeText(getActivity(), "Error loading cart", Toast.LENGTH_SHORT).show();
                    });
        });
    }





    // 🔹 **Get Current User ID from Room Database**
    private void getCurrentUserId(Callback<String> callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<UserEntity> users = userDao.getAllUsers();
            final String userId = (!users.isEmpty()) ? users.get(0).getId() : null; // ✅ Correct
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(userId));
        });
    }


    // ✅ Callback Interface for Async Calls
    public interface Callback<T> {
        void onResult(T result);
    }
    // 🔹 **Show Empty Cart Message**
    public void showEmptyCart() {
        Log.d("CartF", "Cart is empty, hiding UI elements");

        // ✅ Show Empty Cart Image and Message
        emptyCartImage.setVisibility(View.VISIBLE);
        emptyCartText.setVisibility(View.VISIBLE);

        // ❌ Hide RecyclerView, Checkout Button, and Total Price
        recyclerViewCart.setVisibility(View.GONE);
        cartItemsLayout.setVisibility(View.GONE);
        checkoutLayout.setVisibility(View.GONE);
        buttonCheckout.setVisibility(View.GONE);
        textTotalPrice.setVisibility(View.GONE);
    }

    private void showCartItems() {
        Log.d("CartF", "Showing cart items, count: " + cartItems.size());

        // ✅ Check if the fragment is still attached to an activity
        FragmentActivity activity = getActivity();
        if (activity == null || !isAdded()) {
            Log.e("CartF", "Fragment is not attached! Skipping UI update.");
            return;
        }

        activity.runOnUiThread(() -> {
            if (cartItems.isEmpty()) {
                showEmptyCart(); // ✅ If no items, show empty cart UI
            } else {
                // ✅ Hide Empty Cart Image and Message
                if (emptyCartImage != null) emptyCartImage.setVisibility(View.GONE);
                if (emptyCartText != null) emptyCartText.setVisibility(View.GONE);

                // ✅ Show RecyclerView, Checkout Button, and Total Price
                if (recyclerViewCart != null) recyclerViewCart.setVisibility(View.VISIBLE);
                if (cartItemsLayout != null) cartItemsLayout.setVisibility(View.VISIBLE);
                if (checkoutLayout != null) checkoutLayout.setVisibility(View.VISIBLE);
                if (buttonCheckout != null) buttonCheckout.setVisibility(View.VISIBLE);
                if (textTotalPrice != null) textTotalPrice.setVisibility(View.VISIBLE);
            }
        });
    }

}