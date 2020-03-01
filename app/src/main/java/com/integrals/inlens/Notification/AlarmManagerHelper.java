package com.integrals.inlens.Notification;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

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
                + (i * 1000), pendingIntent);
        Log.d("InLens","Alarm Manager  initiated");



    }

    public void deinitateAlarmManager(){
        alarmManager.cancel(pendingIntent);
        NotificationManager notificationManager=
                (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        Log.d("InLens","Alarm Manager  de-initiated");
        Toast.makeText(context, "Stopped inlens service", Toast.LENGTH_SHORT).show();
    }

}
