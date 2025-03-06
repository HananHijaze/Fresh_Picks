package com.example.fresh_picks;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserDao;
import com.example.fresh_picks.DAO.UserEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class SignUp extends AppCompatActivity {
    private TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword, etPhoneNumber, etAddress;
    private MaterialButton btnSignUp;
    private FirebaseFirestore db;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        db = FirebaseFirestore.getInstance();
        AppDatabase database = AppDatabase.getInstance(this);
        userDao = database.userDao();

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

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) ||
                TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        createUserWithCart(fullName, email, password, phoneNumber, address);
    }

    private void createUserWithCart(String fullName, String email, String password, String phoneNumber, String address) {
        // **Step 1: Create an empty cart in Firestore**
        String cartId = db.collection("carts").document().getId(); // Generate unique cart ID
        Map<String, Object> cartMap = new HashMap<>();
        cartMap.put("cartId", cartId);
        cartMap.put("items", new HashMap<>()); // Empty cart

        db.collection("carts").document(cartId).set(cartMap)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Cart created successfully!");

                    // **Step 2: Prepare user data with cart ID**
                    List<String> addressList = Collections.singletonList(address); // ✅ Store address as a list

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("fullName", fullName);
                    userMap.put("email", email);
                    userMap.put("password", password);
                    userMap.put("phoneNumber", phoneNumber);
                    userMap.put("addresses", addressList); // ✅ Save as a list
                    userMap.put("cartId", cartId); // ✅ Store cart ID
                    userMap.put("orders", Collections.emptyList()); // ✅ Empty orders list
                    userMap.put("bulkSaleBuyer", false); // ✅ Default to false

                    // **Step 3: Save user in Firestore**
                    db.collection("users").add(userMap).addOnSuccessListener(documentReference -> {
                        String userId = documentReference.getId(); // Firestore-generated ID

                        // **Step 4: Delete existing users & insert new user into Room DB**
                        Executors.newSingleThreadExecutor().execute(() -> {
                            userDao.deleteAllUsers();  // Remove all existing users
                            UserEntity newUser = new UserEntity(userId, fullName, email, password, phoneNumber, address);
                            userDao.insertUser(newUser);

                            runOnUiThread(() -> {
                                Toast.makeText(SignUp.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignUp.this, Home.class));
                                finish();
                            });
                        });
                    }).addOnFailureListener(e ->
                            Toast.makeText(SignUp.this, "Error saving user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(SignUp.this, "Error creating cart: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
