package com.example.fresh_picks;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.core.view.WindowCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.Log;

import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private UserDao userDao; // Declare userDao
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        // Initialize database manager and start listening for super sales
        databaseManager = new DatabaseManager();
        databaseManager.startSuperSaleListener(); // ✅ Start listening for super deals

        // Adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Room database
        AppDatabase db = AppDatabase.getInstance(this);
        userDao = db.userDao();

        // Run database check asynchronously
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            boolean hasUsers = !userDao.getAllUsers().isEmpty(); // Fetch users from Room
            Log.d(TAG, "Has users in DB: " + hasUsers);

            // Switch to UI thread with a 3-second delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent;
                if (!hasUsers) {
                    intent = new Intent(MainActivity.this, LogIn.class);
                } else {
                    intent = new Intent(MainActivity.this, MainActivity2.class);
                }
                startActivity(intent);
                finish();
            }, 3000); // 3-second delay
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseManager != null) {
            databaseManager.stopListeners(); // ✅ Stop listener when activity is destroyed
        }
    }
}
