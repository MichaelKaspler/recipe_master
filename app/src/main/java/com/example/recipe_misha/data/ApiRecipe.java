package com.example.recipe_misha.data;

import java.util.List;
import java.util.ArrayList;

public class ApiRecipe {
    private String title;
    private String ingredients;
    private String servings;
    private String instructions;
    private boolean isSaved = false;
    public ApiRecipe() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getServings() {
        return servings;
    }

    public void setServings(String servings) {
        this.servings = servings;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }
    public Recipe toRecipe() {
        Recipe recipe = new Recipe();
        recipe.setTitle(this.title != null ? this.title : "");
        recipe.setInstructions(this.instructions != null ? this.instructions : "");

        List<Ingredient> ingredientList = new ArrayList<>();
        List<String> ingredientStrings = getIngredientsList();
        for (String ingredientString : ingredientStrings) {
            if (ingredientString != null && !ingredientString.trim().isEmpty()) {
                ingredientList.add(new Ingredient(ingredientString.trim(), ""));
            }
        }
        recipe.setIngredients(ingredientList);
        recipe.setSource("API");
        return recipe;
    }

    //Making a list of ingredients from the string \/
    public List<String> getIngredientsList() {
        List<String> ingredientsList = new ArrayList<>();
        if (ingredients != null && !ingredients.isEmpty()) {
            String[] items = ingredients.split("\\|");
            for (String item : items) {
                ingredientsList.add(item.trim());
            }
        }
        return ingredientsList;
    }
} 