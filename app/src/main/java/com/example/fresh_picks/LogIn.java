package com.example.fresh_picks;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserDao;
import com.example.fresh_picks.DAO.UserEntity;
import com.example.fresh_picks.MainActivity;
import com.example.fresh_picks.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.concurrent.Executors;

public class LogIn extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private FirebaseFirestore db;
    private UserDao userDao;
    private TextView textView4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        db = FirebaseFirestore.getInstance();
        AppDatabase database = AppDatabase.getInstance(this);
        userDao = database.userDao();

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        textView4 = findViewById(R.id.textView4);
        btnLogin.setOnClickListener(v -> loginUser());
        textView4.setOnClickListener(v -> signUp(v));
    }

    private void loginUser() {
        String email = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").whereEqualTo("email", email).get().addOnSuccessListener(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                String storedPassword = document.getString("password");

                if (storedPassword != null && storedPassword.equals(password)) {
                    String userId = document.getId();
                    String fullName = document.getString("fullName");
                    String phoneNumber = document.getString("phoneNumber");
                    String address = document.getString("address");

                    Executors.newSingleThreadExecutor().execute(() -> {
                        userDao.deleteAllUsers(); // ✅ Delete all users before inserting a new one

                        UserEntity userEntity = new UserEntity(userId, fullName, email, password, phoneNumber, address);
                        userDao.insertUser(userEntity);

                        runOnUiThread(() -> {
                            Toast.makeText(LogIn.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LogIn.this, MainActivity.class));
                            finish();
                        });
                    });
                } else {
                    Toast.makeText(LogIn.this, "Invalid password", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LogIn.this, "User not found", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void signUp(View view) {
        Intent intent = new Intent(this, SignUp.class);
        startActivity(intent);
    }

}
