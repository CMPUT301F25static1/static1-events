package com.static1.fishylottery.services;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.auth.User;

/**
 * Responsible for managing the Firebase Auth and user uid.
 * Ensures that user is signed in anonymously.
 */
public class AuthManager {
    private static AuthManager instance;
    private final FirebaseAuth auth;

    public AuthManager(FirebaseAuth auth) {
        this.auth = auth;
    }

    public static synchronized AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager(FirebaseAuth.getInstance());
        }
        return instance;
    }

    public static synchronized void setInstanceForTesting(AuthManager mockInstance) {
        instance = mockInstance;
    }

    /**
     * Signs in the user with anonymous authentication
     */
    public Task<AuthResult> signInAnonymously() {
        return auth.signInAnonymously();
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

    public Task<Void> deleteUser() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return Tasks.forException(new IllegalStateException("No user signed in"));
        }

        return user.delete();
    }

    public boolean isSignedIn() {
        return auth.getCurrentUser() != null;
    }
}
