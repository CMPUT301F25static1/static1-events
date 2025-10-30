package com.static1.fishylottery;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.static1.fishylottery.services.AuthManager;

public class MainApplication extends Application {
    private AuthManager authManager;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

        authManager = new AuthManager();
    }

    public AuthManager getAuthManager() {
        return authManager;
    }
}
