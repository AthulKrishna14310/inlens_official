package com.integrals.inlens.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.integrals.inlens.JobScheduler.ScannerTask;
import com.integrals.inlens.Models.GalleryImageModel;

import java.util.List;


public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

        ScannerTask scannerTask = new ScannerTask(context);
        scannerTask.execute("");

    }
}
