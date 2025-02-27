package com.example.recipe_misha.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.recipe_misha.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainFragment extends Fragment {

    private ButtonClickListener buttonClickListener;
    private TextView textViewWelcomeUser;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public interface ButtonClickListener {
        void onButtonClicked(int buttonId);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ButtonClickListener) {
            buttonClickListener = (ButtonClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ButtonClickListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textViewWelcomeUser = view.findViewById(R.id.textViewWelcomeUser);
        Button btnCreateRecipe = view.findViewById(R.id.btnFragment1);
        Button btnBrowseRecipes = view.findViewById(R.id.btnFragment2);
        Button btnSavedRecipes = view.findViewById(R.id.btnFragment3);
        loadUserData();

        btnCreateRecipe.setOnClickListener(v -> {
            if (buttonClickListener != null) {
                buttonClickListener.onButtonClicked(1);
            }
        });

        btnBrowseRecipes.setOnClickListener(v -> {
            if (buttonClickListener != null) {
                buttonClickListener.onButtonClicked(2);
            }
        });

        btnSavedRecipes.setOnClickListener(v -> {
            if (buttonClickListener != null) {
                buttonClickListener.onButtonClicked(3);
            }
        });
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                textViewWelcomeUser.setText("Hello, " + displayName + "!");
            } else {
                db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            if (username != null && !username.isEmpty()) {
                                textViewWelcomeUser.setText("Hello, " + username + "!");
                            } else {
                                textViewWelcomeUser.setText("Hello, Chef!");
                            }
                        } else {
                            textViewWelcomeUser.setText("Hello, Chef!");
                        }
                    })
                    .addOnFailureListener(e -> {
                        textViewWelcomeUser.setText("Hello, Chef!");
                    });
            }
        } else {
            textViewWelcomeUser.setText("Hello, Guest!");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        buttonClickListener = null;
    }
} 