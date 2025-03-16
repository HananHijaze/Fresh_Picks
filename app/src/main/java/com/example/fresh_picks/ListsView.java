package com.example.fresh_picks;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fresh_picks.classes.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListsView extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_PRODUCTS = "products";  // 🔹 Added to accept search results

    private String title;
    private String category;
    private List<Product> products = new ArrayList<>();

    private GridView gridView;
    private ProductAdapter productAdapter;
    private FirebaseFirestore firestore;
    private TextView no_results_text;
    public ListsView() {
        // Required empty public constructor
    }

    /**
     * 🔹 Method to create `ListsView` with a category filter.
     */
    public static ListsView newInstance(String title, String category) {
        ListsView fragment = new ListsView();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 🔹 Method to create `ListsView` with search results.
     */
    public static ListsView newInstance(String title, List<Product> products) {
        ListsView fragment = new ListsView();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putParcelableArrayList(ARG_PRODUCTS, new ArrayList<>(products));  // Convert list to ArrayList
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            category = getArguments().getString(ARG_CATEGORY);
            products = getArguments().getParcelableArrayList(ARG_PRODUCTS);

            if (products == null) {
                products = new ArrayList<>();  // 🔹 Ensure products is never null
            }

            Log.d("ListsView", "Title: " + title + ", Category: " + category + ", Products: " + products.size());
        } else {
            products = new ArrayList<>();  // 🔹 Ensure products is never null
        }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lists_view, container, false);

        // Set the title
        TextView textView = view.findViewById(R.id.textView);
        if (title != null) {
            textView.setText(title);
        }

        // Initialize GridView
        gridView = view.findViewById(R.id.grid_view);
        no_results_text=view.findViewById(R.id.no_results_text);
        // 🔹 Ensure `products` is not null before passing to the adapter
        // If products list is empty, show "No products found"
        updateUI();


        // Fetch products if needed
        if (category != null) {
            fetchProductsByCategory();
        } else {
            productAdapter.notifyDataSetChanged();  // Update UI if search results are used
        }

        return view;
    }


    /**
     * 🔹 Fetches products from Firestore based on the selected category.
     */
    private void fetchProductsByCategory() {
        if (category == null || category.isEmpty()) {
            Toast.makeText(requireContext(), "Category is not valid!", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("products")
                .whereArrayContains("category", category) // Check if the category matches
                .whereEqualTo("inStock", true) // Filter for products in stock
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            products.clear();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Product product = document.toObject(Product.class);
                                products.add(product);
                            }
                        }
                        updateUI(); // 🔹 Update UI after fetching data

                    } else {
                        Log.e("ListsView", "Error fetching products", task.getException());
                        Toast.makeText(requireContext(), "Error fetching products!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public static ListsView newInstanceWithProducts(String title, List<Product> products) {
        ListsView fragment = new ListsView();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putParcelableArrayList("products", new ArrayList<>(products)); // Ensure correct type
        fragment.setArguments(args);
        return fragment;
    }
    /**
     * 🔹 Updates UI based on products availability.
     */
    private void updateUI() {
        if (products == null || products.isEmpty()) {
            no_results_text.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.GONE);
        } else {
            no_results_text.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);
            productAdapter = new ProductAdapter(requireContext(), products);
            gridView.setAdapter(productAdapter);
        }
    }

}
