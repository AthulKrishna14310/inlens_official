package com.integrals.inlens.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.integrals.inlens.Activities.InlensGalleryActivity;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.MainActivity;
import com.integrals.inlens.R;
import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;

public class NotificationHelper {
    private Context context;
    int notificationIDAlbumEnd=7907,notificationIDAlbumPhoto=7907;


    public NotificationHelper(Context context) {
        this.context = context;
    }

    public void displayRecentImageNotification(int count,int notiCount){


        Intent intent = new Intent(context, InlensGalleryActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, (int) (Math.random() * 100),
                intent, 0);

        NotificationManager notificationManager= (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);


        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            String channelID = "ID_503";
            NotificationChannel notificationChannel = new NotificationChannel(channelID,"Cloud Album Photos", NotificationManager.IMPORTANCE_DEFAULT);
            if(notiCount<2)
            {
                Uri path= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{400,200,400});
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build();
                notificationChannel.setSound(path,audioAttributes);

            }
            notificationManager.createNotificationChannel(notificationChannel);

            Notification.Builder notificationBuilder = new Notification.Builder(context, channelID)
                    .setContentTitle("InLens Recent Image")
                    .setContentText("You have "+count+" new photos to upload to your album.")
                    .setSmallIcon(R.drawable.inlens_logo)
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent);



            notificationManager.notify(notificationIDAlbumEnd,notificationBuilder.build());
        }
        else
        {
            NotificationCompat.Builder builder=new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.drawable.inlens_logo)
                    .setContentTitle("InLens Recent Images")
                    .setContentText("You have "+count+" new photos to upload to your album.")
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setContentIntent(contentIntent);

            if(notiCount<2)
            {
                Uri path= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                builder.setSound(path);
                builder.setVibrate(new long[]{400,200,400});
            }
            notificationManager.notify(notificationIDAlbumPhoto,builder.build());
        }

    }

    public void displayAlbumEndedNotification(){


        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, (int) (Math.random() * 100),
                intent, 0);



        NotificationManager notificationManager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            String channelID = "ID_504";
            NotificationChannel notificationChannel = new NotificationChannel(channelID,"Cloud Album Ended", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);

            Notification.Builder notificationBuilder = new Notification.Builder(context, channelID)
                    .setContentTitle("Cloud Album Ended.")
                    .setContentText("Your cloud album has ended. Create a new album to upload more.")
                    .setSmallIcon(R.drawable.inlens_logo)
                    .setAutoCancel(true);

            notificationManager.notify(notificationIDAlbumEnd,notificationBuilder.build());
        }
        else
        {
            NotificationCompat.Builder builder=new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.drawable.inlens_logo)
                    .setContentTitle("Cloud Album Ended.")
                    .setContentText("Your cloud album has ended. Create a new album to upload more.")
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setContentIntent(contentIntent);

            Uri path= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            builder.setSound(path);
            notificationManager.notify(notificationIDAlbumEnd,builder.build());
        }

    }

//    private void generateNotificationBitmap(String imageLocation)
//    {
//        try {
//            File imageFile=new File(imageLocation);
//            recentImageBitmap = new Compressor(context)
//                    .setMaxHeight(130)
//                    .setMaxWidth(130)
//                    .setQuality(85)
//                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
//                    .compressToBitmap(imageFile);
//        } catch (IOException e) {
//            Log.d("InLens","Bitmap creation failed");
//            e.printStackTrace();
//
//        }catch (NullPointerException e){
//
//            recentImageBitmap=BitmapFactory.decodeResource(context.getResources(),
//                    R.drawable.scenery);
//        }
//
//
//
//    }

}
