package com.integrals.inlens.Interface;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public interface FirebaseRead {
    void onSuccess(DataSnapshot snapshot);
    void onStart();
    void onFailure(DatabaseError databaseError);
}
