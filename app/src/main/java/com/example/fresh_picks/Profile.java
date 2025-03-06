package com.example.fresh_picks;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.concurrent.Executors;

public class Profile extends Fragment {

    private TextView tvUserName, tvUserPhone, tvUserAddresses;
    private ImageView logoutIcon;
    private static final String TAG = "ProfileFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserPhone = view.findViewById(R.id.tv_user_phone);
        tvUserAddresses = view.findViewById(R.id.btn_my_location);
        logoutIcon = view.findViewById(R.id.logout); // 🔹 Find the logout icon

        // Load user data
        loadUserIdFromRoom();

        // 🔹 Set logout click listener
        logoutIcon.setOnClickListener(v -> logoutUser());

        return view;
    }

    private void loadUserIdFromRoom() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Access the Room database
            AppDatabase db = AppDatabase.getInstance(requireContext());
            UserDao userDao = db.userDao();
            UserEntity user = userDao.getAllUsers().isEmpty() ? null : userDao.getAllUsers().get(0);

            if (user != null) {
                String userId = user.getId(); // Retrieve the Firestore document ID from Room
                loadUserDataFromFirestore(userId);
            }
        });
    }

    private void loadUserDataFromFirestore(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Retrieve the user data from Firestore
                        String name = task.getResult().getString("name");
                        String phone = task.getResult().getString("phoneNumber");
                        String addresses = task.getResult().get("addresses").toString();

                        // Update the UI on the main thread
                        requireActivity().runOnUiThread(() -> {
                            tvUserName.setText(name);
                            tvUserPhone.setText(phone);
                            tvUserAddresses.setText(addresses);
                        });
                    } else {
                        Log.w(TAG, "Error getting user data", task.getException());
                    }
                });
    }

    private void logoutUser() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // 🔹 Access Room database and delete user data
            AppDatabase db = AppDatabase.getInstance(requireContext());
            UserDao userDao = db.userDao();
            userDao.deleteAllUsers(); // Delete all users from Room

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show();

                // 🔹 Navigate to Login Screen (Optional)
                Intent intent = new Intent(requireContext(), LogIn.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears activity stack
                startActivity(intent);
            });
        });
    }
}
