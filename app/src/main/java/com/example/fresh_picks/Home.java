package com.example.fresh_picks;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.fresh_picks.classes.Product;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Home extends Fragment {
    private FirebaseFirestore db;
    private SearchView searchView;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    public Home() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firestore and search view
        db = FirebaseFirestore.getInstance();
        searchView = view.findViewById(R.id.searchView);
        setupSearchView();

        // Set up CardView click listeners using a Map
        setupCardViewClickListeners(view);

        return view;
    }

    private void setupCardViewClickListeners(View view) {
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

        for (Map.Entry<Integer, String[]> entry : cardViewMap.entrySet()) {
            int cardViewId = entry.getKey();
            String title = entry.getValue()[0];
            String category = entry.getValue()[1];

            CardView cardView = view.findViewById(cardViewId);
            if (cardView != null) {
                cardView.setOnClickListener(v -> navigateToListsView(title, category));
            }
        }
    }

    private void navigateToListsView(String title, String category) {
        ListsView listsViewFragment = ListsView.newInstance(title, category);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, listsViewFragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.trim().isEmpty()) {
                    return false; // Don't search if the query is empty
                }

                // Cancel any previously scheduled searches
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Schedule a new search after 500ms
                searchRunnable = () -> performSearch(newText);
                searchHandler.postDelayed(searchRunnable, 500);

                return true;
            }
        });
    }
    private void performSearch(String query) {
        final String searchQuery = query.trim();
        if (searchQuery.isEmpty()) {
            return; // Skip empty queries
        }

        db.collection("products")
                .orderBy("name")
                .startAt(searchQuery)
                .endAt(searchQuery + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        displaySearchResults(queryDocumentSnapshots);
                    } else {
                        db.collection("products")
                                .orderBy("name_ar")
                                .startAt(searchQuery)
                                .endAt(searchQuery + "\uf8ff")
                                .get()
                                .addOnSuccessListener(this::displaySearchResults) // ✅ Fixed reference
                                .addOnFailureListener(e -> Log.e("Search", "Error searching Arabic: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> Log.e("Search", "Error searching English: " + e.getMessage()));
    }

    private void displaySearchResults(QuerySnapshot queryDocumentSnapshots) {
        List<Product> searchResults = new ArrayList<>();

        for (DocumentSnapshot document : queryDocumentSnapshots) {
            Product product = document.toObject(Product.class);
            if (product != null) {
                searchResults.add(product);
            }
        }

        // 🔹 Navigate to ListsView with search results
        ListsView listsViewFragment = ListsView.newInstance("Search Results", searchResults); // ✅ Ensure ListsView supports this method

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, listsViewFragment)
                .addToBackStack(null)
                .commit();
    }
}
