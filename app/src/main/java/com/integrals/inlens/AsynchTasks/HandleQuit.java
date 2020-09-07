package com.integrals.inlens.AsynchTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import android.widget.Toast;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.integrals.inlens.Activities.InlensGalleryActivity;
import com.integrals.inlens.Database.UploadQueueDB;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.FirebaseConstants;
import com.integrals.inlens.WorkManager.UploadWorker;

import java.util.UUID;

public class HandleQuit extends AsyncTask<Void,Void,Void>
{

    private DatabaseReference statusRef,currentUserRef;
    private String activeCommunityId;
    private Context context;
    SharedPreferences CurrentActiveCommunity;
    UploadQueueDB uploadQueueDB;

    public HandleQuit(Context applicationContext, DatabaseReference currentUserRef,DatabaseReference statusRef, String currentActiveCommunityID) {

        context=applicationContext;
        this.currentUserRef= currentUserRef;
        this.statusRef=statusRef;
        activeCommunityId=currentActiveCommunityID;
        uploadQueueDB = new UploadQueueDB(applicationContext);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        CurrentActiveCommunity = context.getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
    }

    @Override
    protected Void doInBackground(Void... voids) {

        statusRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    currentUserRef.child(FirebaseConstants.LIVECOMMUNITYID).setValue(AppConstants.NOT_AVALABLE);

                    Cursor cursor = uploadQueueDB.getQueuedData();
                    if(cursor.getCount()==0)
                    {
                        SharedPreferences.Editor ceditor = CurrentActiveCommunity.edit();
                        ceditor.remove("id");
                        ceditor.remove("time");
                        ceditor.remove("stopAt");
                        ceditor.remove("startAt");
                        ceditor.remove("notiCount");
                        ceditor.commit();
                        String scanWorkId = CurrentActiveCommunity.getString("scanWorkerId", AppConstants.NOT_AVALABLE);
                        String albumEndWorkId = CurrentActiveCommunity.getString("albumendWorkerId", AppConstants.NOT_AVALABLE);
                        if (scanWorkId.equals(AppConstants.NOT_AVALABLE)) {
                            WorkManager.getInstance(context).cancelUniqueWork(AppConstants.PHOTO_SCAN_WORK);
                        } else {
                            WorkManager.getInstance(context).cancelWorkById(UUID.fromString(scanWorkId));
                        }
                        if (albumEndWorkId.equals(AppConstants.NOT_AVALABLE)) {
                            WorkManager.getInstance(context).cancelAllWork();
                        } else {
                            WorkManager.getInstance(context).cancelWorkById(UUID.fromString(albumEndWorkId));
                        }
                    }
                    else
                    {
                        //  todo upload the remaining images and den quit from the album.
                        Constraints quitWorkConstraint = new Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build();
                        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(UploadWorker.class)
                                .addTag("uploadWorker")
                                .setConstraints(quitWorkConstraint)
                                .build();
                        WorkManager.getInstance(context).cancelAllWorkByTag("uploadWorker");
                        WorkManager.getInstance(context).enqueueUniqueWork("uploadWorker", ExistingWorkPolicy.REPLACE,request);

                    }

                    cursor.close();

                }
            }
        });
        return null;
    }
}