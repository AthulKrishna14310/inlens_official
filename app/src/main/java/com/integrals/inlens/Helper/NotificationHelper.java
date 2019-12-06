package com.integrals.inlens.Helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.integrals.inlens.R;

public class NotificationHelper extends ContextWrapper {

    private static final String Channel_ID = "123";
    private static final String Channel_NAME = "Inlens_Oreo_notification";
    private NotificationManager manager;


    public NotificationHelper(Context base) {
        super(base);
        CreateChannel();
    }

    private void CreateChannel() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(Channel_ID,Channel_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(false);
            channel.enableLights(false);
            channel.setImportance(NotificationManager.IMPORTANCE_MIN);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            getManager().createNotificationChannel(channel);
        }
    }

    public NotificationManager getManager() {

        if(manager==null)
        {
             manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotificationBuilder(String title, String body)
    {
        return new Notification.Builder(getApplicationContext(),Channel_ID).setContentText(body)
                .setContentTitle(title)
            .setProgress(100,0,true)

            .setSmallIcon(R.drawable.inlens_logo_m).setOnlyAlertOnce(true)
                ;
    }

}
