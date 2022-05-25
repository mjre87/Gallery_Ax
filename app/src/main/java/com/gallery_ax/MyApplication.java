package com.gallery_ax;

import android.app.Application;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // خاموش کردن حالت آفلاین Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        db.setFirestoreSettings(settings);
    }
}
