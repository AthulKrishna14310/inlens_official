package com.integrals.inlens.Interface;

import com.google.firebase.database.DataSnapshot;

public interface FirebaseRead {
    void onSuccess(DataSnapshot snapshot);
    void onStart();
    void onFailure();
}
