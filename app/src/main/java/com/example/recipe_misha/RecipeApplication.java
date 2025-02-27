package com.example.recipe_misha;

import android.app.Application;
import com.example.recipe_misha.data.FirebaseService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class RecipeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseFirestore.setLoggingEnabled(true);

        initializeFirebase();

        FirebaseService.getInstance(getApplicationContext());
    }

    private void initializeFirebase() {
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
            FirebaseFirestore.getInstance().setFirestoreSettings(settings);
        }
    }
} 