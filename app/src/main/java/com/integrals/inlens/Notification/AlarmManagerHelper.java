package com.integrals.inlens.Notification;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static android.content.Context.ALARM_SERVICE;

public class AlarmManagerHelper {
  private Context context;
  private AlarmManager alarmManager;
  private Intent alarmIntent;
  private PendingIntent pendingIntent;

    public AlarmManagerHelper(Context context) {
        this.context = context;
        alarmManager=(AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmIntent = new Intent(context, MyBroadcastReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(
                context, 234324243, alarmIntent, 0);

    }


    public void initiateAlarmManager(int i){

        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                + (i * 60 * 1000), pendingIntent);
        // the alarm manager is initiated only afer i minutes
        Log.d("InLens","Alarm Manager  initiated");

    }

    public void deinitateAlarmManager(){
        alarmManager.cancel(pendingIntent);
        NotificationManager notificationManager=
                (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        Log.d("InLens","Alarm Manager  de-initiated");

    }

}
