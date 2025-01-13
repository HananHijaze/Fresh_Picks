package com.example.fresh_picks.APIS;

import com.example.fresh_picks.classes.Order;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface OrderApiService {
    @POST("api/orders")
    Call<Void> placeOrder(@Body Order order);

    @GET("api/orders/user/{userId}")
    Call<List<Order>> getOrdersByUser(@Path("userId") int userId);

    @PUT("api/orders/{id}/status")
    Call<Void> updateOrderStatus(@Path("id") int id, @Body String status);
}
