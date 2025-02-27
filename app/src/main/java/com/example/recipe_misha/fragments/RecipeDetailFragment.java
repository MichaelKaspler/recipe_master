package com.example.recipe_misha.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.recipe_misha.R;
import com.example.recipe_misha.data.Ingredient;
import com.example.recipe_misha.data.Recipe;
import com.example.recipe_misha.viewmodel.RecipeViewModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeDetailFragment extends Fragment {

    private static final String ARG_RECIPE_TITLE = "recipe_title";
    private static final String TAG = "RecipeDetailFragment";
    private static final String COLLECTION_RECIPES = "recipes";
    
    private String recipeTitle;
    private RecipeViewModel recipeViewModel;
    private NavController navController;
    private FirebaseFirestore db;
    
    private TextView textViewRecipeTitleDetail;
    private TextView textViewServingsDetail;
    private LinearLayout layoutIngredients;
    private TextView textViewInstructionsDetail;
    private Button buttonBack;
    private ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        
        if (getArguments() != null) {
            recipeTitle = getArguments().getString(ARG_RECIPE_TITLE);
            Log.d(TAG, "Recipe title from arguments: " + recipeTitle);
        } else {
            Log.w(TAG, "No arguments provided to RecipeDetailFragment");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        textViewRecipeTitleDetail = view.findViewById(R.id.textViewRecipeTitleDetail);
        textViewServingsDetail = view.findViewById(R.id.textViewServingsDetail);
        layoutIngredients = view.findViewById(R.id.layoutIngredients);
        textViewInstructionsDetail = view.findViewById(R.id.textViewInstructionsDetail);
        buttonBack = view.findViewById(R.id.buttonBack);

        progressBar = view.findViewById(R.id.progressBarDetail);
        if (progressBar == null) {
            progressBar = new ProgressBar(getContext());
            progressBar.setId(View.generateViewId());
            ((ViewGroup) view).addView(progressBar);
            progressBar.setVisibility(View.GONE);
        }

        buttonBack.setOnClickListener(v -> {
            navController.navigateUp();
        });

        recipeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
        
        return view;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        loadRecipeDetailsFromFirestore();
    }
    
    private void loadRecipeDetailsFromFirestore() {
        try {
            if (recipeTitle == null || recipeTitle.isEmpty()) {
                Log.e(TAG, "Recipe title is null or empty");
                Toast.makeText(getContext(), "Error: Recipe title is missing", Toast.LENGTH_SHORT).show();
                navController.navigateUp();
                return;
            }
            
            Log.d(TAG, "Loading recipe details from Firestore for: " + recipeTitle);
            showProgress(true);

            db.collection(COLLECTION_RECIPES)
                .whereEqualTo("title", recipeTitle)
                .get()
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            
                            Log.d(TAG, "Recipe found in Firestore with ID: " + document.getId());

                            String title = document.getString("title");
                            String servings = document.getString("servings");
                            String instructions = document.getString("instructions");
                            String source = document.getString("source");
                            
                            Recipe recipe = new Recipe();
                            recipe.setTitle(title != null ? title : "");
                            recipe.setServings(servings != null ? servings : "");
                            recipe.setInstructions(instructions != null ? instructions : "");
                            recipe.setSource(source != null ? source : "USER");

                            List<Map<String, String>> ingredientMaps = 
                                (List<Map<String, String>>) document.get("ingredients");
                            List<Ingredient> recipeIngredients = new ArrayList<>();
                            
                            if (ingredientMaps != null) {
                                for (Map<String, String> ingredientMap : ingredientMaps) {
                                    String name = ingredientMap.get("name");
                                    String amount = ingredientMap.get("amount");
                                    
                                    Ingredient ingredient = new Ingredient(
                                        name != null ? name : "",
                                        amount != null ? amount : ""
                                    );
                                    recipeIngredients.add(ingredient);
                                }
                            }
                            
                            recipe.setIngredients(recipeIngredients);
                            displayRecipe(recipe);
                            
                        } else {
                            Log.e(TAG, "Recipe not found in Firestore: " + recipeTitle);
                            Toast.makeText(getContext(), "Recipe not found in database", Toast.LENGTH_SHORT).show();
                            navController.navigateUp();
                        }
                    } else {
                        Log.e(TAG, "Error getting recipe from Firestore", task.getException());
                        Toast.makeText(getContext(), "Error loading recipe: " + 
                            (task.getException() != null ? task.getException().getMessage() : "Unknown error"), 
                            Toast.LENGTH_SHORT).show();
                        navController.navigateUp();
                    }
                });
        } catch (Exception e) {
            showProgress(false);
            Log.e(TAG, "Exception in loadRecipeDetailsFromFirestore", e);
            Toast.makeText(getContext(), "Error loading recipe: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            navController.navigateUp();
        }
    }
    
    private void displayRecipe(Recipe recipe) {
        try {
            Log.d(TAG, "Displaying recipe: " + recipe.getTitle());

            String title = recipe.getTitle();
            textViewRecipeTitleDetail.setText(title != null ? title : "");

            String servings = recipe.getServings();
            if (servings != null && !servings.isEmpty()) {
                textViewServingsDetail.setVisibility(View.VISIBLE);
                textViewServingsDetail.setText("Servings: " + servings);
            } else {
                textViewServingsDetail.setVisibility(View.GONE);
            }

            String instructions = recipe.getInstructions();
            textViewInstructionsDetail.setText(instructions != null ? instructions : "");

            layoutIngredients.removeAllViews();

            List<Ingredient> ingredients = recipe.getIngredients();
            if (ingredients != null) {
                for (Ingredient ingredient : ingredients) {
                    if (ingredient == null) continue;
                    
                    View ingredientView = getLayoutInflater().inflate(R.layout.ingredient_detail_item, layoutIngredients, false);
                    
                    TextView textViewIngredientName = ingredientView.findViewById(R.id.textViewIngredientName);
                    TextView textViewIngredientAmount = ingredientView.findViewById(R.id.textViewIngredientAmount);
                    
                    textViewIngredientName.setText(ingredient.getName() != null ? ingredient.getName() : "");
                    textViewIngredientAmount.setText(ingredient.getAmount() != null ? ingredient.getAmount() : "");
                    
                    layoutIngredients.addView(ingredientView);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying recipe: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error displaying recipe: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            navController.navigateUp();
        }
    }
    private void showProgress(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
} 