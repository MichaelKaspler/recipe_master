package com.example.recipe_misha.data;

import androidx.annotation.NonNull;

public class Ingredient {
    private String name;
    private String amount;

    public Ingredient() {
        this.name = "";
        this.amount = "";
    }
    public Ingredient(String name, String amount) {
        this.name = name != null ? name : "";
        this.amount = amount != null ? amount : "";
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name : "";
    }
    public String getAmount() {
        return amount;
    }
    public void setAmount(String amount) {
        this.amount = amount != null ? amount : "";
    }
    @NonNull
    @Override
    public String toString() {
        if (amount != null && !amount.isEmpty()) {
            return amount + " " + name;
        }
        return name;
    }
} 