package com.example.fresh_picks;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.fresh_picks.classes.Product;

import java.util.List;

public class ProductAdapter extends ArrayAdapter<Product> {

    private final Context context;
    private final List<Product> products;

    public ProductAdapter(@NonNull Context context, @NonNull List<Product> products) {
        super(context, 0, products);
        this.context = context;
        this.products = products;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.listcard, parent, false);
        }

        // Get the product at the current position
        Product product = products.get(position);

        // Bind the product details to the UI components
        ImageView productImage = convertView.findViewById(R.id.product_image);
        TextView productPrice = convertView.findViewById(R.id.product_price);
        TextView productTitle = convertView.findViewById(R.id.product_title);

        // Set the product image
        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.logo) // Placeholder
                .error(R.drawable.noconnection3) // Error fallback
                .into(productImage);

        // Set the product title and price
        productTitle.setText(product.getName());
        productPrice.setText(String.format("%.2f ILS", product.getPrice()));

        Log.d("ProductAdapter", "Binding product: " + product.toString());

        return convertView;
    }
}
