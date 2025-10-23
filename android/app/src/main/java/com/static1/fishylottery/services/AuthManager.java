package com.static1.fishylottery.services;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.auth.User;

public class AuthManager {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public void ensureSignedIn(Runnable onReady) {
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously()
                    .addOnSuccessListener(r -> onReady.run())
                    .addOnFailureListener(f -> Log.e("Auth", "Sign-in failed", f));
        } else {
            onReady.run();
        }
    }

    public String getUserId() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            return user.getUid();
        } else {
            return null;
        }
    }
}
