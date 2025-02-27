package com.example.recipe_misha.data;

import java.util.List;

public class Recipe {
    private String title;
    private List<Ingredient> ingredients;
    private String instructions;
    private String servings;
    private String source = "USER";
    private String group = "General";

    public Recipe() {
    }
    public Recipe(String title, List<Ingredient> ingredients, String instructions) {
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }
    public Recipe(String title, List<Ingredient> ingredients, String instructions, String source) {
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.source = source;
    }

    public Recipe(String title, List<Ingredient> ingredients, String instructions, String source, String group) {
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.source = source;
        this.group = group != null ? group : "General";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getServings() {
        return servings;
    }
    
    public void setServings(String servings) {
        this.servings = servings;
    }
    
    public String getGroup() {
        return group != null ? group : "General";
    }
    
    public void setGroup(String group) {
        this.group = group != null ? group : "General";
    }
} 