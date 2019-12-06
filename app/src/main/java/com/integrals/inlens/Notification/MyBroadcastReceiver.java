package com.integrals.inlens.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.integrals.inlens.Models.GalleryImageModel;

import java.util.List;


public class MyBroadcastReceiver extends BroadcastReceiver {
    private NotificationHelper notificationHelper;
    private AlarmManagerHelper alarmManagerHelper;
    private List<GalleryImageModel> AllImages;

    @Override
    public void onReceive(final Context context, Intent intent) {

        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {


                if (AllImages.size() > 0) {

                    notificationHelper.displayRecentImageNotification();
                    SharedPreferences LastShownNotificationInfo = context.getSharedPreferences("LastNotification.pref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor =LastShownNotificationInfo.edit();
                    editor.putString("time",AllImages.get(AllImages.size()-1).getCreatedTime());
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
                AllImages = recentImageScan.getAllShownImagesPath();
                if(AllImages.size()>0)
                {
                    notificationHelper = new NotificationHelper(context,AllImages.get(AllImages.size()-1).getImageUri());
                }
                alarmManagerHelper = new AlarmManagerHelper(context);
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
            }
        }.execute("");


    }
}
