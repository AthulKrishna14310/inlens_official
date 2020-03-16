package com.integrals.inlens.Helper;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.integrals.inlens.Interface.FirebaseRead;

public class ReadFirebaseData {

    public ReadFirebaseData() {
    }

    public ValueEventListener readData(Query ref, final FirebaseRead listener) {
        listener.onStart();
        ValueEventListener eventListener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailure(databaseError);
            }
        });

        return eventListener;
    }
}
