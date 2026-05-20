package com.example.movietracker.data.api;

import com.example.movietracker.model.gemini.GeminiRequest;
import com.example.movietracker.model.gemini.GeminiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

import retrofit2.http.Path;

public interface GeminiApiService {
    @Headers("Content-Type: application/json")
    @POST("v1beta/models/{model}:generateContent")
    Call<GeminiResponse> generateContent(
        @Path("model") String model,
        @Query("key") String apiKey,
        @Body GeminiRequest request
    );
}
