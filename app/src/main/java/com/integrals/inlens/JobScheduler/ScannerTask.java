package com.integrals.inlens.JobScheduler;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Models.GalleryImageModel;
import com.integrals.inlens.Models.UnNotifiedImageModel;
import com.integrals.inlens.Notification.AlarmManagerHelper;
import com.integrals.inlens.Notification.NotificationHelper;
import com.integrals.inlens.Notification.RecentImageScan;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

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

        SharedPreferences LastShownNotificationInfo = context.getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = LastShownNotificationInfo.edit();
        TimeZone timeZone = TimeZone.getDefault();
        long offsetInMillis = timeZone.getOffset(Calendar.ZONE_OFFSET);
        long serverTimeInMillis = System.currentTimeMillis()-offsetInMillis;
        long endTime = Long.parseLong(LastShownNotificationInfo.getString("stopAt",String.valueOf(System.currentTimeMillis())));
        if(unNotifiedImage !=null)
        {
            if (unNotifiedImage.getCreatedTime() != null) {

                notificationHelper.displayRecentImageNotification();
                editor.putString("time", unNotifiedImage.getCreatedTime());
                editor.commit();
            }
        }
        boolean isNotified = LastShownNotificationInfo.getBoolean("notified",false);
        //Log.i("timeScanner","serverTime "+serverTimeInMillis+" EndTime "+endTime+" SysTime "+System.currentTimeMillis()+" offset "+offsetInMillis);
        if(!isNotified && serverTimeInMillis > endTime)
        {
            //Log.i("timeScannerNotif","Notification deploy");
            editor.putBoolean("notified", true);
            editor.commit();
            notificationHelper = new NotificationHelper(context);
            notificationHelper.displayAlbumEndedNotification();

        }
        else
        {
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
