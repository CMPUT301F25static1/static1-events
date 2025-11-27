package com.static1.fishylottery.services;

import com.google.firebase.auth.FirebaseAuth;

public class FakeAuthManager extends AuthManager {
    private final String uid;

    public FakeAuthManager(String fakeUid) {
        super(FirebaseAuth.getInstance());
        this.uid = fakeUid;
    }

    @Override
    public String getUserId() {
        return uid;
    }
}
