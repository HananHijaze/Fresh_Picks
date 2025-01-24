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

    private String title;
    private String category;

    private GridView gridView;
    private ProductAdapter productAdapter;
    private List<Product> products = new ArrayList<>();
    private FirebaseFirestore firestore;

    public ListsView() {
        // Required empty public constructor
    }

    /**
     * Factory method to create a new instance of this fragment.
     *
     * @param title    The title to display in the fragment.
     * @param category The category to fetch products for.
     * @return A new instance of fragment ListsView.
     */
    public static ListsView newInstance(String title, String category) {
        ListsView fragment = new ListsView();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            category = getArguments().getString(ARG_CATEGORY);
            Log.d("ListsView", "Title: " + title + ", Category: " + category);
        }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lists_view, container, false);

        // Set the title in the TextView
        TextView textView = view.findViewById(R.id.textView);
        if (title != null) {
            textView.setText(title);
        }

        // Initialize the GridView and Adapter
        gridView = view.findViewById(R.id.grid_view);
        productAdapter = new ProductAdapter(requireContext(), products);
        gridView.setAdapter(productAdapter);

        // Fetch and display products based on the selected category
        fetchProductsByCategory();

        return view;
    }

    /**
     * Fetches products from Firestore based on the selected category.
     */
    private void fetchProductsByCategory() {
        if (category == null || category.isEmpty()) {
            Toast.makeText(requireContext(), "Category is not valid!", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("products")
                .whereArrayContains("category", category) // Check if the category matches
                .whereEqualTo("inStock", true) // Filter for products that are in stock
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            products.clear(); // Clear the previous list
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Product product = document.toObject(Product.class);
                                products.add(product);
                                Log.d("ListsView", "Product fetched: " + product.toString());
                            }
                            productAdapter.notifyDataSetChanged(); // Notify adapter of data change
                        }
                    } else {
                        Log.e("ListsView", "Error fetching products", task.getException());
                        Toast.makeText(requireContext(), "Error fetching products!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
