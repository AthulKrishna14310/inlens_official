package com.integrals.inlens.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.integrals.inlens.AsynchTasks.HandleQuit;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.FirebaseConstants;
import com.integrals.inlens.Helper.QRCodeBottomSheet;
import com.integrals.inlens.Helper.ReadFirebaseData;
import com.integrals.inlens.Interface.FirebaseRead;
import com.integrals.inlens.MainActivity;
import com.integrals.inlens.R;
import com.integrals.inlens.WorkManager.AlbumScanWorker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.integrals.inlens.Helper.AppConstants.MY_PERMISSIONS_REQUEST_START_WORKMANAGER;

public class SplashScreenActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference userRef,communityRef,linkRef;
    List<String> userCommunityIdList;
    String currentUserId,currentActiveCommunityID=AppConstants.NOT_AVALABLE;
    static final int DELAY_IN_MILLIS=1000;
    ValueEventListener listener,communityRefListenerForActiveAlbum;

    String appTheme="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences appDataPref = getSharedPreferences(AppConstants.appDataPref, Context.MODE_PRIVATE);
        final SharedPreferences.Editor appDataPrefEditor = appDataPref.edit();
        if(appDataPref.contains(AppConstants.appDataPref_theme))
        {
            appTheme = appDataPref.getString(AppConstants.appDataPref_theme,AppConstants.themeLight);
            if(appTheme.equals(AppConstants.themeLight))
            {
                setTheme(R.style.AppTheme);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            else
            {
                setTheme(R.style.DarkTheme);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        }
        else
        {
            appTheme = AppConstants.themeLight;
            appDataPrefEditor.putString(AppConstants.appDataPref_theme,AppConstants.themeLight);
            appDataPrefEditor.commit();
            setTheme(R.style.AppTheme);

        }
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        userCommunityIdList = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.USERS);
        communityRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.COMMUNITIES);
        linkRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.INVITE_LINK);

        CheckUserAuthentication();


    }

    private void CheckUserAuthentication() {

        if (firebaseAuth.getCurrentUser() != null) {

            currentUserId  =  firebaseAuth.getCurrentUser().getUid();
            ReadFirebaseData readFirebaseData = new ReadFirebaseData();
            listener= readFirebaseData.readData(userRef.child(currentUserId), new FirebaseRead() {
                @Override
                public void onSuccess(DataSnapshot datasnapshot) {

                    if (datasnapshot.hasChild(FirebaseConstants.COMMUNITIES)) {
                        for (DataSnapshot snapshot : datasnapshot.child(FirebaseConstants.COMMUNITIES).getChildren()) {
                            userCommunityIdList.add(snapshot.getKey());
                        }
                    }
                    SharedPreferences LastShownNotificationInfo = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
                    if (datasnapshot.hasChild(FirebaseConstants.LIVECOMMUNITYID)) {

                        currentActiveCommunityID = datasnapshot.child(FirebaseConstants.LIVECOMMUNITYID).getValue().toString();
                        if (!LastShownNotificationInfo.contains("id")) {
                            SharedPreferences.Editor editor = LastShownNotificationInfo.edit();
                            editor.putString("id", currentActiveCommunityID);
                            editor.commit();
                        }

                        communityRefListenerForActiveAlbum = readFirebaseData.readData(communityRef.child(currentActiveCommunityID), new FirebaseRead() {
                            @Override
                            public void onSuccess(DataSnapshot snapshot) {

                                // optimization 1 resulted in this error, everytime the album is quit even if the album is inactive
                                // so first check the album  has status status;
                                if (snapshot.hasChild(FirebaseConstants.COMMUNITYSTATUS)) {


                                    String status = snapshot.child(FirebaseConstants.COMMUNITYSTATUS).getValue().toString();
                                    if (status.equals("T")) {
                                        long endtime = Long.parseLong(snapshot.child(FirebaseConstants.COMMUNITYENDTIME).getValue().toString());

                                        if (!LastShownNotificationInfo.contains("stopAt")) {
                                            SharedPreferences.Editor editor = LastShownNotificationInfo.edit();
                                            editor.putString("stopAt", String.valueOf(endtime));
                                            editor.commit();
                                        }

                                        TimeZone timeZone = TimeZone.getDefault();
                                        long offsetInMillis = timeZone.getOffset(Calendar.ZONE_OFFSET);
                                        long serverTimeInMillis = (System.currentTimeMillis() - offsetInMillis);
                                        //Log.i("timeQuit", "Server : " + serverTimeInMillis + " End : " + endtime + " Systemmillis : " + System.currentTimeMillis());
                                        if (serverTimeInMillis >= endtime) {
                                            quitCloudAlbum();

                                        }
                                    } else {
                                        // stop the necessary services
                                        WorkManager.getInstance().cancelAllWorkByTag(AppConstants.PHOTO_SCAN_WORK);

                                    }
                                }

                            }

                            @Override
                            public void onStart() {

                            }

                            @Override
                            public void onFailure(DatabaseError databaseError) {


                            }
                        });
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent mainIntent =  new Intent(SplashScreenActivity.this, MainActivity.class);
                            mainIntent.putStringArrayListExtra(AppConstants.USER_ID_LIST, (ArrayList<String>) userCommunityIdList);
                            startActivity(mainIntent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish();
                        }
                    },DELAY_IN_MILLIS);

                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFailure(DatabaseError databaseError) {

                }
            });

        } else {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashScreenActivity.this, AuthActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
            },DELAY_IN_MILLIS);


        }
    }


    private void quitCloudAlbum() {

        HandleQuit handleQuit = new HandleQuit(getApplicationContext(),userRef.child(currentUserId),linkRef,communityRef.child(currentActiveCommunityID).child(FirebaseConstants.COMMUNITYSTATUS),currentActiveCommunityID);
        handleQuit.execute();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listener !=null)
        {
            userRef.child(currentUserId).removeEventListener(listener);
        }

    }
}
