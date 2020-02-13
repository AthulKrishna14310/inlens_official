package com.integrals.inlens.JobScheduler;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.integrals.inlens.Notification.AlarmManagerHelper;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public class Scheduler extends JobService {

    ScannerTask scannerTask;
    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        AlarmManagerHelper alarmManagerHelper=new AlarmManagerHelper(getApplicationContext());
        alarmManagerHelper.initiateAlarmManager(3);


        scannerTask = new ScannerTask(this)
        {
            @Override
            protected void onPostExecute(Object o) {
                jobFinished(jobParameters,true);
            }
        };
        scannerTask.execute("");
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {

        scannerTask.cancel(true);
        return true;
    }
}
