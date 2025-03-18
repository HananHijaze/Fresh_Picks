package com.example.fresh_picks;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserEntity;
import com.example.fresh_picks.classes.Order;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;

public class PickOrderActivity extends AppCompatActivity {

    private RecyclerView orderRecyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> ordersList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_order);

        orderRecyclerView = findViewById(R.id.orderRecyclerView);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(this, ordersList);
        orderRecyclerView.setAdapter(orderAdapter);

        loadUserIdFromRoom();
    }

    private void loadUserIdFromRoom() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<UserEntity> users = db.userDao().getAllUsers();

            if (!users.isEmpty()) {
                userId = users.get(0).getId();
                runOnUiThread(this::fetchUserOrders);
            } else {
                runOnUiThread(() -> Toast.makeText(this, "No user found in local database!", Toast.LENGTH_SHORT).show());
            }
        });
    }

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
                        if (orderIds != null && !orderIds.isEmpty()) {
                            fetchOrdersByIds(orderIds);
                        } else {
                            Toast.makeText(this, "No orders found!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No orders found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load orders!", Toast.LENGTH_SHORT).show());
    }

    private void fetchOrdersByIds(List<String> orderIds) {
        ordersList.clear();
        final int[] processedOrders = {0};

        for (String orderId : orderIds) {
            db.collection("orders").document(orderId)
                    .get()
                    .addOnSuccessListener(orderSnapshot -> {
                        processedOrders[0]++;
                        if (orderSnapshot.exists()) {
                            Order order = orderSnapshot.toObject(Order.class);
                            if (order != null) {
                                ordersList.add(order);
                            }
                        }
                        if (processedOrders[0] == orderIds.size()) {
                            sortAndDisplayOrders();
                        }
                    })
                    .addOnFailureListener(e -> {
                        processedOrders[0]++;
                        Toast.makeText(this, "Failed to load order: " + orderId, Toast.LENGTH_SHORT).show();
                        if (processedOrders[0] == orderIds.size()) {
                            sortAndDisplayOrders();
                        }
                    });
        }
    }

    private void sortAndDisplayOrders() {
        // Sort orders by latest first
        Collections.sort(ordersList, (o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));

        // Limit to last 6 orders
        if (ordersList.size() > 6) {
            ordersList = ordersList.subList(0, 6);
        }

        orderAdapter.updateOrders(ordersList);
    }
}
