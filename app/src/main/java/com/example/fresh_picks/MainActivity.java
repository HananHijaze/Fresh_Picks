package com.example.fresh_picks;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fresh_picks.APIS.MongoDBHelper;
import com.example.fresh_picks.DAO.AppDatabase;
import com.mongodb.client.MongoDatabase;

import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Room database asynchronously
        initializeRoomDatabase();

        // Connect to MongoDB asynchronously
        connectToMongoDB();

        // Wait 3 seconds and then start the next activity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            startActivity(intent);
            finish(); // Optional: Finish the current activity
        }, 3000); // 3 seconds delay
    }

    private void initializeRoomDatabase() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Initialize Room database using AppDatabase
                db = AppDatabase.getInstance(this);
                Log.i("RoomDB", "Room database initialized successfully.");

                // Example: Query some data from the database
                if (db.userDao().getAllUsers().isEmpty()) { // Assuming `getAllUsers()` is a method in your UserDao
                    Log.i("RoomDB", "No users found in the database.");
                } else {
                    Log.i("RoomDB", "Users fetched successfully.");
                }
            } catch (Exception e) {
                Log.e("RoomDB", "Error accessing Room database", e);
            }
        });
    }

    private void connectToMongoDB() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Initialize MongoDB connection using MongoDBHelper
                MongoDBHelper.init();

                // Access the database directly
                MongoDatabase database = MongoDBHelper.getDatabase();
                Log.i("MongoDB", "Connected to MongoDB successfully. Database: " + database.getName());
            } catch (Exception e) {
                Log.e("MongoDB", "Error connecting to MongoDB", e);
            }
        });
    }
}
