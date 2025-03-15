package com.example.fresh_picks;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserDao;
import com.example.fresh_picks.DAO.UserEntity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class Checkout extends AppCompatActivity {

    private Spinner locationSpinner;
    private Button btnNext;
    private TextView tvTotalPrice, tvStoreLocation, tvStoreLocationLink;
    private FirebaseFirestore db;
    private UserDao userDao;
    private List<String> userAddresses;
    private String selectedLocation = "";
    private double totalPrice = 0.0; // Placeholder for total price
    private LinearLayout pickupOption, courierOption, storeLocationLayout, cardOption, cashOption;
    private String selectedShippingMethod = "Shop Pickup"; // Default selection
    private CardView locationSelectionLayout, cardViewCardDetails;
    private String selectedPaymentMethod = "Credit Card"; // Default selection

    private final String STORE_LOCATION = "אל שריף, Tamra, North District";
    private final String STORE_MAP_LINK = "https://maps.app.goo.gl/8nAKwTJDPuoiSd9A9";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Initialize UI components
        locationSpinner = findViewById(R.id.location_spinner);
        btnNext = findViewById(R.id.btn_next);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        pickupOption = findViewById(R.id.pickup_option);
        courierOption = findViewById(R.id.courier_option);
        tvStoreLocation = findViewById(R.id.tv_store_location);
        tvStoreLocationLink = findViewById(R.id.tv_store_location_link);
        storeLocationLayout = findViewById(R.id.store_location_layout);
        locationSelectionLayout = findViewById(R.id.location_selection_layout); // CardView
        LinearLayout locationSelectionInnerLayout = findViewById(R.id.location_selection_layout1); // LinearLayout)
        db = FirebaseFirestore.getInstance();
        userDao = AppDatabase.getInstance(this).userDao();
        userAddresses = new ArrayList<>();
        cardViewCardDetails = findViewById(R.id.cardView_carddata); // Credit Card Details Layout
        cardOption = findViewById(R.id.card_option); // Credit Card Payment Option
        cashOption = findViewById(R.id.cash_option); // Cash Payment Option

        // Hide address selection layout by default
        locationSelectionLayout.setVisibility(View.GONE);
        locationSelectionInnerLayout.setVisibility(View.GONE);

        // Setup click listeners for shipping method selection
        pickupOption.setOnClickListener(v -> selectShippingMethod("Shop Pickup"));
        courierOption.setOnClickListener(v -> selectShippingMethod("Courier"));
        // Setup click listeners for payment method selection
        cardOption.setOnClickListener(v -> selectPaymentMethod("Credit Card"));
        cashOption.setOnClickListener(v -> selectPaymentMethod("Cash"));
        // Retrieve total price from Intent (passed from previous activity)
        // ✅ Retrieve total price from Intent (passed from Cart)
        if (getIntent().hasExtra("totalPrice")) {
            totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);
        }

        Log.d("Checkout", "Received Total Price: " + totalPrice); // Debugging Log

        // ✅ If total price is still 0, fetch from Firebase
        if (totalPrice == 0.0) {
            fetchCartItemsAndCalculateTotal(userId);
        } else {
            updateTotalPriceText(totalPrice);
        }
        // Load user addresses
        loadUserAddresses();

        // Handle location selection
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLocation = userAddresses.get(position);

                if (selectedLocation.equals("Add New Address...")) {
                    showAddAddressDialog();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Proceed to next step
        btnNext.setOnClickListener(v -> proceedToCheckout());
    }

    private void loadUserAddresses() {
        getCurrentUserId(userId -> {
            if (userId == null) return;
            db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && documentSnapshot.contains("addresses")) {
                    userAddresses = (List<String>) documentSnapshot.get("addresses");
                } else {
                    userAddresses = new ArrayList<>();
                }

                // ✅ Prevent duplicate "Add New Address..." entries
                if (!userAddresses.contains("Add New Address...")) {
                    userAddresses.add("Add New Address...");
                }

                setupLocationSpinner();
            }).addOnFailureListener(e -> Log.e("Checkout", "Error loading addresses", e));
        });
    }


    // Setup location spinner (for courier option)
    private void setupLocationSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, userAddresses);
        locationSpinner.setAdapter(adapter);
    }

    // Proceed to Checkout2 (Final Step)
    private void proceedToCheckout() {
        // ✅ Check if total price is valid
        if (totalPrice <= 0.0) {
            Toast.makeText(this, "Cart is empty! Please add items before checkout.", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Validate selected location
        if (selectedShippingMethod.equals("Courier") && (selectedLocation == null || selectedLocation.isEmpty())) {
            Toast.makeText(this, "Select a shipping location!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Validate Credit Card details if payment method is "Credit Card"
        if (selectedPaymentMethod.equals("Credit Card") && !isCreditCardValid()) {
            Toast.makeText(this, "Please enter valid credit card details!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ All validations passed → Proceed to Checkout2
        Intent intent = new Intent(this, Checkout2.class);
        intent.putExtra("selectedLocation", selectedLocation);
        intent.putExtra("shippingMethod", selectedShippingMethod);
        intent.putExtra("paymentMethod", selectedPaymentMethod);
        intent.putExtra("totalPrice", totalPrice);
        startActivity(intent);
    }


    // Retrieve the current user ID from Room Database
    private void getCurrentUserId(Callback<String> callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<UserEntity> users = userDao.getAllUsers();
            final String userId = (!users.isEmpty()) ? users.get(0).getId() : null;
            runOnUiThread(() -> callback.onResult(userId));
        });
    }

    // Show store location when "Shop Pickup" is selected
    private void showStoreLocation() {
        storeLocationLayout.setVisibility(View.VISIBLE);
        tvStoreLocation.setText("Store Location:\n" + STORE_LOCATION);
        tvStoreLocationLink.setText("Open in Google Maps");
        tvStoreLocationLink.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(STORE_MAP_LINK));
            startActivity(intent);
        });
    }

    // Show dialog to add a new address
    private void showAddAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Address");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String newAddress = input.getText().toString().trim();
            if (!newAddress.isEmpty()) {
                userAddresses.add(0, newAddress);
                setupLocationSpinner();
                saveAddressToFirebase(newAddress);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Save new address to Firestore
    private void saveAddressToFirebase(String newAddress) {
        getCurrentUserId(userId -> {
            if (userId == null) return;
            db.collection("users").document(userId).update("addresses", userAddresses)
                    .addOnSuccessListener(aVoid -> Log.d("Checkout", "Address added to Firestore"))
                    .addOnFailureListener(e -> Log.e("Checkout", "Failed to add address", e));
        });
    }

    public interface Callback<T> {
        void onResult(T result);
    }

    private void selectShippingMethod(String method) {
        selectedShippingMethod = method;

        if (method.equals("Shop Pickup")) {
            pickupOption.setBackgroundResource(R.drawable.selected_background);
            courierOption.setBackgroundResource(R.drawable.unselected_background);
            storeLocationLayout.setVisibility(View.VISIBLE);
            locationSelectionLayout.setVisibility(View.GONE);
            showStoreLocation();

            // ✅ Automatically set the store location when Shop Pickup is selected
            selectedLocation = STORE_LOCATION;
            Log.d("Checkout", "Shop Pickup selected, location set to: " + selectedLocation);

        } else {
            courierOption.setBackgroundResource(R.drawable.selected_background);
            pickupOption.setBackgroundResource(R.drawable.unselected_background);
            storeLocationLayout.setVisibility(View.GONE);
            locationSelectionLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.location_selection_layout1).setVisibility(View.VISIBLE);

            // ✅ Reset selected location to force the user to pick one
            selectedLocation = "";
            loadUserAddresses();
        }
    }

    private void selectPaymentMethod(String method) {
        selectedPaymentMethod = method;

        if (method.equals("Credit Card")) {
            cardOption.setBackgroundResource(R.drawable.selected_background);
            cashOption.setBackgroundResource(R.drawable.unselected_background);
            cardViewCardDetails.setVisibility(View.VISIBLE); // Show Credit Card Details
        } else {
            cashOption.setBackgroundResource(R.drawable.selected_background);
            cardOption.setBackgroundResource(R.drawable.unselected_background);
            cardViewCardDetails.setVisibility(View.GONE); // Hide Credit Card Details
        }

    }

    // ✅ Method to update the total price text
    private void updateTotalPriceText(double totalPrice) {
        runOnUiThread(() -> {
            String formattedPrice = String.format("Total Price: ILS %.2f", totalPrice);
            tvTotalPrice.setText(formattedPrice);
            Log.d("Checkout", "Updated UI Total Price: " + formattedPrice); // Debugging Log
        });
    }


    private void fetchCartItemsAndCalculateTotal(String userId) {
        if (userId == null) {
            Log.e("Checkout", "User ID is null. Cannot fetch cart items.");
            return;
        }

        db.collection("users").document(userId).collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double total = 0.0;
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.exists()) {
                            Double price = document.getDouble("price");
                            Long quantity = document.getLong("quantity");

                            if (price != null && quantity != null) {
                                total += price * quantity;
                            }
                        }
                    }

                    Log.d("Checkout", "Fetched Total Price from Firebase: " + total);
                    updateTotalPriceText(total);
                })
                .addOnFailureListener(e -> Log.e("Checkout", "Error fetching cart items", e));
    }

    private void getCurrentUserIdAsync(Callback<String> callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<UserEntity> users = userDao.getAllUsers(); // ✅ Runs in the background
            String userId = (!users.isEmpty()) ? users.get(0).getId() : null;
            runOnUiThread(() -> callback.onResult(userId)); // ✅ Pass result to the main thread
        });
    }

    private boolean isCreditCardValid() {
        EditText etCardholderName = findViewById(R.id.et_cardholder_name);
        EditText etCardNumber = findViewById(R.id.et_card_number);
        EditText etExpiryDate = findViewById(R.id.et_expiry_date);
        EditText etCvv = findViewById(R.id.et_cvv);

        String cardholderName = etCardholderName.getText().toString().trim();
        String cardNumber = etCardNumber.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();
        String cvv = etCvv.getText().toString().trim();

        // ✅ Validate Cardholder Name
        if (cardholderName.isEmpty()) {
            etCardholderName.setError("Enter Cardholder Name");
            return false;
        }

        // ✅ Validate Card Number (16 digits)
        if (!cardNumber.matches("\\d{16}")) {
            etCardNumber.setError("Enter a valid 16-digit card number");
            return false;
        }

        // ✅ Validate Expiry Date (Format MM/YY and not expired)
        if (!expiryDate.matches("\\d{2}/\\d{2}") || !isExpiryDateValid(expiryDate)) {
            etExpiryDate.setError("Enter a valid expiry date (MM/YY)");
            return false;
        }

        // ✅ Validate CVV (3 digits)
        if (!cvv.matches("\\d{3}")) {
            etCvv.setError("Enter a valid 3-digit CVV");
            return false;
        }

        return true; // ✅ All checks passed
    }
    private boolean isExpiryDateValid(String expiryDate) {
        try {
            String[] parts = expiryDate.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt("20" + parts[1]); // Convert YY to YYYY

            if (month < 1 || month > 12) {
                return false; // Invalid month
            }

            java.util.Calendar calendar = java.util.Calendar.getInstance();
            int currentYear = calendar.get(java.util.Calendar.YEAR);
            int currentMonth = calendar.get(java.util.Calendar.MONTH) + 1; // January = 0

            if (year < currentYear || (year == currentYear && month < currentMonth)) {
                return false; // Card expired
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
