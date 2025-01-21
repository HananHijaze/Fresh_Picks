package com.example.fresh_picks;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.HashMap;
import java.util.Map;

public class Home extends Fragment {

    public Home() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Set up CardView click listeners using a Map
        setupCardViewClickListeners(view);

        return view;
    }

    private void setupCardViewClickListeners(View view) {
        // Map each CardView ID to its title and category
        Map<Integer, String[]> cardViewMap = new HashMap<>();
        cardViewMap.put(R.id.cardView1, new String[]{"Seasonal", "seasonal"});
        cardViewMap.put(R.id.cardView2, new String[]{"Fruits", "fruits"});
        cardViewMap.put(R.id.cardView3, new String[]{"Vegetables", "vegetables"});
        cardViewMap.put(R.id.cardView4, new String[]{"Bread and Pastries", "bread and pastries"});
        cardViewMap.put(R.id.cardView5, new String[]{"Dairy", "dairy"});
        cardViewMap.put(R.id.cardView6, new String[]{"Grocery Essentials", "grocery essentials"});
        cardViewMap.put(R.id.cardView7, new String[]{"Frozen Food", "frozen food"});
        cardViewMap.put(R.id.cardView8, new String[]{"Snacks", "snacks"});
        cardViewMap.put(R.id.cardView9, new String[]{"Drinks", "drinks"});
        cardViewMap.put(R.id.cardView10, new String[]{"Spices", "spices"});
        cardViewMap.put(R.id.cardView11, new String[]{"Support Super 👸", "handmade"});
        cardViewMap.put(R.id.cardView12, new String[]{"Super Deals", "super_deals"});

        // Loop through the Map and set click listeners for each CardView
        for (Map.Entry<Integer, String[]> entry : cardViewMap.entrySet()) {
            int cardViewId = entry.getKey();
            String title = entry.getValue()[0]; // Display title
            String category = entry.getValue()[1]; // Firebase category

            CardView cardView = view.findViewById(cardViewId);
            if (cardView != null) { // Ensure the CardView exists in the layout
                cardView.setOnClickListener(v -> {
                    Log.d("Home", "Navigating to ListsView with title: " + title + " and category: " + category);
                    navigateToListsView(title, category);
                });
            }
        }
    }

    private void navigateToListsView(String title, String category) {
        // Create a new instance of ListsView with the selected title and category
        ListsView listsViewFragment = ListsView.newInstance(title, category);

        // Navigate to the ListsView fragment
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, listsViewFragment)
                .addToBackStack(null)
                .commit();
    }

}
