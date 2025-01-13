package com.example.fresh_picks.APIS;

import com.example.fresh_picks.classes.Coupon;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface CouponApiService {
    @GET("api/coupons")
    Call<List<Coupon>> getCoupons();

    @POST("api/coupons/validate")
    Call<Boolean> validateCoupon(@Body Coupon coupon);
}
