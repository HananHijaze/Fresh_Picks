package com.example.fresh_picks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserEntity;

public class Profile extends Fragment {

    private TextView tvUserName, tvUserPhone, tvUserAddresses;
    private ImageView imgUserIcon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        imgUserIcon = view.findViewById(R.id.img_user_icon);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserPhone = view.findViewById(R.id.tv_user_phone);
        tvUserAddresses = view.findViewById(R.id.tv_user_addresses);

        // Load user data
        loadUserData("0532811583"); // Replace with the actual phone number, if dynamically retrieved

        return view;
    }

    private void loadUserData(String phoneNumber) {
        new Thread(() -> {
            // Access the Room database
            AppDatabase db = AppDatabase.getInstance(requireContext());
            UserEntity user = db.userDao().getUserByPhoneNumber(phoneNumber);

            // Update the UI with the user data
            if (user != null) {
                requireActivity().runOnUiThread(() -> {
                    tvUserName.setText(user.getName());
                    tvUserPhone.setText(phoneNumber);
                    tvUserAddresses.setText(String.join(", ", user.getAddresses()));
                });
            }
        }).start();
    }
}
