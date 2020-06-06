package com.integrals.inlens.AsynchTasks;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.integrals.inlens.Helper.AppConstants;

import com.integrals.inlens.Helper.FirebaseConstants;
import com.integrals.inlens.Notification.NotificationHelper;
import com.integrals.inlens.Notification.RecentImageScan;

import java.util.Calendar;
import java.util.TimeZone;

public class ScannerTask extends AsyncTask<Void, Void, Void> {

    private NotificationHelper notificationHelper;
    private int count = 0;
    private Context context;

    public ScannerTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        SharedPreferences LastShownNotificationInfo = context.getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);

        if (!LastShownNotificationInfo.contains("time")) {
            SharedPreferences.Editor editor = LastShownNotificationInfo.edit();
            editor.putString("time", String.valueOf(System.currentTimeMillis()));
            editor.commit();

        } else {

            String timeStr = LastShownNotificationInfo.getString("time", String.valueOf(System.currentTimeMillis()));
            //Log.i(AppConstants.PHOTO_SCAN_WORK,"TimeString "+timeStr);

            if (!TextUtils.isEmpty(timeStr)) {

                RecentImageScan recentImageScan = new RecentImageScan(context, Long.parseLong(timeStr));

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    count = recentImageScan.getNotifiedImageCount();
                }

            }

        }

    }

    @Override
    protected Void doInBackground(Void... voids) {

        //Log.i(AppConstants.PHOTO_SCAN_WORK,"started doing in background");
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {



            try {
                //Log.i(AppConstants.PHOTO_SCAN_WORK,"get into notified block");
                SharedPreferences LastShownNotificationInfo = context.getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = LastShownNotificationInfo.edit();
                long endTime = Long.parseLong(LastShownNotificationInfo.getString("stopAt", String.valueOf(System.currentTimeMillis())));
                notificationHelper = new NotificationHelper(context);

                ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

                //Log.i(AppConstants.PHOTO_SCAN_WORK,count+" Recent Image count");
                if (count > 0 && System.currentTimeMillis() < endTime) {
                    //Log.i(AppConstants.PHOTO_SCAN_WORK,"started doing in display notification");

                    // check network connectivity
                    if(isConnected)
                    {
                        int notiCount = LastShownNotificationInfo.getInt("notiCount", 0);
                        notificationHelper.displayRecentImageNotification(count, notiCount);
                        if (notiCount < 2) {
                            editor.putInt("notiCount", ++notiCount);
                        }
                        editor.putString("time", String.valueOf(System.currentTimeMillis()));
                        editor.commit();
                    }
                }
                else if(System.currentTimeMillis()>=endTime)
                {
                    if(!LastShownNotificationInfo.contains(AppConstants.IS_NOTIFIED))
                    {
                        notificationHelper.displayAlbumEndedNotification();
                        editor.putBoolean(AppConstants.IS_NOTIFIED,true);
                        editor.commit();
                    }
                    if(isConnected)
                    {
                        FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.USERS).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(FirebaseConstants.LIVECOMMUNITYID).removeValue();
                    }
                }
                //Log.i("timeScanner","serverTime "+serverTimeInMillis+" EndTime "+endTime+" SysTime "+System.currentTimeMillis()+" offset "+offsetInMillis);
            /*
            if(serverTimeInMillis > endTime)
            {
                //Log.i("timeScannerNotif","Notification deploy");
                editor.putBoolean("notified", true);
                editor.commit();

            }

             */
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
     }
}
