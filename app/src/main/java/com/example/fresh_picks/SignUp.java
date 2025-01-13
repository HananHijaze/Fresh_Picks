package com.example.fresh_picks;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fresh_picks.APIS.RetrofitClient;
import com.example.fresh_picks.APIS.UserApiService;
import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserEntity;
import com.example.fresh_picks.classes.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUp extends AppCompatActivity {
    private TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword, etPhoneNumber, etAddress;
    private MaterialButton btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
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
        User user = new User(fullName, email, password, Arrays.asList(address), phoneNumber);
        UserApiService userApiService = RetrofitClient.getRetrofitInstance().create(UserApiService.class);

        Call<User> call = userApiService.registerUser(user);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    UserEntity userEntity = new UserEntity(
                            response.body().getId(),
                            response.body().getName(),
                            response.body().getEmail(),
                            response.body().getPassword(),
                            response.body().getPhoneNumber()
                    );

                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getInstance(SignUp.this);
                        db.userDao().insertUser(userEntity);
                        runOnUiThread(() -> Toast.makeText(SignUp.this, "User saved locally!", Toast.LENGTH_SHORT).show());

                        // Navigate to Checkout activity
                        Intent intent = new Intent(SignUp.this, Checkout.class);
                        startActivity(intent);
                        finish(); // Finish the SignUp activity
                    }).start();

                    Toast.makeText(SignUp.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignUp.this, "Registration failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(SignUp.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
