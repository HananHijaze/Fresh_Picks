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
import retrofit2.http.Query;

public interface ProductApiService {
    @GET("api/products")
    Call<List<Product>> getProducts();

    @GET("api/products/{id}")
    Call<Product> getProductById(@Path("id") String id);

    @POST("api/products")
    Call<Void> addProduct(@Body Product product);

    @PUT("api/products/{id}")
    Call<Void> updateProduct(@Path("id") String id, @Body Product product);

    @DELETE("api/products/{id}")
    Call<Void> deleteProduct(@Path("id") String id);

    @GET("api/products/all")
    Call<List<Product>> getAllProducts();

    @GET("api/products/category")
    Call<List<Product>> getProductsByCategory(@Query("category") String category);
}
