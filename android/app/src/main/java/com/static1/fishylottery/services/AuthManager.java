package com.static1.fishylottery.services;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.auth.User;

/**
 * Responsible for managing the Firebase Auth and user uid.
 * Ensures that user is signed in anonymously.
 */
public class AuthManager {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    /**
     * Check to make sure that the user is signed in with anonymous authentication.
     * @param onReady Indicates that a part of the app is ready to run upon auth completion.
     */
    public void ensureSignedIn(Runnable onReady) {
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously()
                    .addOnSuccessListener(r -> onReady.run())
                    .addOnFailureListener(f -> Log.e("Auth", "Sign-in failed", f));
        } else {
            onReady.run();
        }
    }

    /**
     * Get the UID of the Firebase Auth user.
     *
     * @return The uid.
     */
    public String getUserId() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            return user.getUid();
        } else {
            return null;
        }
    }
}
