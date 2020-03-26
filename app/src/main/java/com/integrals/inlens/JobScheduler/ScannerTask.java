package com.integrals.inlens.JobScheduler;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Models.GalleryImageModel;
import com.integrals.inlens.Models.UnNotifiedImageModel;
import com.integrals.inlens.Notification.AlarmManagerHelper;
import com.integrals.inlens.Notification.NotificationHelper;
import com.integrals.inlens.Notification.RecentImageScan;

import java.util.List;

public class ScannerTask extends AsyncTask {

    private NotificationHelper notificationHelper;
    private AlarmManagerHelper alarmManagerHelper;
    private UnNotifiedImageModel unNotifiedImage;
    private Context context;

    public ScannerTask(Context context) {
        this.context = context;
    }

    @Override
    protected Object doInBackground(Object[] objects) {

        if(unNotifiedImage !=null)
        {
            SharedPreferences LastShownNotificationInfo = context.getSharedPreferences(AppConstants.LAST_NOTIFICATION_PREF, Context.MODE_PRIVATE);
            if (unNotifiedImage.getCreatedTime() != null) {

                notificationHelper.displayRecentImageNotification();
                SharedPreferences.Editor editor = LastShownNotificationInfo.edit();
                editor.putString("time", unNotifiedImage.getCreatedTime());
                editor.commit();
            }
            alarmManagerHelper.initiateAlarmManager(5);
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        SharedPreferences LastShownNotificationInfo = context.getSharedPreferences(AppConstants.LAST_NOTIFICATION_PREF, Context.MODE_PRIVATE);
        if (!LastShownNotificationInfo.contains("time")) {
            SharedPreferences.Editor editor = LastShownNotificationInfo.edit();
            editor.putString("time", String.valueOf(System.currentTimeMillis()));
            editor.commit();
        }

        long time = Long.parseLong(LastShownNotificationInfo.getString("time", String.valueOf(System.currentTimeMillis())));
        RecentImageScan recentImageScan = new RecentImageScan(context, time);

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            unNotifiedImage = recentImageScan.checkForNotifiedImageExist();
            if (unNotifiedImage.getUri() != null) {
                notificationHelper = new NotificationHelper(context, unNotifiedImage.getUri());
            }
            alarmManagerHelper = new AlarmManagerHelper(context);

        }


    }
}
