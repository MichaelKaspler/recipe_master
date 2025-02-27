package com.example.recipe_misha.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.recipe_misha.R;
import com.example.recipe_misha.adapter.GroupedRecipeAdapter;
import com.example.recipe_misha.data.Recipe;
import com.example.recipe_misha.viewmodel.RecipeViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Set;

public class SavedRecipesFragment extends Fragment implements 
        GroupedRecipeAdapter.OnRecipeActionListener,
        GroupedRecipeAdapter.OnGroupActionListener {

    private static final String TAG = "SavedRecipesFragment";
    private RecyclerView recyclerViewRecipes;
    private TextView textViewEmptyList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAddGroup;
    private RecipeViewModel recipeViewModel;
    private GroupedRecipeAdapter groupedRecipeAdapter;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_recipes, container, false);

        recipeViewModel = new ViewModelProvider(requireActivity()).get(RecipeViewModel.class);

        recyclerViewRecipes = view.findViewById(R.id.recyclerViewRecipes);
        textViewEmptyList = view.findViewById(R.id.textViewEmptyList);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        fabAddGroup = view.findViewById(R.id.fabAddGroup);

        setupRecyclerView();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            recipeViewModel.refreshRecipes();
        });

        fabAddGroup.setOnClickListener(v -> {
            showAddGroupDialog();
        });

        observeRecipes();

        recipeViewModel.getRecipeGroups().observe(getViewLifecycleOwner(), groups -> {
            if (groups != null && groups.isEmpty()) {
                recipeViewModel.addGroup("General");
            }
        });

        recipeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        recipeViewModel.getSuccessMessage().observe(getViewLifecycleOwner(), successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
            }
        });
        
        return view;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }
    
    private void setupRecyclerView() {
        groupedRecipeAdapter = new GroupedRecipeAdapter(this);
        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRecipes.setHasFixedSize(true);
        recyclerViewRecipes.setAdapter(groupedRecipeAdapter);
    }
    
    private void observeRecipes() {
        recipeViewModel.getAllRecipes().observe(getViewLifecycleOwner(), recipes -> {
            Set<String> groups = recipeViewModel.getRecipeGroups().getValue();
            groupedRecipeAdapter.setRecipesAndGroups(recipes, groups);

            if (recipes == null || recipes.isEmpty()) {
                recyclerViewRecipes.setVisibility(View.GONE);
                textViewEmptyList.setVisibility(View.VISIBLE);
            } else {
                recyclerViewRecipes.setVisibility(View.VISIBLE);
                textViewEmptyList.setVisibility(View.GONE);
            }

            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
                Log.d(TAG, "Stopped refresh animation");
            }
        });

        recipeViewModel.getRecipeGroups().observe(getViewLifecycleOwner(), groups -> {
            if (groups != null && groups.isEmpty()) {
                recipeViewModel.addGroup("General");
            }
            List<Recipe> currentRecipes = recipeViewModel.getAllRecipes().getValue();
            if (currentRecipes != null && groups != null) {
                groupedRecipeAdapter.setRecipesAndGroups(currentRecipes, groups);
            }
        });
    }
    
    private void showAddGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_group, null);
        builder.setView(dialogView);
        
        TextInputEditText editTextGroupName = dialogView.findViewById(R.id.editTextGroupName);
        
        AlertDialog dialog = builder.create();

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add", (dialogInterface, i) -> {
            String groupName = editTextGroupName.getText().toString().trim();
            if (!groupName.isEmpty()) {
                recipeViewModel.addGroup(groupName);
                Toast.makeText(getContext(), "Group '" + groupName + "' added", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                if (groupedRecipeAdapter != null) {
                    groupedRecipeAdapter.toggleGroupExpansion(groupName);
                }
            } else {
                Toast.makeText(getContext(), "Group name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialogInterface, i) -> {
            Log.d(TAG, "Add group dialog cancelled");
            dialog.dismiss();
        });
        
        dialog.show();
        Log.d(TAG, "Showing add group dialog");
    }
    
    @Override
    public void onDeleteGroup(String groupName) {
        Log.d(TAG, "Delete group requested: " + groupName);
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Group")
                .setMessage("Are you sure you want to delete the group '" + groupName + "'?\n\n" +
                           "All recipes in this group will be moved to the 'General' group.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    recipeViewModel.deleteGroup(groupName);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    @Override
    public void onViewRecipe(Recipe recipe) {
        try {
            if (recipe == null) {
                Toast.makeText(getContext(), "Error: Recipe data is missing", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (recipe.getTitle() == null || recipe.getTitle().isEmpty()) {
                Toast.makeText(getContext(), "Error: Recipe title is missing", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (navController == null) {
                Toast.makeText(getContext(), "Navigation error: Please try again", Toast.LENGTH_SHORT).show();
                return;
            }
            Bundle args = new Bundle();
            args.putString("recipe_title", recipe.getTitle());
            navController.navigate(R.id.action_savedRecipesFragment_to_recipeDetailFragment, args);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error viewing recipe: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onEditRecipe(Recipe recipe) {
        Bundle args = new Bundle();
        args.putString("recipe_title", recipe.getTitle());
        navController.navigate(R.id.action_savedRecipesFragment_to_editRecipeFragment, args);
    }
    
    @Override
    public void onDeleteRecipe(Recipe recipe) {
        deleteRecipe(recipe);
    }
    
    private void deleteRecipe(Recipe recipe) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Recipe")
                .setMessage("Are you sure you want to delete this recipe?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    recipeViewModel.delete(recipe);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        recipeViewModel.refreshRecipes();
    }
} 