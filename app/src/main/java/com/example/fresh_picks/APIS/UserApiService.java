package com.example.fresh_picks.APIS;

import com.example.fresh_picks.classes.LoginRequest;
import com.example.fresh_picks.classes.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import retrofit2.http.Path;

public interface UserApiService {

    // Endpoint to register a user
    @POST("api/users/register")
    Call<User> registerUser(@Body User user);

    // Endpoint to login a user
    @POST("api/users/login")
    Call<String> loginUser(@Body LoginRequest loginRequest);

    // Endpoint to get a user by ID
    @GET("api/users/{id}")
    Call<User> getUserById(@Path("id") Long id);

    // Endpoint to update a user
    @PUT("api/users/{id}")
    Call<User> updateUser(@Path("id") Long id, @Body User user);

    // Endpoint to delete a user
    @DELETE("api/users/{id}")
    Call<Void> deleteUser(@Path("id") Long id);
}
