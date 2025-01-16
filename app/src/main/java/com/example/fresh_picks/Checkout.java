package com.example.fresh_picks;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.core.view.WindowCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Checkout extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable Edge-to-Edge UI
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Set the content view to the checkout layout
        setContentView(R.layout.activity_checkout);

        // Adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Define the btn_next button and set an OnClickListener
        Button btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(view -> {
            // Navigate to Checkout2 activity
            Intent intent = new Intent(Checkout.this, Checkout2.class);
            startActivity(intent);
        });

        // Define the btn_signup button and set an OnClickListener
    }
}
