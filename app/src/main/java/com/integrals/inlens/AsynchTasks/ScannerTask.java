package com.integrals.inlens.AsynchTasks;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import androidx.work.WorkManager;

import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Models.UnNotifiedImageModel;
import com.integrals.inlens.Notification.InlensImageModel;
import com.integrals.inlens.Notification.NotificationHelper;
import com.integrals.inlens.Notification.RecentImageScan;

import java.util.Calendar;
import java.util.TimeZone;

public class ScannerTask extends AsyncTask<Void,Void,Void> {

    private NotificationHelper notificationHelper;
    private InlensImageModel imageModel;
    private Context context;

    public ScannerTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
        {
            SharedPreferences LastShownNotificationInfo = context.getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = LastShownNotificationInfo.edit();
            TimeZone timeZone = TimeZone.getDefault();
            long offsetInMillis = timeZone.getOffset(Calendar.ZONE_OFFSET);
            long serverTimeInMillis = System.currentTimeMillis()-offsetInMillis;
            long endTime = Long.parseLong(LastShownNotificationInfo.getString("stopAt",String.valueOf(System.currentTimeMillis())));

            notificationHelper = new NotificationHelper(context);
            boolean isNotified = LastShownNotificationInfo.getBoolean("notified",false);


            if(!imageModel.getUri().equals("") && !imageModel.getLastModified().equals("") && !isNotified)
            {
                int notiCount = LastShownNotificationInfo.getInt("notiCount",0);
                notificationHelper.displayRecentImageNotification(imageModel.getUri(),imageModel.getCount(),notiCount);
                if(notiCount<2)
                {
                    editor.putInt("notiCount", ++notiCount);
                }
                editor.putString("time", imageModel.getLastModified());
                editor.commit();
            }
            //Log.i("timeScanner","serverTime "+serverTimeInMillis+" EndTime "+endTime+" SysTime "+System.currentTimeMillis()+" offset "+offsetInMillis);
            if(!isNotified && serverTimeInMillis > endTime)
            {
                //Log.i("timeScannerNotif","Notification deploy");
                editor.putBoolean("notified", true);
                editor.commit();
                notificationHelper.displayAlbumEndedNotification();
                WorkManager.getInstance().cancelAllWorkByTag(AppConstants.PHOTO_SCAN_WORK);
            }
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        SharedPreferences LastShownNotificationInfo = context.getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
        if (!LastShownNotificationInfo.contains("time")) {
            SharedPreferences.Editor editor = LastShownNotificationInfo.edit();
            editor.putString("time", String.valueOf(System.currentTimeMillis()));
            editor.commit();
        }
        else
        {
            String timeStr = LastShownNotificationInfo.getString("time", String.valueOf(System.currentTimeMillis()));
            if(!TextUtils.isEmpty(timeStr))
            {
                RecentImageScan recentImageScan = new RecentImageScan(context, Long.parseLong(timeStr));

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                {
                    imageModel = recentImageScan.getNotifiedImageCount();
                }
            }

        }



    }
}
