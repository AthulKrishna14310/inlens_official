package com.integrals.inlens.WorkManager;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.integrals.inlens.AsynchTasks.HandleQuit;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.FirebaseConstants;

import java.util.logging.Handler;

public class AlbumEndWorker extends Worker {

    HandleQuit handleQuit;
    private DatabaseReference linkRef,communityRef,currentUserRef;
    private String activeCommunityId;
    Context context;

    public AlbumEndWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        this.context=context;
        SharedPreferences currentActiveCommunity = context.getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
        activeCommunityId = currentActiveCommunity.getString("id",AppConstants.NOT_AVALABLE);
        currentUserRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.USERS).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        communityRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.COMMUNITIES);
        linkRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.INVITE_LINK);

    }

    @NonNull
    @Override
    public Result doWork() {
        if(!activeCommunityId.equals(AppConstants.NOT_AVALABLE))
        {
            handleQuit = new HandleQuit(context,currentUserRef,linkRef,communityRef.child(activeCommunityId).child(FirebaseConstants.COMMUNITYSTATUS),activeCommunityId);
            handleQuit.execute();
            return Result.success();
        }
        else
        {
            return Result.failure();
        }

    }
}
