package com.example.recipe_misha.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipe_misha.R;
import com.example.recipe_misha.adapter.IngredientAdapterAPI;
import com.example.recipe_misha.data.Ingredient;
import com.example.recipe_misha.data.Recipe;
import com.example.recipe_misha.viewmodel.RecipeViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CreateRecipeFragment extends Fragment implements IngredientAdapterAPI.OnIngredientRemovedListener {
    
    private static final String TAG = "CreateRecipeFragment";
    
    private Button buttonAddIngredient;
    private Button buttonSaveRecipe;
    private TextInputEditText editTextRecipeTitle;
    private TextInputEditText editTextServings;
    private TextInputEditText editTextInstructions;
    private RecyclerView recyclerViewIngredients;
    
    private List<Ingredient> ingredients;
    private IngredientAdapterAPI ingredientAdapter;
    private RecipeViewModel recipeViewModel;
    private NavController navController;
    private String selectedGroup = "General"; // Default group

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_recipe, container, false);

        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        buttonAddIngredient = view.findViewById(R.id.buttonAddIngredient);
        buttonSaveRecipe = view.findViewById(R.id.buttonSaveRecipe);
        editTextRecipeTitle = view.findViewById(R.id.editTextRecipeTitle);
        editTextServings = view.findViewById(R.id.editTextServings);
        editTextInstructions = view.findViewById(R.id.editTextInstructions);
        recyclerViewIngredients = view.findViewById(R.id.recyclerViewIngredients);

        ingredients = new ArrayList<>();
        ingredientAdapter = new IngredientAdapterAPI(ingredients, this);
        recyclerViewIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewIngredients.setAdapter(ingredientAdapter);

        buttonAddIngredient.setOnClickListener(v -> {
            ingredients.add(new Ingredient("", ""));
            ingredientAdapter.notifyItemInserted(ingredients.size() - 1);
        });
        
        buttonSaveRecipe.setOnClickListener(v -> {
            if (validateRecipe()) {
                showGroupSelectionDialog();
            }
        });

        recipeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                buttonSaveRecipe.setEnabled(true);
                buttonSaveRecipe.setText("Save Recipe");
            }
        });

        recipeViewModel.getSuccessMessage().observe(getViewLifecycleOwner(), successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();

                buttonSaveRecipe.setEnabled(true);
                buttonSaveRecipe.setText("Save Recipe");

                editTextRecipeTitle.setText("");
                editTextServings.setText("");
                editTextInstructions.setText("");
                ingredients.clear();
                ingredientAdapter.notifyDataSetChanged();
            }
        });
        return view;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }
    
    private boolean validateRecipe() {
        String title = editTextRecipeTitle.getText().toString().trim();
        String instructions = editTextInstructions.getText().toString().trim();
        if (title.isEmpty()) {
            editTextRecipeTitle.setError("Title is required");
            editTextRecipeTitle.requestFocus();
            return false;
        }
        
        if (ingredients.isEmpty()) {
            Toast.makeText(getContext(), "Please add at least one ingredient", Toast.LENGTH_SHORT).show();
            return false;
        }
        boolean hasEmptyIngredient = false;
        for (Ingredient ingredient : ingredients) {
            if (ingredient.getName().trim().isEmpty()) {
                hasEmptyIngredient = true;
                break;
            }
        }
        
        if (hasEmptyIngredient) {
            Toast.makeText(getContext(), "All ingredients must have a name", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (instructions.isEmpty()) {
            editTextInstructions.setError("Instructions are required");
            editTextInstructions.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void showGroupSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_group, null);
        builder.setView(dialogView);
        
        RadioGroup radioGroupRecipeGroups = dialogView.findViewById(R.id.radioGroupRecipeGroups);
        Button buttonAddNewGroup = dialogView.findViewById(R.id.buttonAddNewGroup);
        Button buttonSelectGroup = dialogView.findViewById(R.id.buttonSelectGroup);

        Set<String> groups = recipeViewModel.getRecipeGroups().getValue();
        if (groups == null || groups.isEmpty()) {
            radioGroupRecipeGroups.removeAllViews();
            RadioButton radioButtonGeneral = new RadioButton(getContext());
            radioButtonGeneral.setText("General");
            radioButtonGeneral.setId(View.generateViewId());
            radioButtonGeneral.setChecked(true);
            radioGroupRecipeGroups.addView(radioButtonGeneral);
        } else {
            radioGroupRecipeGroups.removeAllViews();

            boolean foundSelected = false;
            
            for (String group : groups) {
                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setText(group);
                radioButton.setId(View.generateViewId());

                boolean shouldCheck = group.equals(selectedGroup) || 
                                     (group.equals("General") && (selectedGroup == null || selectedGroup.isEmpty()));
                
                if (shouldCheck) {
                    radioButton.setChecked(true);
                    foundSelected = true;
                }
                
                radioGroupRecipeGroups.addView(radioButton);
            }
            if (!foundSelected && radioGroupRecipeGroups.getChildCount() > 0) {
                ((RadioButton) radioGroupRecipeGroups.getChildAt(0)).setChecked(true);
            }
        }
        
        AlertDialog dialog = builder.create();

        buttonAddNewGroup.setOnClickListener(v -> {
            dialog.dismiss();
            showAddGroupDialog();
        });
        
        buttonSelectGroup.setOnClickListener(v -> {
            int childCount = radioGroupRecipeGroups.getChildCount();
            String selectedGroupName = null;
            
            for (int i = 0; i < childCount; i++) {
                RadioButton radioButton = (RadioButton) radioGroupRecipeGroups.getChildAt(i);
                if (radioButton.isChecked()) {
                    selectedGroupName = radioButton.getText().toString();
                    break;
                }
            }
            
            if (selectedGroupName != null) {
                selectedGroup = selectedGroupName;
                saveRecipe();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Please select a group", Toast.LENGTH_SHORT).show();
            }
        });
        
        dialog.show();
    }
    
    private void showAddGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_group, null);
        builder.setView(dialogView);
        
        TextInputEditText editTextGroupName = dialogView.findViewById(R.id.editTextGroupName);
        
        AlertDialog dialog = builder.create();
        
        // Add buttons programmatically
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add", (dialogInterface, i) -> {
            String groupName = editTextGroupName.getText().toString().trim();
            if (!groupName.isEmpty()) {
                recipeViewModel.addGroup(groupName);
                selectedGroup = groupName;
                saveRecipe();
            } else {
                Toast.makeText(getContext(), "Group name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialogInterface, i) -> {
            dialog.dismiss();
            showGroupSelectionDialog();
        });
        
        dialog.show();
    }
    
    private void saveRecipe() {
        String title = editTextRecipeTitle.getText().toString().trim();
        String servings = editTextServings.getText().toString().trim();
        String instructions = editTextInstructions.getText().toString().trim();

        Recipe recipe = new Recipe();
        recipe.setTitle(title);
        recipe.setServings(servings);
        recipe.setInstructions(instructions);
        recipe.setIngredients(new ArrayList<>(ingredients));
        recipe.setGroup(selectedGroup);

        buttonSaveRecipe.setEnabled(false);
        buttonSaveRecipe.setText("Saving...");
        
        recipeViewModel.insert(recipe);
    }
    
    @Override
    public void onIngredientRemoved(int position) {
        if (position >= 0 && position < ingredients.size()) {
            ingredients.remove(position);
            ingredientAdapter.notifyItemRemoved(position);
            ingredientAdapter.notifyItemRangeChanged(position, ingredients.size());
        }
    }
} 