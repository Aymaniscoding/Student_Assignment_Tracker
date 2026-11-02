package com.example.r005_student_assignment_tracker;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class CampusFlowApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApiKey("AIzaSyCampusFlowAppKeyForTesting123456")
                        .setApplicationId("1:1234567890:android:abcdef1234567890")
                        .setProjectId("campusflow-app")
                        .build();
                FirebaseApp.initializeApp(this, options);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
