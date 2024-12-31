package com.example.fresh_picks;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load the HomeFragment by default
        loadFragment(new Home());

        // Initialize the BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set the onItemSelectedListener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;  // Declare the fragment to be loaded
            int id = item.getItemId();  // Get the ID of the selected menu item

            // Handle item selection based on the selected ID
            if (id == R.id.home_BottomIcon) {
                selectedFragment = new Home();  // Load HomeFragment
            } else if (id == R.id.wishList_BottomIcon) {
                selectedFragment = new WishList();  // Load WishListFragment
            } else if (id == R.id.cart_BottomIcon) {
                selectedFragment = new Cart();
            }

            // If a fragment is selected (not null), replace the existing one
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }

            return true;  // Return true to indicate the item selection is handled
        });
    }

    // Method to replace the fragment in the FrameLayout
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
