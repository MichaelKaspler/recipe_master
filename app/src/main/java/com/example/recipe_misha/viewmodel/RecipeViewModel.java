package com.example.recipe_misha.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.recipe_misha.data.FirebaseService;
import com.example.recipe_misha.data.Recipe;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeViewModel extends AndroidViewModel {
    private final FirebaseService firebaseService;
    private final MutableLiveData<List<Recipe>> allRecipes = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<Set<String>> recipeGroups = new MutableLiveData<>(new HashSet<>());

    public RecipeViewModel(@NonNull Application application) {
        super(application);
        firebaseService = FirebaseService.getInstance(application.getApplicationContext());
        loadRecipes();
    }

    private void loadRecipes() {
        firebaseService.getAllRecipes(new FirebaseService.RecipesCallback() {
            @Override
            public void onRecipesLoaded(List<Recipe> recipes) {
                allRecipes.postValue(recipes);
                updateRecipeGroups(recipes);
            }

            @Override
            public void onError(String errorMessage) {
                setErrorMessage(errorMessage);
                allRecipes.postValue(new ArrayList<>());
            }
        });
    }

    private void updateRecipeGroups(List<Recipe> recipes) {
        Set<String> groups = new HashSet<>();
        groups.add("General");
        
        if (recipes != null) {
            for (Recipe recipe : recipes) {
                if (recipe.getGroup() != null && !recipe.getGroup().isEmpty())
                    groups.add(recipe.getGroup());
            }
        }
        
        Set<String> existingGroups = recipeGroups.getValue();
        if (existingGroups != null) groups.addAll(existingGroups);
        
        recipeGroups.postValue(groups);
    }

    public void addGroup(String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            setErrorMessage("Group name empty");
            return;
        }
        
        Set<String> currentGroups = recipeGroups.getValue();
        if (currentGroups == null) currentGroups = new HashSet<>();
        
        if (currentGroups.contains(groupName)) {
            setErrorMessage("Group exists");
            return;
        }
        
        Set<String> updatedGroups = new HashSet<>(currentGroups);
        updatedGroups.add(groupName);
        recipeGroups.postValue(updatedGroups);
        setSuccessMessage("Group added");
    }

    public void insert(Recipe recipe) {
        if (recipe == null || recipe.getTitle() == null || recipe.getTitle().isEmpty()) {
            setErrorMessage("Title empty");
            return;
        }
        
        if (recipe.getGroup() == null || recipe.getGroup().isEmpty())
            recipe.setGroup("General");
        
        firebaseService.addRecipe(recipe, new FirebaseService.DatabaseCallback() {
            @Override
            public void onSuccess() {
                setSuccessMessage("Added");
                refreshRecipes();
            }

            @Override
            public void onError(String error) {
                setErrorMessage(error);
            }
        });
    }

    public void update(Recipe recipe) {
        if (recipe == null || recipe.getTitle() == null || recipe.getTitle().isEmpty()) {
            setErrorMessage("Title empty");
            return;
        }
        
        if (recipe.getGroup() == null || recipe.getGroup().isEmpty())
            recipe.setGroup("General");
        
        firebaseService.deleteRecipeByTitle(recipe.getTitle(), new FirebaseService.DatabaseCallback() {
            @Override
            public void onSuccess() {
                firebaseService.addRecipe(recipe, new FirebaseService.DatabaseCallback() {
                    @Override
                    public void onSuccess() {
                        setSuccessMessage("Updated");
                        refreshRecipes();
                    }

                    @Override
                    public void onError(String error) {
                        setErrorMessage(error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (error.contains("Recipe not found")) {
                    firebaseService.addRecipe(recipe, new FirebaseService.DatabaseCallback() {
                        @Override
                        public void onSuccess() {
                            setSuccessMessage("Added");
                            refreshRecipes();
                        }

                        @Override
                        public void onError(String error) {
                            setErrorMessage(error);
                        }
                    });
                } else {
                    setErrorMessage(error);
                }
            }
        });
    }

    public void update(String oldTitle, Recipe updatedRecipe) {
        if (oldTitle == null || oldTitle.isEmpty() || updatedRecipe == null || 
            updatedRecipe.getTitle() == null || updatedRecipe.getTitle().isEmpty()) {
            setErrorMessage("Title empty");
            return;
        }
        
        if (updatedRecipe.getGroup() == null || updatedRecipe.getGroup().isEmpty())
            updatedRecipe.setGroup("General");
        
        firebaseService.deleteRecipeByTitle(oldTitle, new FirebaseService.DatabaseCallback() {
            @Override
            public void onSuccess() {
                firebaseService.addRecipe(updatedRecipe, new FirebaseService.DatabaseCallback() {
                    @Override
                    public void onSuccess() {
                        setSuccessMessage("Updated");
                        refreshRecipes();
                    }

                    @Override
                    public void onError(String error) {
                        setErrorMessage(error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (error.contains("Recipe not found")) {
                    firebaseService.addRecipe(updatedRecipe, new FirebaseService.DatabaseCallback() {
                        @Override
                        public void onSuccess() {
                            setSuccessMessage("Added");
                            refreshRecipes();
                        }

                        @Override
                        public void onError(String error) {
                            setErrorMessage(error);
                        }
                    });
                } else {
                    setErrorMessage(error);
                }
            }
        });
    }

    public void delete(Recipe recipe) {
        if (recipe == null || recipe.getTitle() == null || recipe.getTitle().isEmpty()) {
            setErrorMessage("Title empty");
            return;
        }
        
        firebaseService.deleteRecipeByTitle(recipe.getTitle(), new FirebaseService.DatabaseCallback() {
            @Override
            public void onSuccess() {
                setSuccessMessage("Deleted");
                refreshRecipes();
            }

            @Override
            public void onError(String error) {
                setErrorMessage(error);
            }
        });
    }

    public LiveData<List<Recipe>> getAllRecipes() {
        return allRecipes;
    }
    public void refreshRecipes() {
        Set<String> existingGroups = recipeGroups.getValue();
        loadRecipes();
        
        if (existingGroups != null && !existingGroups.isEmpty()) {
            Set<String> currentGroups = recipeGroups.getValue();
            if (currentGroups == null) currentGroups = new HashSet<>();
            currentGroups.addAll(existingGroups);
            recipeGroups.postValue(currentGroups);
        }
    }

    public LiveData<Set<String>> getRecipeGroups() {
        return recipeGroups;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    private void setErrorMessage(String message) {
        errorMessage.postValue(message);
        new Handler(Looper.getMainLooper()).postDelayed(() -> errorMessage.postValue(null), 3000);
    }

    private void setSuccessMessage(String message) {
        successMessage.postValue(message);
        new Handler(Looper.getMainLooper()).postDelayed(() -> successMessage.postValue(null), 3000);
    }

    public void deleteGroup(String groupName) {
        if (groupName == null || groupName.trim().isEmpty() || "General".equals(groupName)) {
            setErrorMessage("Cannot delete group");
            return;
        }
        
        Set<String> currentGroups = recipeGroups.getValue();
        if (currentGroups == null || !currentGroups.contains(groupName)) {
            setErrorMessage("Group not found");
            return;
        }
        
        List<Recipe> recipes = allRecipes.getValue();
        if (recipes != null) {
            boolean hasChanges = false;
            
            for (Recipe recipe : recipes) {
                if (groupName.equals(recipe.getGroup())) {
                    recipe.setGroup("General");
                    hasChanges = true;
                    updateRecipeGroup(recipe);
                }
            }
            
            Set<String> updatedGroups = new HashSet<>(currentGroups);
            updatedGroups.remove(groupName);
            recipeGroups.postValue(updatedGroups);
            
            setSuccessMessage("Group deleted");
            
            if (hasChanges) refreshRecipes();
        }
    }
    
    private void updateRecipeGroup(Recipe recipe) {
        if (recipe == null || recipe.getTitle() == null || recipe.getTitle().isEmpty()) return;
        
        firebaseService.deleteRecipeByTitle(recipe.getTitle(), new FirebaseService.DatabaseCallback() {
            @Override
            public void onSuccess() {
                firebaseService.addRecipe(recipe, new FirebaseService.DatabaseCallback() {
                    @Override
                    public void onSuccess() {}

                    @Override
                    public void onError(String error) {}
                });
            }

            @Override
            public void onError(String error) {}
        });
    }
}