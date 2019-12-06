package com.integrals.inlens.Helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/*
*
*
*
* DO NOT DELETE THIS CLASS
* ELSON JOSE
*
*
*
*
* */

public class PreOperationCheck
{
    DatabaseReference databaseReference;
    String communityId;
    String status = "F";

    public PreOperationCheck()
    {

    }

    public PreOperationCheck(DatabaseReference databaseReference, String communityId) {

        this.databaseReference = databaseReference;
        this.communityId = communityId;
    }

    public void hideSoftKeyboard(Context context, View view )
    {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isAcceptingText())
        {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);

        }
    }

    public boolean getAlbumStatus()
    {

        databaseReference.child("Communities").child(communityId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("ActiveIndex"))
                {
                    status = dataSnapshot.child("ActiveIndex").getValue().toString();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        return status.equals("T");

    }

    public boolean checkInternetConnectivity(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }

}
