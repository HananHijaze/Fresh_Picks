package com.example.fresh_picks;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserDao;
import com.example.fresh_picks.DAO.UserEntity;
import com.example.fresh_picks.classes.Cart;
import com.example.fresh_picks.classes.Product;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
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
    private Cart userCart;
    private ConstraintLayout cartItemsLayout, checkoutLayout;

    public CartF() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        // ✅ Initialize Views
        recyclerViewCart = view.findViewById(R.id.recyclerViewCart);
        emptyCartText = view.findViewById(R.id.empty_cart_text);
        emptyCartImage = view.findViewById(R.id.my_cart_image);
        cartItemsLayout = view.findViewById(R.id.constraintLayoutItems);
        checkoutLayout = view.findViewById(R.id.constraintLayoutTotal);
        buttonCheckout = view.findViewById(R.id.buttonCheckout);

        // ✅ Initialize RecyclerView
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(getActivity()));
        cartItems = new ArrayList<>();
        userCart = new Cart();
        cartAdapter = new ShoppingCartAdapter(userCart, cartItems, getActivity());
        recyclerViewCart.setAdapter(cartAdapter);

        // ✅ Initialize Firestore and Room Database
        db = FirebaseFirestore.getInstance();
        userDao = AppDatabase.getInstance(getActivity()).userDao();

        // ✅ Load Cart Data
        loadCartItems();

        // ✅ Checkout Button Click
        buttonCheckout.setOnClickListener(v -> {
            if (!cartItems.isEmpty()) {
                Intent intent = new Intent(getActivity(), Checkout.class);
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), "Your cart is empty!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadCartItems() {
        getCurrentUserId(userId -> {
            if (userId == null) {
                showEmptyCart();
                return;
            }

            // ✅ Get user's cart ID from Firestore
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

    private void fetchCartItems(String cartId) {
        db.collection("carts").document(cartId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // ✅ Fetch product quantities from Firestore (Long values)
                        Map<String, Long> firestoreItems = (Map<String, Long>) documentSnapshot.get("items");

                        if (firestoreItems == null || firestoreItems.isEmpty()) {
                            showEmptyCart();
                            return;
                        }

                        // ✅ Convert Long → Integer
                        Map<String, Integer> convertedItems = new HashMap<>();
                        for (Map.Entry<String, Long> entry : firestoreItems.entrySet()) {
                            convertedItems.put(entry.getKey(), entry.getValue().intValue()); // 🔹 Convert Long to Integer
                        }

                        userCart.setItems(convertedItems); // ✅ Update local cart

                        // ✅ Fetch product details
                        cartItems.clear();
                        for (String productId : convertedItems.keySet()) {
                            db.collection("products").document(productId)
                                    .get()
                                    .addOnSuccessListener(productSnapshot -> {
                                        if (productSnapshot.exists()) {
                                            Product product = productSnapshot.toObject(Product.class);
                                            if (product != null) {
                                                cartItems.add(product);
                                            }
                                        }
                                        cartAdapter.notifyDataSetChanged();
                                    });
                        }

                        showCartItems();
                    } else {
                        showEmptyCart();
                    }
                })
                .addOnFailureListener(e -> {
                    showEmptyCart();
                    Toast.makeText(getActivity(), "Error loading cart", Toast.LENGTH_SHORT).show();
                });
    }


    private void showEmptyCart() {
        emptyCartImage.setVisibility(View.VISIBLE);
        emptyCartText.setVisibility(View.VISIBLE);
        recyclerViewCart.setVisibility(View.GONE);
        cartItemsLayout.setVisibility(View.GONE);
        checkoutLayout.setVisibility(View.GONE);
    }

    private void showCartItems() {
        emptyCartImage.setVisibility(View.GONE);
        emptyCartText.setVisibility(View.GONE);
        recyclerViewCart.setVisibility(View.VISIBLE);
        cartItemsLayout.setVisibility(View.VISIBLE);
        checkoutLayout.setVisibility(View.VISIBLE);
    }

    private void getCurrentUserId(Callback<String> callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<UserEntity> users = userDao.getAllUsers();
            String userId = (!users.isEmpty()) ? users.get(0).getId() : null;

            // Post result back to the main thread
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(userId));
        });
    }

    // Callback Interface
    public interface Callback<T> {
        void onResult(T result);
    }

}
