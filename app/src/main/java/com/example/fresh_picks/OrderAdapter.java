package com.example.fresh_picks;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fresh_picks.R;
import com.example.fresh_picks.RecipeActivity;
import com.example.fresh_picks.classes.Order;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_card, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        // Set order details
        holder.tvOrderDate.setText(context.getString(R.string.order_date) + " " + order.getCreatedAt());
        holder.tvTotalPrice.setText(context.getString(R.string.total_price) + " $" + order.getTotalPrice());
        holder.tvPaymentMethod.setText(context.getString(R.string.payment_method) + " " + order.getPaymentMethod());
        holder.tvShippingMethod.setText(context.getString(R.string.shipping_method) + " " + order.getShippingMethod());

        // Load products
        holder.productContainer.removeAllViews();
        for (Map.Entry<String, Integer> entry : order.getProductQuantities().entrySet()) {
            View productView = LayoutInflater.from(context).inflate(R.layout.listcard1, holder.productContainer, false);

            ImageView productImage = productView.findViewById(R.id.product_image);
            TextView productTitle = productView.findViewById(R.id.product_title_text);

            String productId = entry.getKey();
            int quantity = entry.getValue();

            db.collection("products").document(productId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String productName = documentSnapshot.getString("name");
                            double price = documentSnapshot.contains("price") ? documentSnapshot.getDouble("price") : 0.0;
                            String unitSize = documentSnapshot.contains("unit") ? documentSnapshot.getString("unit") : "unit";

                            productTitle.setText(productName + "\n" + quantity + " " + unitSize + " * $" + price);

                            if (documentSnapshot.contains("imageUrl")) {
                                Glide.with(context)
                                        .load(documentSnapshot.getString("imageUrl"))
                                        .placeholder(R.drawable.noconnection3)
                                        .error(R.drawable.logo)
                                        .fitCenter()
                                        .into(productImage);
                            } else {
                                productImage.setImageResource(R.drawable.logo);
                            }
                        }
                    });

            holder.productContainer.addView(productView);
        }

        // Open RecipeActivity on click
        holder.orderCard.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeActivity.class);
            intent.putExtra("orderId", order.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void updateOrders(List<Order> newOrders) {
        this.orderList = newOrders;
        notifyDataSetChanged();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderDate, tvTotalPrice, tvPaymentMethod, tvShippingMethod;
        LinearLayout productContainer;
        CardView orderCard;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date_time);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            tvPaymentMethod = itemView.findViewById(R.id.tv_payment_method);
            tvShippingMethod = itemView.findViewById(R.id.tv_shipping_method);
            productContainer = itemView.findViewById(R.id.product_container);
            orderCard = itemView.findViewById(R.id.order_card);
        }
    }
}
