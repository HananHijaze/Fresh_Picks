package com.example.fresh_picks;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.view.WindowCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fresh_picks.APIS.RetrofitClient;
import com.example.fresh_picks.APIS.UserApiService;
import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserDao;
import com.example.fresh_picks.DAO.UserEntity;
import com.example.fresh_picks.classes.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUp extends AppCompatActivity {
    private TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword, etPhoneNumber, etAddress;
    private MaterialButton btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        etAddress = findViewById(R.id.et_adress);
        btnSignUp = findViewById(R.id.btn_sign_up);

        btnSignUp.setOnClickListener(v -> validateAndSubmit());
    }

    private void validateAndSubmit() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required. Please enter your full name.");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required. Please enter your email address.");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email format. Please enter a valid email address.");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required. Please enter a password.");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters long.");
            etPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password.");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match. Please re-enter.");
            etConfirmPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            etPhoneNumber.setError("Phone number is required. Please enter your phone number.");
            etPhoneNumber.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Address is required. Please enter at least one address.");
            etAddress.requestFocus();
            return;
        }

        saveUserData(fullName, email, password, phoneNumber, address);
    }

    private void saveUserData(String fullName, String email, String password, String phoneNumber, String address) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new User object
        User user = new User(fullName, email, password, Arrays.asList(address), phoneNumber);

        // Create a map to store the user details for Firestore
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", user.getName());
        userMap.put("email", user.getEmail());
        userMap.put("password", user.getPassword()); // Avoid storing passwords as plain text
        userMap.put("addresses", user.getAddresses());
        userMap.put("phoneNumber", user.getPhoneNumber());
        userMap.put("bulkSaleBuyer", user.isBulkSaleBuyer());
        userMap.put("cart", user.getCart());
        userMap.put("orders", user.getOrders());

        // Save user data to Firestore
        db.collection("users")
                .add(userMap)
                .addOnSuccessListener(documentReference -> {
                    String userId = documentReference.getId(); // Get the Firestore-generated ID
                    user.setId(userId.hashCode()); // Optional: Keep track of the Firestore ID in your user object

                    // Save user to Room database
                    new Thread(() -> {
                        AppDatabase dbRoom = AppDatabase.getInstance(SignUp.this);
                        UserDao userDao = dbRoom.userDao();

                        // Delete all existing users
                        userDao.deleteAllUsers();

                        // Insert the new user into Room database
                        UserEntity userEntity = new UserEntity(
                                userId, // Use the Firestore document ID as the Room ID
                                user.getName(),
                                user.getEmail(),
                                user.getPassword(),
                                user.getPhoneNumber()
                        );
                        userDao.insertUser(userEntity);

                        runOnUiThread(() -> {
                            Toast.makeText(SignUp.this, "User saved locally!", Toast.LENGTH_SHORT).show();

                            // Navigate to Checkout activity
                            Intent intent = new Intent(SignUp.this, Checkout.class);
                            startActivity(intent);
                            finish(); // Finish the SignUp activity
                        });
                    }).start();

                    Toast.makeText(SignUp.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignUp.this, "Error registering user: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}
