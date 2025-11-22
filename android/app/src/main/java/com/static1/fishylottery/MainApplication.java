package com.static1.fishylottery;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.static1.fishylottery.services.AuthManager;

/**
 * This is the main application that contains the initial creation of some resources that do
 * not depend on any views as they will load before the views. This includes initializing
 * the Firebase App before anything else runs.
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
