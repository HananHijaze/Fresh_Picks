package com.example.fresh_picks;

import android.content.Context;
import android.util.Log;
import com.example.fresh_picks.classes.Notification;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseManager {
    private static final String TAG = "DatabaseManager";
    private FirebaseFirestore db;
    private ListenerRegistration productListener;
    private ListenerRegistration orderListener;
    private Context context; // ✅ Added context reference

    public DatabaseManager(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.context = context; // ✅ Initialize context
    }

    // ✅ Start listening for new super sales
    public void startSuperSaleListener() {
        productListener = db.collection("products")
                .whereEqualTo("category", "super_deals") // 🔥 Listen for Super Deals
                .orderBy("timestamp", Query.Direction.DESCENDING) // ✅ Latest sales first
                .limit(1) // ✅ Only latest added sale
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening for super deals: ", error);
                        return;
                    }

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentChange change : queryDocumentSnapshots.getDocumentChanges()) {
                            if (change.getType() == DocumentChange.Type.ADDED) { // 🔥 New sale detected
                                String productName = change.getDocument().getString("name");
                                sendNotificationToBulkBuyers(productName);
                            }
                        }
                    }
                });
    }

    // ✅ Start listening for order status updates
    public void startOrderStatusListener(String currentUserId) { // 🔥 Pass current user ID
        orderListener = db.collection("orders")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening for order updates: ", error);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        for (DocumentChange change : queryDocumentSnapshots.getDocumentChanges()) {
                            if (change.getType() == DocumentChange.Type.MODIFIED) { // ✅ Order was updated
                                String orderId = change.getDocument().getId();
                                String userId = change.getDocument().getString("userId");
                                String shippingMethod = change.getDocument().getString("shippingMethod");
                                Boolean isDelivered = change.getDocument().getBoolean("isDelivered");

                                if (userId != null) {
                                    handleOrderStatusChange(userId, orderId, shippingMethod, isDelivered, currentUserId);
                                }
                            }
                        }
                    }
                });
    }

    // ✅ Handle order status changes
    private void handleOrderStatusChange(String userId, String orderId, String shippingMethod, Boolean isDelivered, String currentUserId) {
        String message = null;

        if ("pickup".equalsIgnoreCase(shippingMethod)) {
            message = context.getString(R.string.order_ready_pickup) + orderId + context.getString(R.string.order_pickup_message);
        } else if (Boolean.TRUE.equals(isDelivered)) {
            message = context.getString(R.string.order_out_for_delivery) + orderId + context.getString(R.string.order_delivery_message);
        }

        if (message != null) {
            Notification notification = new Notification(message);
            saveNotificationToFirestore(userId, notification, currentUserId);
        }
    }

    private void sendNotificationToBulkBuyers(String productName) {
        db.collection("users")
                .whereEqualTo("bulkSaleBuyer", true) // ✅ Only bulk buyers
                .get()
                .addOnSuccessListener(usersSnapshot -> {
                    List<String> userIds = new ArrayList<>();
                    for (DocumentSnapshot userDoc : usersSnapshot) {
                        String userId = userDoc.getId();
                        userIds.add(userId);
                    }

                    for (String userId : userIds) {
                        String message = context.getString(R.string.new_super_sale) + productName + context.getString(R.string.super_sale_available);
                        Notification notification = new Notification(message);
                        saveNotificationToFirestore(userId, notification, null);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, context.getString(R.string.error_fetch_bulk_buyers), e));
    }


    private void saveNotificationToFirestore(String userId, Notification notification, String currentUserId) {
        String timestampId = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());

        db.collection("users").document(userId)
                .collection("notifications") // 🔥 Store notifications under each user
                .document(timestampId) // ✅ Unique timestamp ID to prevent duplicates
                .set(notification)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, context.getString(R.string.notification_added) + userId);

                    // ✅ Send system notification to logged-in user only
                    if (currentUserId != null && userId.equals(currentUserId)) {
                        ((MainActivity) context).showNotification("Fresh Picks", notification.getMessage());
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, context.getString(R.string.notification_failed), e));
    }


    // ✅ Stop all listeners when not needed
    public void stopListeners() {
        if (productListener != null) {
            productListener.remove();
            productListener = null;
        }
        if (orderListener != null) {
            orderListener.remove();
            orderListener = null;
        }
    }
}
