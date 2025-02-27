package com.example.recipe_misha.data;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RecipeApiService {
    private static final String API_URL = "https://api.api-ninjas.com/v1/recipe";
    private static final String API_KEY = "k9KqLFxbq7/ddeC9j6yFkA==dc2PzcuiGNST9NoH";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final Type recipeListType = new TypeToken<ArrayList<ApiRecipe>>(){}.getType();
    
    public interface RecipeApiCallback {
        void onSuccess(List<ApiRecipe> recipes);
        void onError(String errorMessage);
    }

    public void searchRecipes(String query, RecipeApiCallback callback) {
        executor.execute(() -> {
            try {
                HttpUrl url = HttpUrl.parse(API_URL).newBuilder()
                    .addQueryParameter("query", query)
                    .build();
                
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("X-Api-Key", API_KEY)
                    .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<ApiRecipe> recipes = gson.fromJson(response.body().string(), recipeListType);
                        mainHandler.post(() -> callback.onSuccess(recipes != null ? recipes : new ArrayList<>()));
                    } else {
                        mainHandler.post(() -> callback.onError("API Error: " + response.code()));
                    }
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
    public void shutdown() {
        executor.shutdown();
    }
} 