package com.example.fresh_picks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.app.Dialog;
import android.widget.Button;

import androidx.appcompat.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.fresh_picks.classes.Product;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Home extends Fragment {
    private FirebaseFirestore db;
    private SearchView searchView;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private Dialog noConnectionDialog;
    private boolean isDialogShowing = false;

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

        // Check for internet connection
        if (!isNetworkAvailable()) {
            showNoConnectionDialog();
        }

        setupSearchView();

        // Set up CardView click listeners using a Map
        setupCardViewClickListeners(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check connectivity when fragment resumes
        if (!isNetworkAvailable() && !isDialogShowing) {
            showNoConnectionDialog();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNoConnectionDialog() {
        if (isDialogShowing) return;

        isDialogShowing = true;
        noConnectionDialog = new Dialog(requireContext());
        noConnectionDialog.setContentView(R.layout.noconnection);
        noConnectionDialog.setCancelable(false);

        Button retryButton = noConnectionDialog.findViewById(R.id.retryButton);
        retryButton.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                noConnectionDialog.dismiss();
                isDialogShowing = false;
            } else {
                Toast.makeText(requireContext(), R.string.still_no_connection, Toast.LENGTH_SHORT).show();
            }
        });

        noConnectionDialog.show();
    }

    private void setupCardViewClickListeners(View view) {
        Map<Integer, String[]> cardViewMap = new HashMap<>();
        cardViewMap.put(R.id.cardView1, new String[]{getString(R.string.seasonal_fruits), "seasonal"});
        cardViewMap.put(R.id.cardView2, new String[]{getString(R.string.fruits), "fruits"});
        cardViewMap.put(R.id.cardView3, new String[]{getString(R.string.vegetables), "vegetables"});
        cardViewMap.put(R.id.cardView4, new String[]{getString(R.string.bread_and_pastries), "bread and pastries"});
        cardViewMap.put(R.id.cardView5, new String[]{getString(R.string.dairy), "dairy"});
        cardViewMap.put(R.id.cardView6, new String[]{getString(R.string.grocery_essentials), "grocery essentials"});
        cardViewMap.put(R.id.cardView7, new String[]{getString(R.string.frozen_foods), "frozen food"});
        cardViewMap.put(R.id.cardView8, new String[]{getString(R.string.snacks), "snacks"});
        cardViewMap.put(R.id.cardView9, new String[]{getString(R.string.drinks), "drinks"});
        cardViewMap.put(R.id.cardView10, new String[]{getString(R.string.spices), "spices"});
        cardViewMap.put(R.id.cardView11, new String[]{getString(R.string.support_super), "handmade"});
        cardViewMap.put(R.id.cardView12, new String[]{getString(R.string.min_90_deals), "super_deals"});

        for (Map.Entry<Integer, String[]> entry : cardViewMap.entrySet()) {
            int cardViewId = entry.getKey();
            String title = entry.getValue()[0];
            String category = entry.getValue()[1];

            CardView cardView = view.findViewById(cardViewId);
            if (cardView != null) {
                cardView.setOnClickListener(v -> {
                    // Check for internet connection before navigation
                    if (!isNetworkAvailable()) {
                        showNoConnectionDialog();
                    } else {
                        navigateToListsView(title, category);
                    }
                });
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

    private void navigateToListsView(String title, List<Product> products, boolean isSearch) {
        ListsView listsViewFragment = ListsView.newInstanceWithProducts(title, products, isSearch);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, listsViewFragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupSearchView() {
        searchView.setIconifiedByDefault(false); // 🔹 Keeps the search bar always expanded

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.trim().isEmpty()) {
                    // Check for internet connection before performing search
                    if (!isNetworkAvailable()) {
                        showNoConnectionDialog();
                    } else {
                        performSearch(query.trim());
                    }
                }
                return false; // Keeps keyboard open after search
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false; // Do nothing on text change (ensures search only happens on Enter)
            }
        });

        // 🔹 Ensure clicking anywhere in the search bar works (not just the magnifying glass)
        searchView.setOnClickListener(v -> {
            searchView.setIconified(false);
            searchView.requestFocus();
        });

        // 🔹 Prevent collapse when losing focus
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                searchView.setIconified(false); // Keep it expanded
            }
        });
    }

    private void performSearch(String query) {
        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> filteredResults = new ArrayList<>();
                    String lowerCaseQuery = query.toLowerCase();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        if (product != null) {
                            // 🔹 Retrieve both English and Arabic names
                            String productNameEn = document.getString("name");
                            String productNameAr = document.getString("nameAr");

                            // 🔹 Convert names to lowercase for case-insensitive search
                            boolean matchesEn = productNameEn != null && productNameEn.toLowerCase().contains(lowerCaseQuery);
                            boolean matchesAr = productNameAr != null && productNameAr.toLowerCase().contains(lowerCaseQuery);

                            // 🔹 Add to results if the query matches either language
                            if (matchesEn || matchesAr) {
                                filteredResults.add(product);
                            }
                        }
                    }

                    // Navigate to search results
                    navigateToListsView(getString(R.string.search_results), filteredResults, true);

                    // 🔹 Clear the search bar input
                    searchView.setQuery("", false);
                    searchView.clearFocus(); // Hide keyboard
                })
                .addOnFailureListener(e -> {
                    if (!isNetworkAvailable()) {
                        showNoConnectionDialog();
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.search_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (noConnectionDialog != null && noConnectionDialog.isShowing()) {
            noConnectionDialog.dismiss();
        }
    }
}