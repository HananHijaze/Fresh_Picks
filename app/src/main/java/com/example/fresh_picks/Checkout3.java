package com.example.fresh_picks;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;

public class Checkout3 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkout3);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button orderButton = findViewById(R.id.orderButton);
        LottieAnimationView lottieAnimationView = findViewById(R.id.lottieAnimationView);

        orderButton.setOnClickListener(v -> {
            // Start a simple scale animation for the button
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(200).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(200);

                // Simulate order submission by showing the Lottie animation
                lottieAnimationView.setVisibility(View.VISIBLE);
                lottieAnimationView.playAnimation();

                // Optionally, hide the button after submission
                orderButton.setVisibility(View.GONE);
            });
        });

    }
}