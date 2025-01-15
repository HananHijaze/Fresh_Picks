package com.example.fresh_picks.APIS;

import com.example.fresh_picks.ProductDeserializer;
import com.example.fresh_picks.classes.Product;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.0.7:8080/"; // Replace with your backend API URL
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // Create a Gson instance with the custom deserializer
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Product.class, new ProductDeserializer())
                    .create();

            // Use the Gson instance with the custom deserializer in Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson)) // Pass the custom Gson instance
                    .build();
        }
        return retrofit;
    }
}
