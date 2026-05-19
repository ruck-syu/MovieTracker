package com.example.movietracker.data.repository;

import com.example.movietracker.data.api.GeminiApiClient;
import com.example.movietracker.data.api.GeminiApiService;
import com.example.movietracker.model.gemini.GeminiRequest;
import com.example.movietracker.model.gemini.GeminiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeminiRepository {
    private final GeminiApiService apiService;
    private final String apiKey;

    public GeminiRepository(String apiKey) {
        this.apiService = GeminiApiClient.getApiService();
        this.apiKey = apiKey;
    }

    public interface GeminiCallback {
        void onSuccess(String responseText);
        void onError(String errorMessage);
    }

    public void generateRecommendation(String context, String userPrompt, GeminiCallback callback) {
        GeminiRequest request = new GeminiRequest();
        String systemInstruction = "You are a helpful movie and TV show recommendation assistant. " +
            "You have access to the user's offline database of tracked shows. " +
            "Use this context to provide personalized recommendations. " +
            "IMPORTANT: Do NOT recommend any movies or TV shows that are already in the user's tracked shows context. " +
            "Format your response nicely and keep it concise.";

        String combinedPrompt = "System Instruction: " + systemInstruction + "\n\nUser's tracked shows context:\n" + context + "\n\nUser request: " + userPrompt;
        request.addContent("user", combinedPrompt);

        apiService.generateContent(apiKey, request).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GeminiResponse geminiResponse = response.body();
                    if (geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {
                        GeminiResponse.Candidate candidate = geminiResponse.getCandidates().get(0);
                        if (candidate.getContent() != null && candidate.getContent().getParts() != null && !candidate.getContent().getParts().isEmpty()) {
                            String text = candidate.getContent().getParts().get(0).getText();
                            callback.onSuccess(text);
                            return;
                        }
                    }
                    callback.onError("Received empty or invalid response from Gemini API.");
                } else {
                    String errorBody = "Unknown error";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    callback.onError("Failed to fetch recommendation: " + response.code() + " " + response.message() + "\nBody: " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}
