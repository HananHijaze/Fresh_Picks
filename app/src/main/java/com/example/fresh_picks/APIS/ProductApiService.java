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

    // Retrieve all products
    @GET("api/products")
    Call<List<Product>> getProducts();

    // Retrieve a product by its ID
    @GET("api/products/{id}")
    Call<Product> getProductById(@Path("id") String id);

    // Add a new product
    @POST("api/products")
    Call<Void> addProduct(@Body Product product);

    // Update an existing product by its ID
    @PUT("api/products/{id}")
    Call<Void> updateProduct(@Path("id") String id, @Body Product product);

    // Delete a product by its ID
    @DELETE("api/products/{id}")
    Call<Void> deleteProduct(@Path("id") String id);

    // Retrieve all products in a specific category
    @GET("api/products/category")
    Call<List<Product>> getProductsByCategory(@Query("category") String category);

    // Retrieve products based on dietary information
    @GET("api/products/dietary")
    Call<List<Product>> getProductsByDietaryInfo(@Query("dietaryInfo") String dietaryInfo);

    // Retrieve products by cuisine tags
    @GET("api/products/cuisine")
    Call<List<Product>> getProductsByCuisineTags(@Query("cuisineTag") String cuisineTag);

    // Retrieve seasonal products
    @GET("api/products/seasonal")
    Call<List<Product>> getSeasonalProducts();

    // Retrieve products by food pairing
    @GET("api/products/food-pairing")
    Call<List<Product>> getProductsByFoodPairing(@Query("foodPairing") String foodPairing);

    // Retrieve recipe suggestions for a specific product
    @GET("api/products/{id}/recipes")
    Call<List<String>> getRecipeSuggestions(@Path("id") String id);
}
