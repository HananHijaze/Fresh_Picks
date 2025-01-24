package com.example.fresh_picks;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.fresh_picks.R;
import com.example.fresh_picks.classes.Product;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class ProductAdapter extends ArrayAdapter<Product> {

    private final Context context;
    private final List<Product> products;

    public ProductAdapter(Context context, List<Product> products) {
        super(context, 0, products);
        this.context = context;
        this.products = products;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.listcard, parent, false);
        }

        // Get the product at the current position
        Product product = products.get(position);

        // Bind the product details to the UI components
        ImageView productImage = convertView.findViewById(R.id.product_image);
        TextView productPrice = convertView.findViewById(R.id.product_price);
        TextView productTitle = convertView.findViewById(R.id.product_title_text); // Updated id

        // Set product data
        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.logo)
                .into(productImage);

        productTitle.setText(product.getName());
        productPrice.setText(String.format("₪%.2f", product.getPrice()));

        // Set click listener to show the bottom sheet
        convertView.setOnClickListener(v -> showProductDetails(product));

        return convertView;
    }


    private void showProductDetails(Product product) {
        // Inflate the layout for the BottomSheetDialog
        View sheetView = LayoutInflater.from(context).inflate(R.layout.addtocart, null);

        // Initialize BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(sheetView);

        try {
            // Bind the views
            ImageView productImage = sheetView.findViewById(R.id.product_image);
            TextView productPrice = sheetView.findViewById(R.id.product_price);
            TextView productName = sheetView.findViewById(R.id.product_name);
            TextView quantityText = sheetView.findViewById(R.id.quantity_text);
            Button minusButton = sheetView.findViewById(R.id.quantity_minus);
            Button plusButton = sheetView.findViewById(R.id.quantity_plus);
            Button addToCartButton = sheetView.findViewById(R.id.add_to_cart_button);

            // Log to verify the views
            Log.d("ProductAdapter", "Product Image: " + (productImage != null));
            Log.d("ProductAdapter", "Product Price: " + (productPrice != null));
            Log.d("ProductAdapter", "Product Name: " + (productName != null));
            Log.d("ProductAdapter", "Quantity Text: " + (quantityText != null));

            // Load product image
            Glide.with(context).load(product.getImageUrl()).into(productImage);

            // Set product details
            productName.setText(product.getName());
            productPrice.setText(String.format("₪%.2f", product.getPrice()));

            // Handle quantity selection
            final int[] quantity = {1};
            quantityText.setText(String.valueOf(quantity[0]));

            minusButton.setOnClickListener(v -> {
                if (quantity[0] > 1) {
                    quantity[0]--;
                    quantityText.setText(String.valueOf(quantity[0]));
                }
            });

            plusButton.setOnClickListener(v -> {
                quantity[0]++;
                quantityText.setText(String.valueOf(quantity[0]));
            });

            // Handle "Add to Cart" button click
            addToCartButton.setOnClickListener(v -> {
                // Add the product to the cart with the selected quantity
                Log.d("ProductAdapter", "Added to cart: " + product.getName() + ", Quantity: " + quantity[0]);
                bottomSheetDialog.dismiss();
            });

            // Show the BottomSheetDialog
            bottomSheetDialog.show();

        } catch (ClassCastException e) {
            Log.e("ProductAdapter", "View casting error: " + e.getMessage());
            bottomSheetDialog.dismiss(); // Dismiss the dialog in case of an error
        }
    }
}
