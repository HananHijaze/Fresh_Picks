package com.example.fresh_picks;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserEntity;

import java.util.List;

public class Cart extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public Cart() {
        // Required empty public constructor
    }

    public static Cart newInstance(String param1, String param2) {
        Cart fragment = new Cart();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        // Define the buttonCheckout
        Button buttonCheckout = view.findViewById(R.id.buttonCheckout);

        // Set a click listener to check user registration status
        buttonCheckout.setOnClickListener(v -> {
            if (isUserRegistered()) {
                // User is registered, proceed to CheckoutActivity
                Intent intent = new Intent(getActivity(), Checkout.class);
                startActivity(intent);
            } else {
                // User is not registered, redirect to SignUp activity
                Toast.makeText(getActivity(), "Please sign up before checking out.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), SignUp.class);
                startActivity(intent);
            }
        });

        return view;
    }

    // Method to check if the user is registered
    private boolean isUserRegistered() {
        final boolean[] isRegistered = {false}; // Use an array to allow modification inside the thread

        Thread thread = new Thread(() -> {
            // Access the Room database
            AppDatabase db = AppDatabase.getInstance(getActivity());
            // Check if there is any user in the database
            List<UserEntity> users = db.userDao().getAllUsers(); // Fetch all users
            isRegistered[0] = users != null && !users.isEmpty(); // Set true if there's at least one user
        });

        thread.start();

        try {
            thread.join(); // Wait for the thread to finish execution
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return isRegistered[0];
    }
}
