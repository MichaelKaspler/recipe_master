package com.example.recipe_misha.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipe_misha.data.ApiRecipe;
import com.example.recipe_misha.R;

import java.util.ArrayList;
import java.util.List;

public class ApiRecipeAdapter extends RecyclerView.Adapter<ApiRecipeAdapter.ApiRecipeViewHolder> {

    private List<ApiRecipe> apiRecipes = new ArrayList<>();
    private final OnApiRecipeClickListener listener;
    public interface OnApiRecipeClickListener {
        void onApiRecipeClick(ApiRecipe recipe);
    }
    public ApiRecipeAdapter(OnApiRecipeClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ApiRecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_api_recipe, parent, false);
        return new ApiRecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApiRecipeViewHolder holder, int position) {
        ApiRecipe recipe = apiRecipes.get(position);
        holder.textTitle.setText(recipe.getTitle());

        String ingredients = recipe.getIngredients();
        if (ingredients != null && ingredients.length() > 50) {
            ingredients = ingredients.substring(0, 50) + "...";
        }
        holder.textIngredients.setText(ingredients);

        if (recipe.getServings() != null && !recipe.getServings().isEmpty()) {
            holder.textServings.setText("Servings: " + recipe.getServings());
            holder.textServings.setVisibility(View.VISIBLE);
        } else {
            holder.textServings.setVisibility(View.GONE);
        }

        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onApiRecipeClick(recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return apiRecipes.size();
    }

    public void setApiRecipes(List<ApiRecipe> apiRecipes) {
        this.apiRecipes = apiRecipes;
        notifyDataSetChanged();
    }

    public void clearApiRecipes() {
        this.apiRecipes.clear();
        notifyDataSetChanged();
    }

    static class ApiRecipeViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textTitle;
        TextView textIngredients;
        TextView textServings;

        ApiRecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textIngredients = itemView.findViewById(R.id.textIngredients);
            textServings = itemView.findViewById(R.id.textServings);
        }
    }
} 