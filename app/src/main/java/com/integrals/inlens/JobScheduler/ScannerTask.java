package com.integrals.inlens.JobScheduler;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

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


        if (unNotifiedImage.getCreatedTime() != null) {

            notificationHelper.displayRecentImageNotification();
            SharedPreferences LastShownNotificationInfo = context.getSharedPreferences("LastNotification.pref", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor =LastShownNotificationInfo.edit();
            editor.putString("time",unNotifiedImage.getCreatedTime());
            editor.commit();
        }
        alarmManagerHelper.initiateAlarmManager(5);

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        SharedPreferences LastShownNotificationInfo = context.getSharedPreferences("LastNotification.pref", Context.MODE_PRIVATE);
        long time = Long.parseLong(LastShownNotificationInfo.getString("time", String.valueOf(System.currentTimeMillis())));
        RecentImageScan recentImageScan = new RecentImageScan(context, time);
        unNotifiedImage = recentImageScan.checkForNotifiedImageExist();

        if(unNotifiedImage.getUri() != null)
        {
            notificationHelper = new NotificationHelper(context,unNotifiedImage.getUri());
        }
        alarmManagerHelper = new AlarmManagerHelper(context);
    }
}
