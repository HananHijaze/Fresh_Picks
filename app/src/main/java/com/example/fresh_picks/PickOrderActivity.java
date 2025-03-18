package com.example.fresh_picks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserEntity;
import com.example.fresh_picks.R;
import com.example.fresh_picks.classes.Order;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class PickOrderActivity extends AppCompatActivity {

    private ListView orderListView;
    private ArrayAdapter<String> orderAdapter;
    private List<Order> ordersList = new ArrayList<>();
    private List<String> orderDescriptions = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId;  // User Firestore ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_order);

        orderListView = findViewById(R.id.orderListView);
        orderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, orderDescriptions);
        orderListView.setAdapter(orderAdapter);

        // Fetch User ID from Room Database
        loadUserIdFromRoom();

        orderListView.setOnItemClickListener((parent, view, position, id) -> {
            Order selectedOrder = ordersList.get(position);
            openRecipeActivity(selectedOrder);
        });
    }

    /**
     * 🔹 Load the User ID from Room Database (Local Storage)
     */
    private void loadUserIdFromRoom() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<UserEntity> users = db.userDao().getAllUsers();

            if (!users.isEmpty()) {
                userId = users.get(0).getId();
                runOnUiThread(this::fetchUserOrders);  // Once retrieved, fetch orders
            } else {
                runOnUiThread(() -> Toast.makeText(this, "No user found in local database!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    /**
     * 🔹 Fetch User's Order IDs from Firestore
     */
    private void fetchUserOrders() {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("orders")) {
                        List<String> orderIds = (List<String>) documentSnapshot.get("orders");

                        if (orderIds == null || orderIds.isEmpty()) {
                            Toast.makeText(this, "No orders found!", Toast.LENGTH_SHORT).show();
                        } else {
                            fetchOrdersByIds(orderIds);
                        }
                    } else {
                        Toast.makeText(this, "No orders found in user document!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load orders!", Toast.LENGTH_SHORT).show());
    }

    /**
     * 🔹 Fetch the last 5 Orders using their IDs from Firestore
     */
    private void fetchOrdersByIds(List<String> orderIds) {
        ordersList.clear();
        orderDescriptions.clear();

        // Get the last 5 orders only
        if (orderIds.size() > 5) {
            orderIds = orderIds.subList(orderIds.size() - 5, orderIds.size());
        }

        for (String orderId : orderIds) {
            db.collection("orders").document(orderId)
                    .get()
                    .addOnSuccessListener(orderSnapshot -> {
                        if (orderSnapshot.exists()) {
                            Order order = orderSnapshot.toObject(Order.class);
                            if (order != null) {
                                ordersList.add(order);
                                orderDescriptions.add("Order ID: " + order.getId() + " | Total: $" + order.getTotalPrice());
                                orderAdapter.notifyDataSetChanged();
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to load order: " + orderId, Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * 🔹 Open the Recipe Activity when an Order is Selected
     */
    private void openRecipeActivity(Order order) {
        Intent intent = new Intent(this, RecipeActivity.class);
        intent.putExtra("orderId", order.getId());
        startActivity(intent);
    }
}
