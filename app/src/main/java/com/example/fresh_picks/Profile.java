package com.example.fresh_picks;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserEntity;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile extends Fragment {

    private TextView tvUserName, tvUserPhone, tvUserAddresses;
    private static final String TAG = "ProfileFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserPhone = view.findViewById(R.id.tv_user_phone);
        tvUserAddresses = view.findViewById(R.id.btn_my_location); // Update ID based on your XML

        // Load user data
        loadUserIdFromRoom();

        return view;
    }

    private void loadUserIdFromRoom() {
        new Thread(() -> {
            // Access the Room database
            AppDatabase db = AppDatabase.getInstance(requireContext());
            UserEntity user = db.userDao().getAllUsers().get(0); // Assuming a single user in Room

            if (user != null) {
                String userId = user.getId(); // Retrieve the Firestore document ID from Room
                loadUserDataFromFirestore(userId);
            }
        }).start();
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

                        // Update the UI
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
}
