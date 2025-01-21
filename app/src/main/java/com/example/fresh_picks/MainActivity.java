package com.example.fresh_picks;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.core.view.WindowCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fresh_picks.DAO.AppDatabase;
import com.example.fresh_picks.DAO.UserDao;
import com.example.fresh_picks.DAO.UserEntity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        // Adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Update the user in Room database asynchronously
      //  addProductToFirebase();


        // Wait 3 seconds and then start the next activity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            startActivity(intent);
            finish(); // Optional: Finish the current activity
        }, 3000); // 3 seconds delay
    }

//    public static void addProductToFirebase() {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        // Prepare product data based on the provided line
//        String id = "0001";
//        String name = "Tomato";
//        String nameAr = "طمام";
//        String category = "vegetables";
//        double price = 6.9;
//        String packSize = "1 kg";
//        int stockQuantity = 100;
//        String unit = "Kg";
//        List<String> dietaryInfo = Arrays.asList("Vegan", "Gluten-Free", "Low-Calorie");
//        List<String> cuisineTags = Arrays.asList("Mediterranean", "Italian", "Middle Eastern", "Israeli Cuisine", "Levantine Cuisine");
//        boolean inStock = true;
//        boolean seasonal = false;
//        List<String> foodPairings = Arrays.asList("Cheese", "Basil", "Olive Oil", "eggs", "Cucumber", "Garlic");
//        List<String> recipeSuggestions = Arrays.asList("Tomato Soup", "Shaksuka", "Israeli Salad", "Stuffed Tomatoes", "Tabbouleh");
//        String imageUrl = "https://drive.google.com/file/d/1HCrc6e9gO8ZZ1v0Lg8zuYHDf7qUeS6XW/view?usp=sharing";
//        int popularityScore = 6;
//
//        // Create a map to represent the Firestore document
//        Map<String, Object> productData = new HashMap<>();
//        productData.put("id", id);
//        productData.put("name", name);
//        productData.put("nameAr", nameAr);
//        productData.put("category", category);
//        productData.put("price", price);
//        productData.put("packSize", packSize);
//        productData.put("stockQuantity", stockQuantity);
//        productData.put("unit", unit);
//        productData.put("dietaryInfo", dietaryInfo);
//        productData.put("cuisineTags", cuisineTags);
//        productData.put("inStock", inStock);
//        productData.put("seasonal", seasonal);
//        productData.put("foodPairings", foodPairings);
//        productData.put("recipeSuggestions", recipeSuggestions);
//        productData.put("imageUrl", imageUrl);
//        productData.put("popularityScore", popularityScore);
//
//        // Add the product to Firestore
//        db.collection("products")
//                .document(id) // Use the product ID as the document ID
//                .set(productData)
//                .addOnSuccessListener(aVoid -> {
//                    System.out.println("Product added successfully: " + name);
//                })
//                .addOnFailureListener(e -> {
//                    System.err.println("Error adding product: " + e.getMessage());
//                });
//    }
}
