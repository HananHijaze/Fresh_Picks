package com.example.fresh_picks;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserDao;
import com.example.fresh_picks.DAO.UserEntity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.concurrent.Executors;

public class Profile extends Fragment {

    private TextView tvUserName, tvUserPhone;
    private Button btnMyLocation, btnCoupons, btnCards;
    private ImageView logoutIcon;
    private static final String TAG = "ProfileFragment";
    private FirebaseFirestore db;
    private UserDao userDao;
    private List<String> userAddresses;
    private List<String> userCoupons;
    private List<String> userCards;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserPhone = view.findViewById(R.id.tv_user_phone);
        btnMyLocation = view.findViewById(R.id.btn_my_location);
        btnCoupons = view.findViewById(R.id.btn_coupons);
        btnCards = view.findViewById(R.id.btn_cards);
        logoutIcon = view.findViewById(R.id.logout);

        // Initialize Firebase Firestore and Room Database
        db = FirebaseFirestore.getInstance();
        userDao = AppDatabase.getInstance(requireContext()).userDao();

        // Load user data
        loadUserFromRoom();

        // Set click listeners to open dialogs
        btnMyLocation.setOnClickListener(v -> showAddressDialog());
        btnCoupons.setOnClickListener(v -> showCouponsDialog());
        btnCards.setOnClickListener(v -> showCardsDialog());

        // Set logout click listener
        logoutIcon.setOnClickListener(v -> logoutUser());

        return view;
    }

    private void loadUserFromRoom() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<UserEntity> users = userDao.getAllUsers();
            if (!users.isEmpty()) {
                UserEntity user = users.get(0); // Ensure only one user exists in Room DB
                String userEmail = user.getEmail(); // Use email to fetch Firestore user

                requireActivity().runOnUiThread(() -> loadUserDataFromFirestore(userEmail));
            } else {
                Log.w(TAG, "No user found in Room Database.");
            }
        });
    }

    private void loadUserDataFromFirestore(String email) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);

                        String name = document.getString("fullName");
                        String phone = document.getString("phoneNumber");
                        userAddresses = (List<String>) document.get("addresses"); // ✅ Store for dialog
                        userCoupons = (List<String>) document.get("coupons"); // ✅ Store for dialog
                        userCards = (List<String>) document.get("cards"); // ✅ Store for dialog

                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                tvUserName.setText(name != null ? name : "N/A");
                                tvUserPhone.setText(phone != null ? phone : "N/A");
                                btnMyLocation.setText(userAddresses != null && !userAddresses.isEmpty() ? "View Addresses" : "No addresses available");
                                btnCoupons.setText(userCoupons != null && !userCoupons.isEmpty() ? "View Coupons" : "No coupons available");
                                btnCards.setText(userCards != null && !userCards.isEmpty() ? "View Cards" : "No cards available");
                            });
                        }
                    } else {
                        Log.w(TAG, "Error fetching user data: ", task.getException());
                    }
                });
    }

    private void showAddressDialog() {
        if (userAddresses == null || userAddresses.isEmpty()) {
            Toast.makeText(requireContext(), "No addresses available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] addressArray = userAddresses.toArray(new String[0]);

        new AlertDialog.Builder(requireContext())
                .setTitle("Your Addresses")
                .setItems(addressArray, (dialog, which) -> {
                    // Handle item click if needed
                })
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showCouponsDialog() {
        if (userCoupons == null || userCoupons.isEmpty()) {
            Toast.makeText(requireContext(), "No coupons available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] couponArray = userCoupons.toArray(new String[0]);

        new AlertDialog.Builder(requireContext())
                .setTitle("Your Coupons")
                .setItems(couponArray, (dialog, which) -> {
                    // Handle item click if needed
                })
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showCardsDialog() {
        if (userCards == null || userCards.isEmpty()) {
            Toast.makeText(requireContext(), "No cards available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] cardsArray = userCards.toArray(new String[0]);

        new AlertDialog.Builder(requireContext())
                .setTitle("Your Cards")
                .setItems(cardsArray, (dialog, which) -> {
                    // Handle item click if needed
                })
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void logoutUser() {
        Executors.newSingleThreadExecutor().execute(() -> {
            userDao.deleteAllUsers(); // Remove user from Room DB

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(requireContext(), LogIn.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });
            }
        });
    }
}
