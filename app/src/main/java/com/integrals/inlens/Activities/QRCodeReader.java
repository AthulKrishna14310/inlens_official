package com.integrals.inlens.Activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;

import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.FirebaseConstants;
import com.integrals.inlens.MainActivity;
import com.integrals.inlens.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import info.androidhive.barcode.BarcodeReader;


public class QRCodeReader extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener {

    private BarcodeReader barcodeReader;
    private DatabaseReference communityRef, userRef, participantRef;
    String currentUserId;
    ProgressBar qrcodeReaderProgressbar;
    List<String> userCommunityIdList;
    static final int MY_PERMISSIONS_REQUEST_CAMERA=459;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_reader);
        getSupportActionBar().hide();

        userCommunityIdList = getIntent().getExtras().getStringArrayList(AppConstants.USER_ID_LIST);


        qrcodeReaderProgressbar = findViewById(R.id.qrcodeReaderProgressbar);
        barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_fragment);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        communityRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.COMMUNITIES);
        userRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.USERS).child(currentUserId);
        participantRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.PARTICIPANTS);


    }

    @Override
    public void onScanned(final Barcode barcode) {
        barcodeReader.pauseScanning();
        // play beep sound
        barcodeReader.playBeep();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                CFAlertDialog.Builder builder = new CFAlertDialog.Builder(QRCodeReader.this)
                        .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                        .setTitle("New Community")
                        .setIcon(R.drawable.inlens_logo)
                        .setMessage("Are you sure you want to join this new community? This means quitting the previous one.")
                        .setTextGravity(Gravity.START)
                        .setCancelable(false)
                        .addButton("YES", -1, getResources().getColor(R.color.colorAccent), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        addPhotographerToCommunity(barcode.displayValue);
                                        qrcodeReaderProgressbar.setVisibility(View.VISIBLE);
                                        dialog.dismiss();
                                    }
                                })
                        .addButton("NO", -1, getResources().getColor(R.color.deep_orange_A400), CFAlertDialog.CFAlertActionStyle.NEGATIVE,
                                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                builder.show();


            }
        });
    }

    @Override
    public void onScannedMultiple(List<Barcode> barcodes) {

    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {

    }

    @Override
    public void onScanError(String errorMessage) {

    }

    @Override
    public void onCameraPermissionDenied() {
        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                .setTitle("Camera Permission")
                .setIcon(R.drawable.ic_info)
                .setMessage("InLens require camera permission to scan QR code. Please enable it and try again.")
                .setCancelable(false)
                .addButton("OK", -1, getResources().getColor(R.color.colorAccent), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(QRCodeReader.this, new String[]{Manifest.permission.CAMERA},MY_PERMISSIONS_REQUEST_CAMERA);
                                dialog.dismiss();

                            }
                        })
                .addButton("CANCEL", -1, getResources().getColor(R.color.deep_orange_A400), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent mainIntent = new Intent(QRCodeReader.this, MainActivity.class);
                                mainIntent.putStringArrayListExtra(AppConstants.USER_ID_LIST, (ArrayList<String>) userCommunityIdList);
                                startActivity(mainIntent);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                finish();
                                dialog.dismiss();

                            }
                        });

        builder.show();
    }


    public void showDialogMessage(String title, String message) {
        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                .setTitle(title)
                .setIcon(R.drawable.ic_check_circle_black_24dp)
                .setMessage(message)
                .setCancelable(false)
                .addButton("OK", -1, getResources().getColor(R.color.colorAccent), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent mainIntent = new Intent(QRCodeReader.this, MainActivity.class);
                                mainIntent.putStringArrayListExtra(AppConstants.USER_ID_LIST, (ArrayList<String>) userCommunityIdList);
                                startActivity(mainIntent);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                finish();
                                dialog.dismiss();
                            }
                        });
        builder.show();
    }

    public void showDialogInfo(String title, String message) {
        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                .setTitle(title)
                .setIcon(R.drawable.ic_info)
                .setMessage(message)
                .setCancelable(false)
                .addButton("OK", -1, getResources().getColor(R.color.colorAccent), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent mainIntent = new Intent(QRCodeReader.this, MainActivity.class);
                                mainIntent.putStringArrayListExtra(AppConstants.USER_ID_LIST, (ArrayList<String>) userCommunityIdList);
                                startActivity(mainIntent);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                finish();
                                dialog.dismiss();
                            }
                        });
        builder.show();
    }

    private String getOffsetDeletedTime(String timeStamp) {
        TimeZone timeZone = TimeZone.getDefault();
        long offsetInMillis = timeZone.getOffset(Calendar.ZONE_OFFSET);
        long givenTime = Long.parseLong(timeStamp);
        long offsetDeletedTime = givenTime-offsetInMillis;
        return String.valueOf(offsetDeletedTime);
    }

    private void addPhotographerToCommunity(final String communityId) {

        communityRef.child(communityId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(FirebaseConstants.COMMUNITYSTATUS)) {
                    long endtime = Long.parseLong(dataSnapshot.child(FirebaseConstants.COMMUNITYENDTIME).getValue().toString());
                    TimeZone timeZone = TimeZone.getDefault();
                    long offsetInMillis = timeZone.getOffset(Calendar.ZONE_OFFSET);
                    long serverTimeInMillis = (System.currentTimeMillis() - offsetInMillis);


                    if (serverTimeInMillis < endtime) {

                        userRef.child(FirebaseConstants.COMMUNITIES).child(communityId).setValue(ServerValue.TIMESTAMP);
                        userRef.child(FirebaseConstants.LIVECOMMUNITYID).setValue(communityId);

                        participantRef.child(communityId).child(currentUserId).setValue(ServerValue.TIMESTAMP);
                        userCommunityIdList.add(0,communityId);
                        showDialogMessage("New Community", "You have been added to a new community.");

                        SharedPreferences CurrentActiveCommunity = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
                        SharedPreferences.Editor ceditor = CurrentActiveCommunity.edit();
                        ceditor.putString("id", communityId);
                        ceditor.putString("time", String.valueOf(System.currentTimeMillis()));
                        ceditor.putString("stopAt", getOffsetDeletedTime(String.valueOf(endtime)));
                        ceditor.putInt("notiCount", 0);
                        ceditor.commit();


                    } else {

                        showDialogInfo("Album Inactive", "The album has expired or admin has made the album inactive.");

                    }

                } else {
                    showDialogInfo("Album Inactive", "The album has expired or admin has made the album inactive.");

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent mainIntent = new Intent(QRCodeReader.this, MainActivity.class);
        mainIntent.putStringArrayListExtra(AppConstants.USER_ID_LIST, (ArrayList<String>) userCommunityIdList);
        startActivity(mainIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}



