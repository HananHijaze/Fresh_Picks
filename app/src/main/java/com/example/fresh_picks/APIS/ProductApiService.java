package com.example.fresh_picks.APIS;

import com.example.fresh_picks.classes.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ProductApiService {
    @GET("api/products")
    Call<List<Product>> getProducts();

    @GET("api/products/{id}")
    Call<Product> getProductById(@Path("id") int id);

    @POST("api/products")
    Call<Void> addProduct(@Body Product product);

    @PUT("api/products/{id}")
    Call<Void> updateProduct(@Path("id") int id, @Body Product product);

    @DELETE("api/products/{id}")
    Call<Void> deleteProduct(@Path("id") int id);
}
