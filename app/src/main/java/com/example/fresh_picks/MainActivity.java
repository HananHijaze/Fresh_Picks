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
import com.mongodb.client.MongoDatabase;

public class MainActivity extends AppCompatActivity {
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

        // Connect to MongoDB
        connectToMongoDB();

        // Wait 3 seconds and then start the next activity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            startActivity(intent);
            finish(); // Optional: Finish the current activity
        }, 3000); // 3 seconds delay
    }

    private void connectToMongoDB() {
        // Initialize MongoDB connection using MongoDBHelper
        MongoDBHelper.init();

        try {
            // Access the database directly
            MongoDatabase database = MongoDBHelper.getDatabase();
            Log.i("MongoDB", "Connected to MongoDB successfully. Database: " + database.getName());
        } catch (Exception e) {
            Log.e("MongoDB", "Error connecting to MongoDB", e);
        }
    }
}
