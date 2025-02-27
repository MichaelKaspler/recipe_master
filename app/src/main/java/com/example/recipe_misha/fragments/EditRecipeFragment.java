package com.example.recipe_misha.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
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
import com.example.recipe_misha.data.FirebaseService;
import com.example.recipe_misha.data.Ingredient;
import com.example.recipe_misha.data.Recipe;
import com.example.recipe_misha.viewmodel.RecipeViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EditRecipeFragment extends Fragment implements IngredientAdapterAPI.OnIngredientRemovedListener {

    private static final String ARG_RECIPE_TITLE = "recipe_title";
    private static final String COLLECTION_RECIPES = "recipes";
    
    private String recipeTitle;
    private Recipe currentRecipe;
    private String documentId;
    private NavController navController;
    private String selectedGroup = "General";
    
    private Button buttonSaveChanges;
    private Button buttonCancelEdit;
    private Button buttonAddEditIngredient;
    private TextInputEditText editTextEditRecipeTitle;
    private TextInputEditText editTextEditInstructions;
    private TextInputEditText editTextEditServings;
    private RecyclerView recyclerViewEditIngredients;
    private ProgressBar progressBarEdit;
    
    private List<Ingredient> ingredients;
    private IngredientAdapterAPI ingredientAdapter;
    private FirebaseFirestore db;
    private FirebaseService firebaseService;
    private RecipeViewModel recipeViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        firebaseService = FirebaseService.getInstance(requireContext());
        if (getArguments() != null) {
            recipeTitle = getArguments().getString(ARG_RECIPE_TITLE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_recipe, container, false);
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        buttonSaveChanges = view.findViewById(R.id.buttonSaveChanges);
        buttonCancelEdit = view.findViewById(R.id.buttonCancelEdit);
        buttonAddEditIngredient = view.findViewById(R.id.buttonAddEditIngredient);
        editTextEditRecipeTitle = view.findViewById(R.id.editTextRecipeName);
        editTextEditInstructions = view.findViewById(R.id.editTextInstructions);
        editTextEditServings = view.findViewById(R.id.editTextServings);
        recyclerViewEditIngredients = view.findViewById(R.id.recyclerViewEditIngredients);
        progressBarEdit = view.findViewById(R.id.progressBarEditRecipe);

        ingredients = new ArrayList<>();
        ingredientAdapter = new IngredientAdapterAPI(ingredients, this);
        recyclerViewEditIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewEditIngredients.setAdapter(ingredientAdapter);

        buttonCancelEdit.setOnClickListener(v -> navController.navigateUp());
        
        buttonAddEditIngredient.setOnClickListener(v -> {
            ingredients.add(new Ingredient("", ""));
            ingredientAdapter.notifyItemInserted(ingredients.size() - 1);
            recyclerViewEditIngredients.smoothScrollToPosition(ingredients.size() - 1);
        });
        
        buttonSaveChanges.setOnClickListener(v -> {
            if (validateRecipe()) {
                showGroupSelectionDialog();
            }
        });

        recipeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                buttonSaveChanges.setEnabled(true);
                buttonSaveChanges.setText("Save Changes");
            }
        });

        recipeViewModel.getSuccessMessage().observe(getViewLifecycleOwner(), successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                buttonSaveChanges.setEnabled(true);
                buttonSaveChanges.setText("Save Changes");
                navController.navigateUp();
            }
        });
        
        return view;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        loadRecipeDataFromFirestore();
    }
    
    private void loadRecipeDataFromFirestore() {
        if (recipeTitle == null || recipeTitle.isEmpty()) {
            Toast.makeText(getContext(), "Error: Recipe title is missing", Toast.LENGTH_SHORT).show();
            navController.navigateUp();
            return;
        }
        
        showProgress(true);
        db.collection(COLLECTION_RECIPES)
            .whereEqualTo("title", recipeTitle)
            .get()
            .addOnCompleteListener(task -> {
                showProgress(false);
                
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        documentId = document.getId();
                        
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
                        
                        // Store the group for later use
                        selectedGroup = recipe.getGroup();
                        
                        List<Map<String, String>> ingredientMaps = 
                            (List<Map<String, String>>) document.get("ingredients");
                        List<Ingredient> recipeIngredients = new ArrayList<>();
                        
                        if (ingredientMaps != null) {
                            for (Map<String, String> ingredientMap : ingredientMaps) {
                                recipeIngredients.add(new Ingredient(
                                    ingredientMap.get("name") != null ? ingredientMap.get("name") : "",
                                    ingredientMap.get("amount") != null ? ingredientMap.get("amount") : ""
                                ));
                            }
                        }
                        
                        recipe.setIngredients(recipeIngredients);
                        displayRecipe(recipe);
                    } else {
                        Toast.makeText(getContext(), "Recipe not found in database", Toast.LENGTH_SHORT).show();
                        navController.navigateUp();
                    }
                } else {
                    Toast.makeText(getContext(), "Error loading recipe: " + task.getException().getMessage(), 
                        Toast.LENGTH_LONG).show();
                    navController.navigateUp();
                }
            });
    }
    
    private void displayRecipe(Recipe recipe) {
        currentRecipe = recipe;
        editTextEditRecipeTitle.setText(recipe.getTitle() != null ? recipe.getTitle() : "");
        editTextEditServings.setText(recipe.getServings() != null ? recipe.getServings() : "");
        editTextEditInstructions.setText(recipe.getInstructions() != null ? recipe.getInstructions() : "");
        
        ingredients.clear();
        if (recipe.getIngredients() != null) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredients.add(new Ingredient(
                    ingredient.getName() != null ? ingredient.getName() : "",
                    ingredient.getAmount() != null ? ingredient.getAmount() : ""
                ));
            }
        }
        ingredientAdapter.notifyDataSetChanged();
    }
    
    private boolean validateRecipe() {
        String title = editTextEditRecipeTitle.getText().toString().trim();
        String instructions = editTextEditInstructions.getText().toString().trim();
        
        if (title.isEmpty()) {
            editTextEditRecipeTitle.setError("Title is required");
            editTextEditRecipeTitle.requestFocus();
            return false;
        }
        
        if (ingredients.isEmpty()) {
            Toast.makeText(getContext(), "Please add at least one ingredient", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        for (Ingredient ingredient : ingredients) {
            if (ingredient.getName().trim().isEmpty()) {
                Toast.makeText(getContext(), "All ingredients must have a name", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        
        if (instructions.isEmpty()) {
            editTextEditInstructions.setError("Instructions are required");
            editTextEditInstructions.requestFocus();
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
                saveChangesToFirestore(selectedGroup);
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

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add", (dialogInterface, i) -> {
            String groupName = editTextGroupName.getText().toString().trim();
            if (!groupName.isEmpty()) {
                recipeViewModel.addGroup(groupName);
                selectedGroup = groupName;
                saveChangesToFirestore(selectedGroup);
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
    
    private void saveChangesToFirestore(String selectedGroup) {
        showProgress(true);
        
        String newTitle = editTextEditRecipeTitle.getText().toString().trim();
        String servings = editTextEditServings.getText().toString().trim();
        String instructions = editTextEditInstructions.getText().toString().trim();
        
        Map<String, Object> recipeMap = new HashMap<>();
        recipeMap.put("title", newTitle);
        recipeMap.put("servings", servings);
        recipeMap.put("instructions", instructions);
        recipeMap.put("source", currentRecipe != null ? currentRecipe.getSource() : "USER");
        recipeMap.put("group", selectedGroup);
        
        List<Map<String, String>> ingredientMaps = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            Map<String, String> ingredientMap = new HashMap<>();
            ingredientMap.put("name", ingredient.getName());
            ingredientMap.put("amount", ingredient.getAmount());
            ingredientMaps.add(ingredientMap);
        }
        recipeMap.put("ingredients", ingredientMaps);
        
        if (documentId != null) {
            updateExistingRecipe(documentId, recipeMap);
        } else if (recipeTitle != null && !recipeTitle.isEmpty()) {
            if (!recipeTitle.equals(newTitle)) {
                updateRecipeWithTitleChange(recipeMap);
            } else {
                findAndUpdateRecipe(recipeMap);
            }
        } else {
            createNewRecipe(recipeMap);
        }
    }
    
    private void updateExistingRecipe(String docId, Map<String, Object> recipeMap) {
        db.collection(COLLECTION_RECIPES).document(docId)
            .set(recipeMap)
            .addOnSuccessListener(aVoid -> {
                showProgress(false);
                Toast.makeText(getContext(), "Recipe updated successfully", Toast.LENGTH_SHORT).show();
                navController.navigateUp();
            })
            .addOnFailureListener(e -> {
                showProgress(false);
                Toast.makeText(getContext(), "Error updating recipe: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }
    
    private void findAndUpdateRecipe(Map<String, Object> recipeMap) {
        String title = (String) recipeMap.get("title");
        
        db.collection(COLLECTION_RECIPES)
            .whereEqualTo("title", title)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    updateExistingRecipe(queryDocumentSnapshots.getDocuments().get(0).getId(), recipeMap);
                } else {
                    createNewRecipe(recipeMap);
                }
            })
            .addOnFailureListener(e -> {
                showProgress(false);
                Toast.makeText(getContext(), "Error finding recipe: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }
    
    private void createNewRecipe(Map<String, Object> recipeMap) {
        db.collection(COLLECTION_RECIPES)
            .add(recipeMap)
            .addOnSuccessListener(documentReference -> {
                showProgress(false);
                Toast.makeText(getContext(), "Recipe created successfully", Toast.LENGTH_SHORT).show();
                navController.navigateUp();
            })
            .addOnFailureListener(e -> {
                showProgress(false);
                Toast.makeText(getContext(), "Error creating recipe: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }
    
    private void updateRecipeWithTitleChange(Map<String, Object> recipeMap) {
        db.collection(COLLECTION_RECIPES)
            .add(recipeMap)
            .addOnSuccessListener(documentReference -> {
                findAndDeleteOldRecipe(recipeTitle);
            })
            .addOnFailureListener(e -> {
                showProgress(false);
                Toast.makeText(getContext(), "Error updating recipe: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }
    
    private void deleteOldRecipe(String docId) {
        db.collection(COLLECTION_RECIPES).document(docId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                showProgress(false);
                Toast.makeText(getContext(), "Recipe updated successfully", Toast.LENGTH_SHORT).show();
                navController.navigateUp();
            })
            .addOnFailureListener(e -> {
                showProgress(false);
                Toast.makeText(getContext(), "Recipe updated but couldn't delete old version", Toast.LENGTH_LONG).show();
                navController.navigateUp();
            });
    }
    
    private void findAndDeleteOldRecipe(String oldTitle) {
        db.collection(COLLECTION_RECIPES)
            .whereEqualTo("title", oldTitle)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    deleteOldRecipe(queryDocumentSnapshots.getDocuments().get(0).getId());
                } else {
                    showProgress(false);
                    Toast.makeText(getContext(), "Recipe updated successfully", Toast.LENGTH_SHORT).show();
                    navController.navigateUp();
                }
            })
            .addOnFailureListener(e -> {
                showProgress(false);
                Toast.makeText(getContext(), "Recipe updated but couldn't delete old version", Toast.LENGTH_LONG).show();
                navController.navigateUp();
            });
    }
    
    private void showProgress(boolean show) {
        if (progressBarEdit != null) progressBarEdit.setVisibility(show ? View.VISIBLE : View.GONE);
        
        if (buttonSaveChanges != null) buttonSaveChanges.setEnabled(!show);
        if (buttonCancelEdit != null) buttonCancelEdit.setEnabled(!show);
        if (buttonAddEditIngredient != null) buttonAddEditIngredient.setEnabled(!show);
        if (editTextEditRecipeTitle != null) editTextEditRecipeTitle.setEnabled(!show);
        if (editTextEditInstructions != null) editTextEditInstructions.setEnabled(!show);
        if (editTextEditServings != null) editTextEditServings.setEnabled(!show);
        if (recyclerViewEditIngredients != null) recyclerViewEditIngredients.setEnabled(!show);
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