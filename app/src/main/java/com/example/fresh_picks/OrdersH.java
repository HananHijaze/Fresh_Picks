package com.example.fresh_picks;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserDao;
import com.example.fresh_picks.DAO.UserEntity;
import com.example.fresh_picks.classes.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class OrdersH extends Fragment {

    private FirebaseFirestore db;
    private UserDao userDao;
    private String userId;

    private Button btnCurrentOrders, btnOrderHistory;
    private TextView tvNoOrders;
    private ScrollView scrollOrderHistory;
    private LinearLayout orderContainer;

    public OrdersH() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_orders_h, container, false);

        // Initialize Firestore & Room Database
        db = FirebaseFirestore.getInstance();
        userDao = AppDatabase.getInstance(requireContext()).userDao(); // ✅ Use requireContext()

        // Initialize UI elements
        btnCurrentOrders = view.findViewById(R.id.btn_current_orders);
        btnOrderHistory = view.findViewById(R.id.btn_order_history);
        tvNoOrders = view.findViewById(R.id.tv_no_orders);
        scrollOrderHistory = view.findViewById(R.id.scroll_order_history);
        orderContainer = view.findViewById(R.id.order_container);

        // Load User ID from Room Database
        loadUserIdFromRoom();

        // Set button click listeners
        btnCurrentOrders.setOnClickListener(v -> {
            loadOrders("current");
        });

        btnOrderHistory.setOnClickListener(v -> {
            loadOrders("history");
        });

        return view; // Return the View
    }

    private void loadUserIdFromRoom() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<UserEntity> users = userDao.getAllUsers();
            if (!users.isEmpty()) {
                userId = users.get(0).getId();
                Log.d("OrdersH", "User ID retrieved: " + userId);

                // Once userId is retrieved, load current orders by default
                requireActivity().runOnUiThread(() -> loadOrders("current"));
            } else {
                Log.e("OrdersH", "No user found in Room database!");
            }
        });
    }

    private void loadOrders(String orderType) {
        if (userId == null) {
            Log.e("OrdersH", "User ID is null, cannot fetch orders");
            showNoOrdersMessage(); // ✅ Show the message if no user found
            return;
        }

        updateButtonColors(orderType);

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("orders")) {
                        List<String> orderIds = (List<String>) documentSnapshot.get("orders");

                        if (orderIds == null || orderIds.isEmpty()) {
                            Log.d("OrdersH", "No orders found for type: " + orderType);
                            showNoOrdersMessage();
                        } else {
                            requireActivity().runOnUiThread(() -> {
                                tvNoOrders.setVisibility(View.GONE);
                                scrollOrderHistory.setVisibility(View.VISIBLE);
                            });
                            fetchOrdersFromIds(orderIds, orderType);
                        }
                    } else {
                        Log.d("OrdersH", "User document has no orders field");
                        showNoOrdersMessage();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("OrdersH", "Error fetching user orders: " + e.getMessage());
                    showNoOrdersMessage();
                });
    }

    // ✅ Helper Method to Show "No Orders Found" Message
    private void showNoOrdersMessage() {
        requireActivity().runOnUiThread(() -> {
            tvNoOrders.setText("No orders found.");
            tvNoOrders.setVisibility(View.VISIBLE);
            scrollOrderHistory.setVisibility(View.GONE);
        });
    }



    private void updateButtonColors(String selectedType) {
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.grey); // ✅ Gray color for selected
        int defaultColor = ContextCompat.getColor(requireContext(), android.R.color.white); // ✅ White color for unselected

        if (selectedType.equals("current")) {
            btnCurrentOrders.setBackgroundColor(selectedColor);
            btnCurrentOrders.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));

            btnOrderHistory.setBackgroundColor(defaultColor);
            btnOrderHistory.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey));
        } else {
            btnOrderHistory.setBackgroundColor(selectedColor);
            btnOrderHistory.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));

            btnCurrentOrders.setBackgroundColor(defaultColor);
            btnCurrentOrders.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey));
        }
    }

    private void addOrderToUI(Order order) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        CardView orderCardView = (CardView) inflater.inflate(R.layout.order_card, orderContainer, false);

        TextView tvOrderDate = orderCardView.findViewById(R.id.tv_order_date_time);
        TextView tvTotalPrice = orderCardView.findViewById(R.id.tv_total_price);
        TextView tvPaymentMethod = orderCardView.findViewById(R.id.tv_payment_method);
        TextView tvShippingMethod = orderCardView.findViewById(R.id.tv_shipping_method);
        LinearLayout productContainer = orderCardView.findViewById(R.id.product_container);

        // Set order details
        tvOrderDate.setText("Order Date: " + order.getCreatedAt());
        tvTotalPrice.setText("Total Price: ILS " + order.getTotalPrice());
        tvPaymentMethod.setText("Payment: " + order.getPaymentMethod());
        tvShippingMethod.setText("Shipping: " + order.getShippingMethod());

        // ✅ Ensure product container is cleared before adding new items
        productContainer.removeAllViews();

        if (order.getProductQuantities().isEmpty()) {
            TextView noProductsText = new TextView(requireContext());
            noProductsText.setText("No products found for this order.");
            noProductsText.setTextSize(16);
            noProductsText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
            noProductsText.setPadding(20, 10, 20, 10);
            productContainer.addView(noProductsText);
        } else {
            for (Map.Entry<String, Integer> entry : order.getProductQuantities().entrySet()) {
                CardView productCard = (CardView) inflater.inflate(R.layout.listcard1, productContainer, false);

                // Find elements in product card
                ImageView productImage = productCard.findViewById(R.id.product_image);
                TextView productPrice = productCard.findViewById(R.id.product_price);
                TextView productTitle = productCard.findViewById(R.id.product_title_text);

                // 🔹 `entry.getKey()` contains the **Product ID**, not the name
                String productId = entry.getKey();
                int quantity = entry.getValue(); // Fetch quantity

                // ✅ Fetch Product Details from Firestore
                db.collection("products").document(productId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String productName = documentSnapshot.contains("name") ? documentSnapshot.getString("name") : "Unknown";
                                String unitSize = documentSnapshot.contains("unit") ? documentSnapshot.getString("unit") : "unit";
                                double price = documentSnapshot.contains("price") ? documentSnapshot.getDouble("price") : 0.0;

                                // 🔹 Format the text correctly: "Cumin\n1kg * 13.2"
                                String formattedText = productName + "\n" + quantity + unitSize + " * " + price;
                                productTitle.setText(formattedText);

                                // ✅ Load product image using Glide
                                if (documentSnapshot.contains("imageUrl")) {
                                    String imageUrl = documentSnapshot.getString("imageUrl");
                                    Glide.with(requireContext())
                                            .load(imageUrl)
                                            .placeholder(R.drawable.noconnection3)
                                            .error(R.drawable.logo)
                                            .into(productImage);
                                } else {
                                    productImage.setImageResource(R.drawable.logo);
                                }
                            } else {
                                productTitle.setText("Product not found.");
                                productImage.setImageResource(R.drawable.logo);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("OrdersH", "Error loading product details for " + productId + ": " + e.getMessage());
                            productTitle.setText("Error loading product.");
                            productImage.setImageResource(R.drawable.logo);
                        });

                // Add product card to the container
                productContainer.addView(productCard);
            }
        }


        // ✅ Add order card to main order container
        orderContainer.addView(orderCardView);
    }

    /**
     * ✅ Helper Method to Map Product Names to Drawable Images
     */
    private int getProductImage(String productName) {
        // Convert product name to lowercase and replace spaces with underscores
        String resourceName = productName.toLowerCase().replace(" ", "_");

        // Get the resource ID dynamically
        int resId = getResources().getIdentifier(resourceName, "drawable", requireContext().getPackageName());

        // Log if the resource is missing
        if (resId == 0) {
            Log.e("OrdersH", "Image resource not found for product: " + productName);
        }

        // Return the found resource ID, or a default placeholder if not found
        return resId != 0 ? resId : R.drawable.logo; // Default placeholder image
    }
    private void fetchOrdersFromIds(List<String> orderIds, String orderType) {
        orderContainer.removeAllViews();
        final int[] processedOrders = {0};
        final boolean[] foundOrders = {false};

        for (String orderId : orderIds) {
            db.collection("orders").document(orderId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        processedOrders[0]++;
                        if (documentSnapshot.exists()) {
                            Log.d("OrdersH", "Firestore Raw Data: " + documentSnapshot.getData()); // ✅ Log raw data

                            Order order = documentSnapshot.toObject(Order.class);

                            if (order != null) {
                                Boolean delivered = order.getIsDelivered(); // 🔹 Using `getIsDelivered()`

                                Log.d("OrdersH", "Fetched Order: " + order.getCreatedAt() + " | isDelivered: " + delivered);

                                boolean isHistory = orderType.equals("history");
                                if ((isHistory && Boolean.TRUE.equals(delivered)) || (!isHistory && Boolean.FALSE.equals(delivered))) {
                                    foundOrders[0] = true;
                                    requireActivity().runOnUiThread(() -> addOrderToUI(order));
                                }
                            } else {
                                Log.e("OrdersH", "Fetched order is null!");
                            }
                        } else {
                            Log.e("OrdersH", "Order document does not exist: " + orderId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        processedOrders[0]++;
                        Log.e("OrdersH", "Error fetching order " + orderId + ": " + e.getMessage());
                    })
                    .addOnCompleteListener(task -> {
                        if (processedOrders[0] == orderIds.size() && !foundOrders[0]) {
                            showNoOrdersMessage();
                        }
                    });
        }
    }

    private String getProductPrice(String productName) {
        final String[] price = {"0"}; // Default price

        db.collection("products").document(productName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("price")) {
                        price[0] = documentSnapshot.get("price").toString(); // Get the price
                    }
                })
                .addOnFailureListener(e -> Log.e("OrdersH", "Error fetching price for " + productName + ": " + e.getMessage()));

        return price[0]; // Return the price
    }

}
