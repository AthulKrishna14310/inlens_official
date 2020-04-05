package com.integrals.inlens.WorkManager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.integrals.inlens.AsynchTasks.ScannerTask;
import com.integrals.inlens.Helper.AppConstants;

public class MyWorker extends Worker {

    private ScannerTask scannerTask ;

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        scannerTask = new ScannerTask(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        if(scannerTask!=null)
        {
            scannerTask.execute();
            Log.i(AppConstants.PHOTO_SCAN_WORK,"Work Executed");
            return Result.success();
        }
        else
        {
            return Result.failure();
        }

    }

    @Override
    public void onStopped() {
        super.onStopped();
        Log.i(AppConstants.PHOTO_SCAN_WORK,"Work Stopped");

    }
}

