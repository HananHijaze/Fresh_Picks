package com.example.fresh_picks;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.fresh_picks.APIS.ProductApiService;
import com.example.fresh_picks.APIS.RetrofitClient;
import com.example.fresh_picks.classes.Product;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListsView extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private String category;

    private GridView gridView;

    public ListsView() {
        // Required empty public constructor
    }

    public static ListsView newInstance(String category) {
        ListsView fragment = new ListsView();
        Bundle args = new Bundle();

        // Convert the category to lowercase
        category = category.toLowerCase(Locale.ROOT);

        args.putString(ARG_PARAM1, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString(ARG_PARAM1);

            // Ensure category is always in lowercase
            if (category != null) {
                category = category.toLowerCase(Locale.ROOT);
            }

            Log.d("ListsView", "Category received: " + category);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lists_view, container, false);

        // Set the category title
        TextView textView = view.findViewById(R.id.textView);
        textView.setText(category);

        gridView = view.findViewById(R.id.grid_view);

        fetchProductsByCategory();

        return view;
    }

    private void fetchProductsByCategory() {
        ProductApiService productApiService = RetrofitClient.getRetrofitInstance().create(ProductApiService.class);

        Call<List<Product>> call = productApiService.getProductsByCategory(category);
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();
                    Log.d("ListsView", "Products fetched: " + products.toString());
                    ProductAdapter adapter = new ProductAdapter(requireContext(), products);
                    gridView.setAdapter(adapter);
                } else {
                    Log.e("ListsView", "Failed to fetch products: " + response.message());
                    Toast.makeText(requireContext(), "No products found for this category", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e("ListsView", "Error fetching products", t);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
