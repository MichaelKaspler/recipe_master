package com.example.recipe_misha.data;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseService {
    private static final String TAG = "FirebaseService";
    private static final String COLLECTION_RECIPES = "recipes";
    private FirebaseFirestore db;
    private Context applicationContext;
    private static FirebaseService instance;
    public interface DatabaseCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface RecipesCallback {
        void onRecipesLoaded(List<Recipe> recipes);
        void onError(String errorMessage);
    }
    
    private FirebaseService(Context context) {
        this.applicationContext = context.getApplicationContext();
        
        if (FirebaseApp.getApps(applicationContext).isEmpty()) {
            return;
        }
        
        db = FirebaseFirestore.getInstance();
        
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build();
        db.setFirestoreSettings(settings);
    }
    
    public static synchronized FirebaseService getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseService(context);
        }
        return instance;
    }

    public void addRecipe(Recipe recipe, DatabaseCallback callback) {
        if (recipe == null || recipe.getTitle() == null || recipe.getTitle().trim().isEmpty() || db == null) {
            callback.onError("Invalid recipe or Firebase not initialized");
            return;
        }
        Map<String, Object> recipeMap = new HashMap<>();
        recipeMap.put("title", recipe.getTitle());
        recipeMap.put("servings", recipe.getServings());
        recipeMap.put("instructions", recipe.getInstructions());
        recipeMap.put("source", recipe.getSource());
        recipeMap.put("group", recipe.getGroup());
        
        List<Map<String, String>> ingredientsList = new ArrayList<>();
        if (recipe.getIngredients() != null) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                Map<String, String> ingredientMap = new HashMap<>();
                ingredientMap.put("name", ingredient.getName());
                ingredientMap.put("amount", ingredient.getAmount());
                ingredientsList.add(ingredientMap);
            }
        }
        recipeMap.put("ingredients", ingredientsList);
        
        db.collection(COLLECTION_RECIPES)
            .add(recipeMap)
            .addOnSuccessListener(documentReference -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onError("Failed to save recipe: " + e.getMessage()));
    }

    public void getAllRecipes(RecipesCallback callback) {
        if (db == null) {
            callback.onError("Firebase not initialized properly");
            return;
        }
        db.collection(COLLECTION_RECIPES)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Recipe> recipes = new ArrayList<>();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    String title = document.getString("title");
                    String servings = document.getString("servings");
                    String instructions = document.getString("instructions");
                    String source = document.getString("source");
                    String group = document.getString("group");
                    
                    Recipe recipe = new Recipe();
                    recipe.setTitle(title != null ? title : "");
                    recipe.setServings(servings != null ? servings : "");
                    recipe.setInstructions(instructions != null ? instructions : "");
                    recipe.setSource(source != null ? source : "USER");
                    recipe.setGroup(group != null ? group : "General");
                    
                    List<Map<String, String>> ingredientMaps = 
                        (List<Map<String, String>>) document.get("ingredients");
                    List<Ingredient> ingredients = new ArrayList<>();
                    
                    if (ingredientMaps != null) {
                        for (Map<String, String> ingredientMap : ingredientMaps) {
                            String name = ingredientMap.get("name");
                            String amount = ingredientMap.get("amount");
                            
                            Ingredient ingredient = new Ingredient(
                                name != null ? name : "",
                                amount != null ? amount : ""
                            );
                            ingredients.add(ingredient);
                        }
                    }
                    
                    recipe.setIngredients(ingredients);
                    recipes.add(recipe);
                }
                
                callback.onRecipesLoaded(recipes);
            })
            .addOnFailureListener(e -> callback.onError("Failed to load recipes: " + e.getMessage()));
    }

    public void deleteRecipeByTitle(String title, DatabaseCallback callback) {
        if (title == null || title.trim().isEmpty() || db == null) {
            callback.onError("Invalid title or Firebase not initialized");
            return;
        }
        
        db.collection(COLLECTION_RECIPES)
            .whereEqualTo("title", title)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    callback.onError("Recipe not found");
                    return;
                }
                
                queryDocumentSnapshots.getDocuments().get(0).getReference().delete()
                    .addOnSuccessListener(unused -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onError("Failed to delete recipe: " + e.getMessage()));
            })
            .addOnFailureListener(e -> callback.onError("Failed to find recipe: " + e.getMessage()));
    }
} 