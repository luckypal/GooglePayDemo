package com.example.myapplication;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface DemoAPI {

    @POST("/app/fcm/v1/driver")
    Call<TaxiDriver> save(@Body TaxiDriver driver);

    @GET("/app/fcm/v1/accept/{token}/{messageId}")
    Call<String> accept(@Path("token") String toke, @Path("messageId") String messageId);

}
