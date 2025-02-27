package com.example.recipe_misha.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipe_misha.data.ApiRecipe;
import com.example.recipe_misha.adapter.IngredientAdapter;
import com.example.recipe_misha.R;
import com.example.recipe_misha.data.FirebaseService;
import com.example.recipe_misha.data.Recipe;
import com.example.recipe_misha.viewmodel.RecipeViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.List;
import java.util.Set;

public class ApiRecipeDetailFragment extends Fragment {

    private static final String ARG_RECIPE_JSON = "recipe_json";
    private static final String TAG = "ApiRecipeDetailFragment";

    private ApiRecipe apiRecipe;
    private TextView textViewRecipeTitle;
    private TextView textViewServings;
    private RecyclerView recyclerViewIngredients;
    private TextView textViewInstructions;
    private Button buttonSaveRecipe;
    private Button buttonBack;
    private IngredientAdapter ingredientAdapter;
    private FirebaseService firebaseService;
    private RecipeViewModel recipeViewModel;
    private String selectedGroup = "General"; // Default group

    public static ApiRecipeDetailFragment newInstance(ApiRecipe recipe) {
        ApiRecipeDetailFragment fragment = new ApiRecipeDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RECIPE_JSON, new Gson().toJson(recipe));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String recipeJson = getArguments().getString(ARG_RECIPE_JSON);
            if (recipeJson != null) {
                apiRecipe = new Gson().fromJson(recipeJson, ApiRecipe.class);
            }
        }
        firebaseService = FirebaseService.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_api_recipe_detail, container, false);

        textViewRecipeTitle = view.findViewById(R.id.textViewRecipeTitle);
        textViewServings = view.findViewById(R.id.textViewServings);
        recyclerViewIngredients = view.findViewById(R.id.recyclerViewIngredients);
        textViewInstructions = view.findViewById(R.id.textViewInstructions);
        buttonSaveRecipe = view.findViewById(R.id.buttonSaveRecipe);
        buttonBack = view.findViewById(R.id.buttonBack);

        setupRecyclerView();
        populateRecipeDetails();
        setupButtons();

        // Initialize ViewModel
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
        
        // Observe error messages
        recipeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                buttonSaveRecipe.setEnabled(true);
                buttonSaveRecipe.setText("Save Recipe");
            }
        });
        
        // Observe success messages
        recipeViewModel.getSuccessMessage().observe(getViewLifecycleOwner(), successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                buttonSaveRecipe.setEnabled(false);
                buttonSaveRecipe.setText("Recipe Saved");
                apiRecipe.setSaved(true);
            }
        });

        return view;
    }

    private void setupRecyclerView() {
        ingredientAdapter = new IngredientAdapter();
        recyclerViewIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewIngredients.setAdapter(ingredientAdapter);
    }

    private void populateRecipeDetails() {
        if (apiRecipe != null) {
            textViewRecipeTitle.setText(apiRecipe.getTitle());
            
            if (apiRecipe.getServings() != null && !apiRecipe.getServings().isEmpty()) {
                textViewServings.setText(apiRecipe.getServings());
            } else {
                textViewServings.setText("Not specified");
            }
            
            List<String> ingredientsList = apiRecipe.getIngredientsList();
            ingredientAdapter.setIngredients(ingredientsList);
            
            textViewInstructions.setText(apiRecipe.getInstructions());
        }
    }

    private void setupButtons() {
        buttonSaveRecipe.setOnClickListener(v -> {
            if (validateRecipe()) {
                showGroupSelectionDialog();
            }
        });
        
        buttonBack.setOnClickListener(v -> {
            // Navigate back to the search fragment
            if (getActivity() != null) {
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
    }
    
    private boolean validateRecipe() {
        if (apiRecipe == null) {
            Toast.makeText(getContext(), "Cannot save recipe: Recipe data is missing", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (apiRecipe.getTitle() == null || apiRecipe.getTitle().isEmpty()) {
            Toast.makeText(getContext(), "Cannot save recipe: Title is empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        List<String> ingredients = apiRecipe.getIngredientsList();
        if (ingredients == null || ingredients.isEmpty()) {
            Toast.makeText(getContext(), "Cannot save recipe: No ingredients found", Toast.LENGTH_SHORT).show();
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
        
        // Get available groups
        Set<String> groups = recipeViewModel.getRecipeGroups().getValue();
        if (groups == null || groups.isEmpty()) {
            // Add default group if none exist
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
        if (apiRecipe != null) {
            try {
                buttonSaveRecipe.setEnabled(false);
                buttonSaveRecipe.setText("Saving...");
                
                Recipe recipe = apiRecipe.toRecipe();
                recipe.setGroup(selectedGroup);
                recipeViewModel.insert(recipe);
            } catch (Exception e) {
                String errorMessage = "Error preparing recipe: " + (e.getMessage() != null ? e.getMessage() : "Unknown error");
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();

                buttonSaveRecipe.setEnabled(true);
                buttonSaveRecipe.setText("Save Recipe");
            }
        }
    }
} 