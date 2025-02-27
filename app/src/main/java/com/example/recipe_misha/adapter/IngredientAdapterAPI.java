package com.example.recipe_misha.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipe_misha.R;
import com.example.recipe_misha.data.Ingredient;

import java.util.List;

public class IngredientAdapterAPI extends RecyclerView.Adapter<IngredientAdapterAPI.IngredientViewHolder> {
    private List<Ingredient> ingredients;
    private OnIngredientRemovedListener listener;
    public interface OnIngredientRemovedListener {
        void onIngredientRemoved(int position);
    }
    public IngredientAdapterAPI(List<Ingredient> ingredients, OnIngredientRemovedListener listener) {
        this.ingredients = ingredients;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ingredient_item, parent, false);
        return new IngredientViewHolder(itemView);
    }
    
    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient currentIngredient = ingredients.get(position);
        holder.editTextIngredient.setText(currentIngredient.getName());
        holder.editTextAmount.setText(currentIngredient.getAmount());
        
        holder.editTextIngredient.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    ingredients.get(adapterPosition).setName(s.toString());
                }
            }
        });
        
        holder.editTextAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    ingredients.get(adapterPosition).setAmount(s.toString());
                }
            }
        });
        
        holder.buttonRemove.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onIngredientRemoved(adapterPosition);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return ingredients.size();
    }
    
    public List<Ingredient> getIngredients() {
        return ingredients;
    }
    
    static class IngredientViewHolder extends RecyclerView.ViewHolder {
        private EditText editTextIngredient;
        private EditText editTextAmount;
        private ImageButton buttonRemove;
        
        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            editTextIngredient = itemView.findViewById(R.id.editTextIngredient);
            editTextAmount = itemView.findViewById(R.id.editTextAmount);
            buttonRemove = itemView.findViewById(R.id.buttonRemoveIngredient);
        }
    }
} 