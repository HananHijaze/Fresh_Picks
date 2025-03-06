package com.example.fresh_picks;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fresh_picks.classes.Cart;
import com.example.fresh_picks.classes.Product;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder> {

    private final Cart cart;
    private final List<Product> cartItems;
    private final Context context;
    private final FirebaseFirestore db;

    public ShoppingCartAdapter(Cart cart, List<Product> cartItems, Context context) {
        this.cart = cart;
        this.cartItems = cartItems;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.shoppingcartcard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = cartItems.get(position);

        // Get quantity from cart (Fix for getQuantity() error)
        int quantity = cart.getItems().getOrDefault(product, 1);

        holder.productName.setText(product.getName());
        holder.productDescription.setText("Price: ₪" + product.getPrice());
        holder.quantity.setText(String.valueOf(quantity));

        Glide.with(context).load(product.getImageUrl()).into(holder.productImage);

        // Remove item from cart
        holder.buttonClose.setOnClickListener(v -> {
            removeItemFromCart(product, position);
        });

        // Increase quantity
        holder.buttonIncrement.setOnClickListener(v -> {
            updateQuantity(product, quantity + 1);
        });

        // Decrease quantity
        holder.buttonDecrement.setOnClickListener(v -> {
            if (quantity > 1) {
                updateQuantity(product, quantity - 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productDescription, quantity;
        ImageButton buttonClose, buttonIncrement, buttonDecrement;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productDescription = itemView.findViewById(R.id.productDescription);
            quantity = itemView.findViewById(R.id.textQuantity);
            buttonClose = itemView.findViewById(R.id.buttonClose);
            buttonIncrement = itemView.findViewById(R.id.buttonIncrement);
            buttonDecrement = itemView.findViewById(R.id.buttonDecrement);
        }
    }

    private void updateQuantity(Product product, int newQuantity) {
        cart.updateQuantity(product, newQuantity);
        notifyDataSetChanged();
    }

    private void removeItemFromCart(Product product, int position) {
        cart.removeItem(product);
        cartItems.remove(position);
        notifyItemRemoved(position);
        Toast.makeText(context, "Removed from cart", Toast.LENGTH_SHORT).show();
    }
}
