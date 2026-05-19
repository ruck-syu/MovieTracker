package com.example.movietracker.data.api;

import com.example.movietracker.model.gemini.GeminiRequest;
import com.example.movietracker.model.gemini.GeminiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GeminiApiService {
    @Headers("Content-Type: application/json")
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    Call<GeminiResponse> generateContent(
        @Query("key") String apiKey,
        @Body GeminiRequest request
    );
}
