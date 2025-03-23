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

    public void startOrderStatusListener(String currentUserId) {
        orderListener = db.collection("orders")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "❌ Firestore listener error: ", error);
                        return;
                    }

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "🔄 Firestore Order Change Detected: " + queryDocumentSnapshots.getDocuments());

                        for (DocumentChange change : queryDocumentSnapshots.getDocumentChanges()) {
                            Log.d(TAG, "🔥 Document Change Type: " + change.getType());

                            Log.d(TAG, "🔄 Firestore Order Change Detected: " + change.getDocument().getData());

                            if (change.getType() == DocumentChange.Type.MODIFIED) {
                                String orderId = change.getDocument().getId();
                                String userId = change.getDocument().getString("userId");
                                String shippingMethod = change.getDocument().getString("shippingMethod");
                                Boolean isDelivered = change.getDocument().getBoolean("isDelivered");

                                Log.d(TAG, "📦 Order ID: " + orderId + " | isDelivered: " + isDelivered + " | UserID: " + userId);

                                if (userId != null && isDelivered != null) {
                                    handleOrderStatusChange(userId, orderId, shippingMethod, isDelivered, currentUserId);
                                } else {
                                    Log.e(TAG, "❌ Missing userId or isDelivered field in Firestore!");
                                }
                            }
                        }
                    }
                });
    }

    private void handleOrderStatusChange(String userId, String orderId, String shippingMethod, Boolean isDelivered, String currentUserId) {
        Log.d(TAG, "🔔 Handling Order Status Change for Order " + orderId);
        Log.d(TAG, "📦 Order Details: UserID: " + userId + ", Shipping Method: " + shippingMethod + ", isDelivered: " + isDelivered);

        String message = null;

        if ("pickup".equalsIgnoreCase(shippingMethod)) {
            message = context.getString(R.string.order_ready_pickup) + " " + orderId + " " + context.getString(R.string.order_pickup_message);
        } else if (Boolean.TRUE.equals(isDelivered)) {
            message = context.getString(R.string.order_out_for_delivery) + " " + orderId + " " + context.getString(R.string.order_delivery_message);
        }

        if (message != null) {
            Log.d(TAG, "📢 Sending Firestore Notification: " + message);
            Notification notification = new Notification(message);
            saveNotificationToFirestore(userId, notification, currentUserId);
        } else {
            Log.e(TAG, "❌ Message is null! No notification sent.");
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
        if (userId == null) {
            Log.e(TAG, "❌ Cannot save notification: userId is null!");
            return;
        }

        String timestampId = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());

        db.collection("users").document(userId)
                .collection("notifications")
                .document(timestampId)
                .set(notification)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Firestore Notification Saved for User: " + userId);

                    if (currentUserId != null && userId.equals(currentUserId)) {
                        Log.d(TAG, "📢 Calling showNotification() for user: " + userId);
                        if (context instanceof MainActivity) {
                            Log.d(TAG, "📱 Triggering system notification for " + userId);
                            ((MainActivity) context).showNotification("Fresh Picks", notification.getMessage());
                        } else {
                            Log.e(TAG, "❌ Context is not an instance of MainActivity. Cannot send notification.");
                        }
                    } else {
                        Log.e(TAG, "❌ Notification not triggered for user: " + userId);
                    }

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error saving notification to Firestore: ", e);
                });
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
