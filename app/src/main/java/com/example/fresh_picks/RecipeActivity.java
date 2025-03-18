package com.example.fresh_picks;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserDao;
import com.example.fresh_picks.DAO.UserEntity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import okhttp3.*;

public class RecipeActivity extends AppCompatActivity {

    private TextView recipeTextView, recipeTitle;
    private ProgressBar loadingProgressBar;
    private Button generateRecipeButton;
    private FirebaseFirestore db;
    private String userId;
    private UserDao userDao;
    private String latestOrderId; // ✅ Store latest order ID

    private static final String HF_API_URL = "https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.1";
    private static String HF_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        // Initialize Firestore & Room Database
        db = FirebaseFirestore.getInstance();
        userDao = AppDatabase.getInstance(getApplicationContext()).userDao();

        // Initialize UI elements
        recipeTextView = findViewById(R.id.recipeResult);
        recipeTitle = findViewById(R.id.recipeTitle);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        generateRecipeButton = findViewById(R.id.generateRecipeButton);
        HF_API_KEY = getString(R.string.huggingface_api_key);

        // 🔹 Load User ID & Fetch Order when the Activity starts
        generateRecipeButton.setEnabled(false);
        loadUserIdFromRoom();

        // 🔹 Set Button Click Listener
        generateRecipeButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            recipeTextView.setText(""); // ✅ Clear old recipe
            recipeTextView.setVisibility(View.GONE);
            recipeTitle.setVisibility(View.GONE);
            generateRecipeButton.setEnabled(false);

            // Fetch updated order details
            fetchLatestOrderId();
        });
    }

    /**
     * 🔹 Re-fetch order details when returning from PickOrderActivity
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("RecipeActivity", "onResume triggered: Re-fetching order details");
        fetchLatestOrderId(); // ✅ Ensure new order is fetched when returning
    }

    /**
     * 🔹 Load the User ID from Room Database
     */
    private void loadUserIdFromRoom() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<UserEntity> users = userDao.getAllUsers();
            if (!users.isEmpty()) {
                userId = users.get(0).getId();
                Log.d("RecipeActivity", "User ID retrieved from Room: " + userId);
                runOnUiThread(this::fetchLatestOrderId);
            } else {
                runOnUiThread(() -> showErrorMessage("No user found in local database!"));
            }
        });
    }

    /**
     * 🔹 Fetch the latest order ID from Firestore based on User ID
     */
    private void fetchLatestOrderId() {
        if (userId == null || userId.isEmpty()) {
            showErrorMessage("User ID not found!");
            return;
        }

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("orders")) {
                        List<String> orderIds = (List<String>) documentSnapshot.get("orders");
                        if (orderIds != null && !orderIds.isEmpty()) {
                            String newOrderId = orderIds.get(orderIds.size() - 1);

                            // ✅ Check if order has changed before fetching new details
                            if (!newOrderId.equals(latestOrderId)) {
                                latestOrderId = newOrderId;
                                fetchOrderDetails(latestOrderId);
                            } else {
                                Log.d("RecipeActivity", "Order ID is the same, skipping fetch");
                            }
                        } else {
                            showErrorMessage("No orders found!");
                        }
                    } else {
                        showErrorMessage("No orders found in user document!");
                    }
                })
                .addOnFailureListener(e -> showErrorMessage("Failed to load orders!"));
    }

    /**
     * 🔹 Fetch the full order details, including products.
     */
    private void fetchOrderDetails(String orderId) {
        loadingProgressBar.setVisibility(View.VISIBLE);

        db.collection("orders").document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("RecipeActivity", "Order found: " + documentSnapshot.getData());
                        Object productQuantitiesObj = documentSnapshot.get("productQuantities");
                        if (productQuantitiesObj instanceof Map) {
                            Map<String, Object> productQuantities = (Map<String, Object>) productQuantitiesObj;
                            if (!productQuantities.isEmpty()) {
                                fetchProductNames(new ArrayList<>(productQuantities.keySet()));
                            } else {
                                showErrorMessage("No products found in this order!");
                            }
                        } else {
                            showErrorMessage("Product data format incorrect in Firestore!");
                        }
                    } else {
                        showErrorMessage("Order details not found!");
                    }
                })
                .addOnFailureListener(e -> showErrorMessage("Failed to load order details: " + e.getMessage()));
    }

    /**
     * 🔹 Fetch product names from Firestore using product IDs.
     */
    private void fetchProductNames(List<String> productIds) {
        List<String> productNames = new ArrayList<>();
        final int[] processedProducts = {0};

        for (String productId : productIds) {
            db.collection("products").document(productId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        processedProducts[0]++;
                        if (documentSnapshot.exists()) {
                            String productName = documentSnapshot.getString("name");
                            if (productName != null) {
                                productNames.add(productName);
                            }
                        }
                    })
                    .addOnFailureListener(e -> processedProducts[0]++)
                    .addOnCompleteListener(task -> {
                        if (processedProducts[0] == productIds.size()) {
                            if (!productNames.isEmpty()) {
                                generateRecipe(productNames);
                            } else {
                                showErrorMessage("No valid product names found!");
                            }
                        }
                    });
        }
    }

    /**
     * 🔹 Call Hugging Face AI model to generate a recipe based on the product list.
     */
    private void generateRecipe(List<String> productList) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        JsonObject json = new JsonObject();
        json.addProperty("inputs", "Generate a detailed Middle Eastern recipe using these ingredients: " + String.join(", ", productList));

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(HF_API_URL)
                .addHeader("Authorization", "Bearer " + HF_API_KEY)
                .post(body)
                .build();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful() || response.body() == null) {
                    showErrorMessage("Failed to generate recipe! HTTP Code: " + response.code());
                    return;
                }

                String responseData = response.body().string();
                JsonArray jsonArray = JsonParser.parseString(responseData).getAsJsonArray();
                String generatedRecipe = jsonArray.get(0).getAsJsonObject().get("generated_text").getAsString();

                runOnUiThread(() -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    recipeTitle.setVisibility(View.VISIBLE);
                    recipeTextView.setVisibility(View.VISIBLE);
                    recipeTextView.setText(generatedRecipe);
                    generateRecipeButton.setEnabled(true);
                });

            } catch (IOException e) {
                showErrorMessage("Network error: " + e.getMessage());
            }
        });
    }
    /**
     * 🔹 Show an error message using a Toast
     */
    private void showErrorMessage(String message) {
        runOnUiThread(() -> {
            loadingProgressBar.setVisibility(View.GONE);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

}
