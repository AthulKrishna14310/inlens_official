package com.integrals.inlens.WorkManager;

import android.content.Context;
import androidx.annotation.NonNull;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.integrals.inlens.AsynchTasks.ScannerTask;

public class AlbumScanWorker extends Worker {

    private ScannerTask scannerTask ;

    public AlbumScanWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        scannerTask = new ScannerTask(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        if(scannerTask!=null)
        {
            scannerTask.execute();
            //Log.i(AppConstants.PHOTO_SCAN_WORK,"Work Executed");
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
        //Log.i(AppConstants.PHOTO_SCAN_WORK,"Work Stopped");

    }


}

