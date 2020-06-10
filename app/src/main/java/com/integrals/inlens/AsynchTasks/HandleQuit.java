package com.integrals.inlens.AsynchTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import android.widget.Toast;

import androidx.work.WorkManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.FirebaseConstants;

import java.util.UUID;

public class HandleQuit extends AsyncTask<Void,Void,Void>
{

    private DatabaseReference linkRef,statusRef,currentUserRef;
    private String activeCommunityId;
    private boolean isTaskSuccessful;
    private Context context;
    SharedPreferences CurrentActiveCommunity;

    public HandleQuit(Context applicationContext, DatabaseReference currentUserRef, DatabaseReference linkRef, DatabaseReference statusRef, String currentActiveCommunityID) {

        context=applicationContext;
        this.currentUserRef= currentUserRef;
        this.linkRef=linkRef;
        this.statusRef=statusRef;
        activeCommunityId=currentActiveCommunityID;
        isTaskSuccessful=false;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        CurrentActiveCommunity = context.getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
    }

    @Override
    protected Void doInBackground(Void... voids) {

        linkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    if(snapshot.hasChild("id") && snapshot.child("id").equals(activeCommunityId))
                    {
                        linkRef.child(snapshot.getKey()).removeValue();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        statusRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    currentUserRef.child(FirebaseConstants.LIVECOMMUNITYID).removeValue();
                    String scanWorkId = CurrentActiveCommunity.getString("scanWorkerId", AppConstants.NOT_AVALABLE);
                    String albumEndWorkId = CurrentActiveCommunity.getString("albumendWorkerId", AppConstants.NOT_AVALABLE);
                    if (scanWorkId.equals(AppConstants.NOT_AVALABLE)) {
                        WorkManager.getInstance().cancelUniqueWork(AppConstants.PHOTO_SCAN_WORK);
                    } else {
                        WorkManager.getInstance().cancelWorkById(UUID.fromString(scanWorkId));
                    }
                    if (albumEndWorkId.equals(AppConstants.NOT_AVALABLE)) {
                        WorkManager.getInstance().cancelAllWork();
                    } else {
                        WorkManager.getInstance().cancelWorkById(UUID.fromString(albumEndWorkId));
                    }
                    isTaskSuccessful=true;

                }
            }
        });
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if(isTaskSuccessful)
        {

            Toast.makeText(context, "Successfully left the album.", Toast.LENGTH_SHORT).show();

        }
        else
        {
            Toast.makeText(context, "Album quit unsuccessful.", Toast.LENGTH_SHORT).show();

        }

    }
}