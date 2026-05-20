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

    private static final String[] MODELS = {
        "gemini-2.5-flash",
        "gemini-3.1-flash-lite",
        "gemini-2.0-flash",
        "gemini-1.5-flash"
    };

    public void generateRecommendation(String context, String userPrompt, GeminiCallback callback) {
        generateRecommendationWithModel(context, userPrompt, 0, callback);
    }

    private void generateRecommendationWithModel(String context, String userPrompt, int modelIndex, GeminiCallback callback) {
        if (modelIndex >= MODELS.length) {
            callback.onError("All Gemini models failed or are currently unavailable due to high load.");
            return;
        }

        String model = MODELS[modelIndex];
        GeminiRequest request = new GeminiRequest();
        
        request.setSystemInstruction("You are a helpful movie and TV show recommendation assistant. " +
            "You have access to the user's offline database of tracked shows. " +
            "Use this context to provide personalized recommendations. " +
            "IMPORTANT: Do NOT recommend any movies or TV shows that are already in the user's tracked shows context. " +
            "For each recommended movie/show, write the name first as a Markdown hyperlink with its IMDb ID as the URL, followed by its description, which must be kept under 20 words (e.g., '**[Inception](tt1375666)**: A thief who steals corporate secrets through dream-sharing technology.'). " +
            "CRITICAL: You must provide the EXACT, REAL, and CORRECT IMDb ID (e.g., tt1375666) in the hyperlink URL. Do NOT hallucinate or guess the IMDb ID; it must match the show's actual IMDb ID exactly, otherwise the details page will fail to load. " +
            "Only add Markdown hyperlinks for the newly recommended titles. Do NOT add hyperlinks for any shows that the user has already watched or which you are referencing from their history; write those referenced shows as plain text only (e.g., Inception, Breaking Bad). " +
            "Format your response nicely, keep it concise, and strictly ensure that every recommendation's description is less than 20 words.");

        String combinedPrompt = "User's tracked shows context:\n" + context + "\n\nUser request: " + userPrompt;
        request.addContent("user", combinedPrompt);

        android.util.Log.d("GeminiRepository", "Attempting request with model: " + model);

        apiService.generateContent(model, apiKey, request).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GeminiResponse geminiResponse = response.body();
                    if (geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {
                        GeminiResponse.Candidate candidate = geminiResponse.getCandidates().get(0);
                        if (candidate.getContent() != null && candidate.getContent().getParts() != null && !candidate.getContent().getParts().isEmpty()) {
                            String text = candidate.getContent().getParts().get(0).getText();
                            android.util.Log.i("GeminiRepository", "Successfully fetched recommendation using model: " + model);
                            callback.onSuccess(text);
                            return;
                        }
                    }
                    android.util.Log.w("GeminiRepository", "Model " + model + " returned empty or invalid response. Trying next fallback...");
                    generateRecommendationWithModel(context, userPrompt, modelIndex + 1, callback);
                } else {
                    String errorBody = "Unknown error";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    android.util.Log.e("GeminiRepository", "Model " + model + " failed with code " + response.code() + ": " + response.message() + "\nBody: " + errorBody);
                    generateRecommendationWithModel(context, userPrompt, modelIndex + 1, callback);
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                android.util.Log.e("GeminiRepository", "Model " + model + " failed with network error: " + t.getMessage());
                generateRecommendationWithModel(context, userPrompt, modelIndex + 1, callback);
            }
        });
    }
}
