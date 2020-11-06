package com.integrals.inlens.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.widget.RemoteViews;

import com.integrals.inlens.Activities.InlensGalleryActivity;
import com.integrals.inlens.MainActivity;
import com.integrals.inlens.R;

public class NotificationHelper {
    private Context context;
    //private RemoteViews notificationLayout;

    int notificationIdAlbumEnd =7908,
            notificationIdAlbumPhoto =7907,
            notificationIdAlbumStart=7906,
            notificationGalleryReport=7852;



    public NotificationHelper(Context context) {
        this.context = context;
        //notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_layout);
    }

    public void displayRecentImageNotification(int count,int notiCount){


        Intent intent = new Intent(context, InlensGalleryActivity.class);
        intent.putExtra("direct","Notification");
        PendingIntent contentIntent = PendingIntent.getActivity(context, (int) (Math.random() * 100),
                intent, 0);

        NotificationManager notificationManager= (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            String channelID = "ID_503";
            NotificationChannel notificationChannel = new NotificationChannel(channelID,"Cloud Album Photos", NotificationManager.IMPORTANCE_LOW);
            if(notiCount<2)
            {
                Uri path= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                notificationChannel.enableVibration(false);
                notificationChannel.setVibrationPattern(new long[]{400,200,400});
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build();
                notificationChannel.setSound(path,audioAttributes);

            }
            notificationManager.createNotificationChannel(notificationChannel);

            Notification.Builder notificationBuilder = new Notification.Builder(context, channelID)
                    .setContentText("You have "+count+" new photos to upload to your album.")
                    .setContentTitle("Upload Photos")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setAutoCancel(true)
                    .setColor(context.getColor(R.color.colorLightAccent))
                    .setLargeIcon(Icon.createWithResource(context,R.drawable.ic_upload_notification))
                    .setContentIntent(contentIntent);



            notificationManager.notify(notificationIdAlbumEnd,notificationBuilder.build());
        }
        else
        {
            Bitmap b=BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_upload_notification);
            NotificationCompat.Builder builder=new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.drawable.ic_notification)
                    .setContentText("You have "+count+" new photos to upload to your album.")
                    .setContentTitle("Upload Photos")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(context.getColor(R.color.colorLightAccent))
                    .setLargeIcon(b)
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent);

            if(notiCount<2)
            {
                Uri path= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                builder.setSound(path);
                builder.setVibrate(new long[]{400,200,400});
            }
            notificationManager.notify(notificationIdAlbumPhoto,builder.build());
        }

    }

    public void displayAlbumEndedNotification(){


        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, (int) (Math.random() * 100),
                intent, 0);



        NotificationManager notificationManager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();


        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            String channelID = "ID_504";
            NotificationChannel notificationChannel = new NotificationChannel(channelID,"Cloud Album Ended", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
            Notification.Builder notificationBuilder = new Notification.Builder(context, channelID)
                    .setContentTitle("Cloud Album Ended.")
                    .setContentText("Your cloud album  for this event has ended. Create a new album to upload more.")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setLargeIcon(Icon.createWithResource(context,R.drawable.ic_exit))
                    .setColor(context.getColor(R.color.Light_red_inlens))
                    .setAutoCancel(true);

            notificationManager.notify(notificationIdAlbumEnd,notificationBuilder.build());
        }
        else
        {
            Bitmap b=BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_exit);
            NotificationCompat.Builder builder=new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Cloud Album Ended.")
                    .setContentText("Your cloud album has ended. Create a new album to upload more.")
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setLargeIcon(b)
                    .setColor(context.getColor(R.color.Light_red_inlens))
                    .setContentIntent(contentIntent);

            Uri path= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            builder.setSound(path);
            notificationManager.notify(notificationIdAlbumEnd,builder.build());
        }

    }

    public void displayTitleMesageNoti(String title, String message){



        NotificationManager notificationManager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);



        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            String channelID = "ID_512";
            NotificationChannel notificationChannel = new NotificationChannel(channelID,"Upload status", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(notificationChannel);

            Notification.Builder notificationBuilder = new Notification.Builder(context, channelID)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setAutoCancel(true);

            notificationManager.notify(notificationGalleryReport,notificationBuilder.build());
        }
        else
        {
            NotificationCompat.Builder builder=new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setAutoCancel(true)
                    .setOngoing(false);
            notificationManager.notify(notificationGalleryReport,builder.build());
        }

    }

    public void displayAlbumStartNotification(String title,String desc){


        Intent intent = new Intent(context, InlensGalleryActivity.class);
        intent.putExtra("direct","Notification");
        PendingIntent contentIntent = PendingIntent.getActivity(context, (int) (Math.random() * 100),
                intent, 0);

        NotificationManager notificationManager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();


        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            String channelID = "ID_505";
            NotificationChannel notificationChannel = new NotificationChannel(channelID,"Cloud Album Started", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
            Notification.Builder notificationBuilder = new Notification.Builder(context, channelID)
                    .setOngoing(true)
                    .setContentTitle(title)
                    .setContentText(desc)
                    .setLargeIcon(Icon.createWithResource(context,R.drawable.ic_upload_notification))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(context.getColor(R.color.colorLightAccent))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true);
            notificationManager.notify(notificationIdAlbumStart,notificationBuilder.build());
        }
        else
        {
            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_upload_notification);
            NotificationCompat.Builder builder=new NotificationCompat.Builder(context);
            builder .setOngoing(true)
                    .setContentTitle(title)
                    .setContentText(desc)
                    .setLargeIcon(bm)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(context.getColor(R.color.colorLightAccent))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true);

            notificationManager.notify(notificationIdAlbumStart,builder.build());
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
