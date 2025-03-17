package com.example.fresh_picks;

import android.util.Log;
import com.example.fresh_picks.classes.Notification;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseManager {
    private static final String TAG = "DatabaseManager";
    private FirebaseFirestore db;
    private ListenerRegistration productListener;
    private ListenerRegistration orderListener;

    public DatabaseManager() {
        db = FirebaseFirestore.getInstance();
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
    public void startOrderStatusListener() {
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
                                    handleOrderStatusChange(userId, orderId, shippingMethod, isDelivered);
                                }
                            }
                        }
                    }
                });
    }

    // ✅ Handle order status changes
    private void handleOrderStatusChange(String userId, String orderId, String shippingMethod, Boolean isDelivered) {
        String message = null;

        if ("pickup".equalsIgnoreCase(shippingMethod)) {
            message = "📦 Your order #" + orderId + " is ready for pickup!";
        } else if (Boolean.TRUE.equals(isDelivered)) {
            message = "🚚 Your order #" + orderId + " is out for delivery!";
        }

        if (message != null) {
            Notification notification = new Notification(message);
            saveNotificationToFirestore(userId, notification);
        }
    }

    // ✅ Send a notification to users with bulkSaleBuyer = true
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
                        Notification notification = new Notification("🔥 New Super Sale: " + productName + " is now available!");
                        saveNotificationToFirestore(userId, notification);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching bulk buyers: ", e));
    }

    // ✅ Save the notification in Firestore under each user's notifications collection
    private void saveNotificationToFirestore(String userId, Notification notification) {
        // Generate a timestamp-based unique notification ID
        String timestampId = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());

        db.collection("users").document(userId)
                .collection("notifications") // 🔥 Store notifications under each user
                .document(timestampId) // ✅ Unique timestamp ID to prevent duplicates
                .set(notification)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Notification added for user: " + userId))
                .addOnFailureListener(e -> Log.e(TAG, "❌ Failed to send notification: ", e));
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
