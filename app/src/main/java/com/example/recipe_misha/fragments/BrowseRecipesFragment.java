package com.example.recipe_misha.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipe_misha.data.ApiRecipe;
import com.example.recipe_misha.R;
import com.example.recipe_misha.adapter.ApiRecipeAdapter;
import com.example.recipe_misha.data.FirebaseService;
import com.example.recipe_misha.data.RecipeApiService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.List;

public class BrowseRecipesFragment extends Fragment implements ApiRecipeAdapter.OnApiRecipeClickListener {

    private static final String TAG = "BrowseRecipesFragment";
    
    private TextInputEditText editTextKeyword;
    private RecyclerView recyclerViewApiRecipes;
    private ProgressBar progressBar;
    private TextView textViewNoResults;
    private Button buttonSearch;

    private ApiRecipeAdapter apiRecipeAdapter;
    private RecipeApiService apiService;
    private FirebaseService firebaseService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_recipes, container, false);
        
        editTextKeyword = view.findViewById(R.id.editTextKeyword);
        buttonSearch = view.findViewById(R.id.buttonSearch);
        recyclerViewApiRecipes = view.findViewById(R.id.recyclerViewApiRecipes);
        progressBar = view.findViewById(R.id.progressBar);
        textViewNoResults = view.findViewById(R.id.textViewNoResults);

        firebaseService = FirebaseService.getInstance(requireContext());
        
        setupRecyclerView();
        setupButtons();

        apiService = new RecipeApiService();

        searchRecipes();
        
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (apiService != null) {
            apiService.shutdown();
        }
    }

    private void setupRecyclerView() {
        apiRecipeAdapter = new ApiRecipeAdapter(this);
        recyclerViewApiRecipes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewApiRecipes.setAdapter(apiRecipeAdapter);
    }

    private void setupButtons() {
        buttonSearch.setOnClickListener(v -> searchRecipes());
    }

    private void searchRecipes() {
        String keyword = editTextKeyword.getText() != null ? editTextKeyword.getText().toString().trim() : "";

        progressBar.setVisibility(View.VISIBLE);
        textViewNoResults.setVisibility(View.GONE);

        apiRecipeAdapter.clearApiRecipes();

        if (TextUtils.isEmpty(keyword)) {
            apiService.searchRecipes("a", new RecipeApiService.RecipeApiCallback() {
                @Override
                public void onSuccess(List<ApiRecipe> recipes) {
                    handleApiResponse(recipes);
                }

                @Override
                public void onError(String errorMessage) {
                    handleApiError(errorMessage);
                }
            });
        } else {
            apiService.searchRecipes(keyword, new RecipeApiService.RecipeApiCallback() {
                @Override
                public void onSuccess(List<ApiRecipe> recipes) {
                    handleApiResponse(recipes);
                }

                @Override
                public void onError(String errorMessage) {
                    handleApiError(errorMessage);
                }
            });
        }
    }

    private void handleApiResponse(List<ApiRecipe> recipes) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                
                if (recipes.isEmpty()) {
                    textViewNoResults.setVisibility(View.VISIBLE);
                } else {
                    textViewNoResults.setVisibility(View.GONE);
                    apiRecipeAdapter.setApiRecipes(recipes);
                }
            });
        }
    }

    //for errors
    private void handleApiError(String errorMessage) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onApiRecipeClick(ApiRecipe recipe) {
        try {
            Gson gson = new Gson();
            String recipeJson = gson.toJson(recipe);

            Bundle args = new Bundle();
            args.putString("recipe_json", recipeJson);

            Navigation.findNavController(requireView())
                .navigate(R.id.action_browseRecipesFragment_to_apiRecipeDetailFragment, args);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to recipe detail: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error opening recipe: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
} 