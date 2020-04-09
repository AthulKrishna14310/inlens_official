package com.integrals.inlens.WorkManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Notification.NotificationHelper;

import java.util.UUID;

public class AlbumEndWorker extends Worker {
    Context context;
    public AlbumEndWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.displayAlbumEndedNotification();
        WorkManager.getInstance().cancelAllWorkByTag(AppConstants.PHOTO_SCAN_WORK);
        SharedPreferences CurrentActiveCommunity = context.getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
        String uuidString = CurrentActiveCommunity.getString("scanWorkerId",AppConstants.NOT_AVALABLE);
        if(uuidString.equals(AppConstants.NOT_AVALABLE))
        {
            WorkManager.getInstance().cancelUniqueWork(AppConstants.PHOTO_SCAN_WORK);
        }
        else
        {
            WorkManager.getInstance().cancelWorkById(UUID.fromString(uuidString));
        }

        return Result.success();
    }

    @Override
    public void onStopped() {
        super.onStopped();
        Log.i("AlbumEndWorker","Stopped");
    }
}
