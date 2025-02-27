package com.example.recipe_misha.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipe_misha.R;
import com.example.recipe_misha.data.Recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class GroupedRecipeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_GROUP = 0;
    private static final int TYPE_RECIPE = 1;

    private List<Object> items = new ArrayList<>();
    private Map<String, List<Recipe>> groupedRecipes = new TreeMap<>();
    private OnRecipeActionListener listener;
    private OnGroupActionListener groupListener;
    private String expandedGroup = null;

    public interface OnRecipeActionListener {
        void onViewRecipe(Recipe recipe);
        void onEditRecipe(Recipe recipe);
        void onDeleteRecipe(Recipe recipe);
    }
    
    public interface OnGroupActionListener {
        void onDeleteGroup(String groupName);
    }

    public GroupedRecipeAdapter(OnRecipeActionListener listener) {
        this.listener = listener;
        if (listener instanceof OnGroupActionListener) {
            this.groupListener = (OnGroupActionListener) listener;
        }
    }
    
    public void setOnGroupActionListener(OnGroupActionListener listener) {
        this.groupListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_GROUP) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recipe_group, parent, false);
            return new GroupViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recipe_item, parent, false);
            return new RecipeViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        
        if (holder instanceof GroupViewHolder) {
            GroupViewHolder groupHolder = (GroupViewHolder) holder;
            Map.Entry<String, Integer> groupEntry = (Map.Entry<String, Integer>) item;
            groupHolder.bind(groupEntry.getKey(), groupEntry.getValue());
        } else if (holder instanceof RecipeViewHolder) {
            RecipeViewHolder recipeHolder = (RecipeViewHolder) holder;
            Recipe recipe = (Recipe) item;
            recipeHolder.bind(recipe);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof Map.Entry) ? TYPE_GROUP : TYPE_RECIPE;
    }

    public void setRecipes(List<Recipe> recipes) {
        groupedRecipes.clear();
        
        if (recipes != null) {
            for (Recipe recipe : recipes) {
                String group = recipe.getGroup();
                if (group == null || group.isEmpty()) {
                    group = "General";
                }
                
                if (!groupedRecipes.containsKey(group)) {
                    groupedRecipes.put(group, new ArrayList<>());
                }
                groupedRecipes.get(group).add(recipe);
            }
        }
        
        if (expandedGroup == null && !groupedRecipes.isEmpty()) {
            expandedGroup = groupedRecipes.keySet().iterator().next();
        }
        
        updateItemsList();
    }

    public void setRecipesAndGroups(List<Recipe> recipes, Set<String> allGroups) {
        groupedRecipes.clear();
        
        if (allGroups != null) {
            for (String group : allGroups) {
                if (!groupedRecipes.containsKey(group)) {
                    groupedRecipes.put(group, new ArrayList<>());
                }
            }
        }
        
        if (recipes != null) {
            for (Recipe recipe : recipes) {
                String group = recipe.getGroup();
                if (group == null || group.isEmpty()) {
                    group = "General";
                }
                
                if (!groupedRecipes.containsKey(group)) {
                    groupedRecipes.put(group, new ArrayList<>());
                }
                groupedRecipes.get(group).add(recipe);
            }
        }
        
        if (expandedGroup == null && !groupedRecipes.isEmpty()) {
            expandedGroup = groupedRecipes.keySet().iterator().next();
        }
        
        updateItemsList();
    }

    private void updateItemsList() {
        items.clear();
        
        for (Map.Entry<String, List<Recipe>> entry : groupedRecipes.entrySet()) {
            items.add(new HashMap.SimpleEntry<>(entry.getKey(), entry.getValue().size()));
            
            if (entry.getKey().equals(expandedGroup)) {
                items.addAll(entry.getValue());
            }
        }
        
        notifyDataSetChanged();
    }

    public void toggleGroupExpansion(String groupName) {
        if (groupName.equals(expandedGroup)) {
            expandedGroup = null;
        } else {
            expandedGroup = groupName;
        }
        
        updateItemsList();
    }

    public List<String> getGroups() {
        return new ArrayList<>(groupedRecipes.keySet());
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewGroupName;
        private TextView textViewRecipeCount;
        private View itemView;
        private ImageView imageViewExpandCollapse;
        private ImageView imageViewDeleteGroup;
        private View groupLayout;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            textViewGroupName = itemView.findViewById(R.id.textViewGroupName);
            textViewRecipeCount = itemView.findViewById(R.id.textViewRecipeCount);
            imageViewExpandCollapse = itemView.findViewById(R.id.imageViewExpandCollapse);
            imageViewDeleteGroup = itemView.findViewById(R.id.imageViewDeleteGroup);
            groupLayout = itemView.findViewById(R.id.groupLayout);
        }

        public void bind(String groupName, int recipeCount) {
            textViewGroupName.setText(groupName);
            textViewRecipeCount.setText(recipeCount + " recipes");
            
            boolean isExpanded = groupName.equals(expandedGroup);
            imageViewExpandCollapse.setImageResource(
                isExpanded ? android.R.drawable.arrow_up_float : android.R.drawable.arrow_down_float
            );
            
            groupLayout.setBackgroundResource(
                isExpanded ? R.drawable.custom_button : R.drawable.group_background
            );
            
            if ("General".equals(groupName)) {
                imageViewDeleteGroup.setVisibility(View.GONE);
            } else {
                imageViewDeleteGroup.setVisibility(View.VISIBLE);
                
                imageViewDeleteGroup.setOnClickListener(v -> {
                    if (groupListener != null) {
                        groupListener.onDeleteGroup(groupName);
                    }
                });
            }
            
            itemView.setOnClickListener(v -> {
                toggleGroupExpansion(groupName);
            });
        }
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewRecipeTitle;
        private TextView textViewIngredientCount;
        private Button buttonViewRecipe;
        private Button buttonEditRecipe;
        private Button buttonDeleteRecipe;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewRecipeTitle = itemView.findViewById(R.id.textViewRecipeTitle);
            textViewIngredientCount = itemView.findViewById(R.id.textViewIngredientCount);
            buttonViewRecipe = itemView.findViewById(R.id.buttonViewRecipe);
            buttonEditRecipe = itemView.findViewById(R.id.buttonEditRecipe);
            buttonDeleteRecipe = itemView.findViewById(R.id.buttonDeleteRecipe);
        }

        public void bind(final Recipe recipe) {
            textViewRecipeTitle.setText(recipe.getTitle());
            
            int ingredientCount = recipe.getIngredients() != null ? recipe.getIngredients().size() : 0;
            textViewIngredientCount.setText(ingredientCount + " ingredients");
            
            buttonViewRecipe.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewRecipe(recipe);
                }
            });
            
            buttonEditRecipe.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditRecipe(recipe);
                }
            });
            
            buttonDeleteRecipe.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteRecipe(recipe);
                }
            });
        }
    }
} 