package com.example.fresh_picks.APIS;

import com.example.fresh_picks.classes.Category;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CategoryApiService {
    @GET("api/categories")
    Call<List<Category>> getCategories();

    @POST("api/categories")
    Call<Void> addCategory(@Body Category category);

    @PUT("api/categories/{id}")
    Call<Void> updateCategory(@Path("id") int id, @Body Category category);

    @DELETE("api/categories/{id}")
    Call<Void> deleteCategory(@Path("id") int id);
}
