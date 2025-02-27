package com.example.recipe_misha.fragments;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.recipe_misha.R;
import com.example.recipe_misha.data.FirebaseService;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements MainFragment.ButtonClickListener {
    private static final String TAG = "MainActivity";
    private NavController navController;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainer);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        FirebaseService.getInstance(getApplicationContext());
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void logout() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build();
        
        navController.navigate(R.id.loginFragment, null, navOptions);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        if (navController != null) {
            return navController.navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
    
    @Override
    public void onButtonClicked(int buttonId) {
        if (navController == null) {
            return;
        }
        switch (buttonId) {
            case 1:
                navController.navigate(R.id.action_mainFragment_to_createRecipeFragment);
                break;
            case 2:
                navController.navigate(R.id.action_mainFragment_to_browseRecipesFragment);
                break;
            case 3:
                navController.navigate(R.id.action_mainFragment_to_savedRecipesFragment);
                break;
        }
    }
}