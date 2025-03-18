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

        // 🔹 Initialize Firestore first to avoid null reference
        firestore = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            category = getArguments().getString(ARG_CATEGORY);
            products = getArguments().getParcelableArrayList(ARG_PRODUCTS);
            boolean isSearch = getArguments().getBoolean("isSearch", false);

            if (products == null) {
                products = new ArrayList<>();
            }

            // 🔹 Ensure `fetchProductsByCategory()` is only called when needed
            if (!isSearch && category != null) {
                fetchProductsByCategory();
            }
        } else {
            products = new ArrayList<>();
        }
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
        } else if(productAdapter != null){
            productAdapter.notifyDataSetChanged();  // Update UI if search results are used
        }else {
            Log.e("ListsView", "productAdapter is null");
        }

        return view;
    }


    /**
     * 🔹 Fetches products from Firestore based on the selected category.
     */
    private void fetchProductsByCategory() {
        if (firestore == null) {  // 🔹 Check if Firestore is properly initialized
            Log.e("ListsView", "Firestore is not initialized");
            return;
        }

        if (category == null || category.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.category_invalid), Toast.LENGTH_SHORT).show();
            return;
        }
        firestore.collection("products")
                .whereArrayContains("category", category)
                .whereEqualTo("inStock", true)
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
                        updateUI();
                    } else {
                        Log.e("ListsView", "Error fetching products", task.getException());
                    }
                });
    }

    public static ListsView newInstanceWithProducts(String title, List<Product> products, boolean isSearch) {
        ListsView fragment = new ListsView();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putParcelableArrayList(ARG_PRODUCTS, new ArrayList<>(products));
        args.putBoolean("isSearch", isSearch); // Mark if it's a search
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 🔹 Updates UI based on products availability.
     */
    private void updateUI() {
        boolean isSearch = getArguments().getBoolean("isSearch", false);

        if (products == null) {
            products = new ArrayList<>();
        }

        if (products.isEmpty()) {
            no_results_text.setVisibility(View.VISIBLE);
            no_results_text.setText(isSearch ? getString(R.string.no_products_found) : getString(R.string.no_items_available));
            gridView.setVisibility(View.GONE);
        } else {
            no_results_text.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);
            if (productAdapter == null) {
                productAdapter = new ProductAdapter(requireContext(), products);
                gridView.setAdapter(productAdapter);
            } else {
                productAdapter.notifyDataSetChanged();
            }
        }

    }


}
