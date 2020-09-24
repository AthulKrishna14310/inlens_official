package com.integrals.inlens.Activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.AppCompatDelegate;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.FirebaseConstants;
import com.integrals.inlens.Helper.SnackShow;
import com.integrals.inlens.MainActivity;
import com.integrals.inlens.Notification.NotificationHelper;
import com.integrals.inlens.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import info.androidhive.barcode.BarcodeReader;


public class QRCodeReader extends AppCompatActivity {

    private BarcodeReader barcodeReader;
    private DatabaseReference communityRef, userRef, participantRef,tempAccessRef;
    String currentUserId;
    ProgressBar qrcodeReaderProgressbar;
    List<String> userCommunityIdList;
    static final int MY_PERMISSIONS_REQUEST_CAMERA=459;
    RelativeLayout relativeLayout;

    private String createIntent="NO";
    private String ID="";

    String appTheme="";
    int cf_bg_color,colorPrimary,red_inlens,cf_alert_dialogue_dim_bg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        relativeLayout=findViewById(R.id.rlQR);
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

        if(appTheme.equals(AppConstants.themeLight))
        {
            cf_bg_color = getResources().getColor(R.color.Light_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorLightPrimary);
            red_inlens =  getResources().getColor(R.color.Light_red_inlens);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Light_cf_alert_dialogue_dim_bg);
        }
        else
        {
            cf_bg_color = getResources().getColor(R.color.Dark_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorDarkPrimary);
            red_inlens =  getResources().getColor(R.color.Dark_red_inlens);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Dark_cf_alert_dialogue_dim_bg);

        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_reader);

        userCommunityIdList = getIntent().getExtras().getStringArrayList(AppConstants.USER_ID_LIST);


        qrcodeReaderProgressbar = findViewById(R.id.qrcodeReaderProgressbar);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        communityRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.COMMUNITIES);
        userRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.USERS);
        participantRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.PARTICIPANTS);
        tempAccessRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.TEMP_ACCESS);

        userRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(FirebaseConstants.LIVECOMMUNITYID))
                {
                    String communityId = dataSnapshot.child(FirebaseConstants.LIVECOMMUNITYID).getValue().toString();
                    barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_fragment);
                    barcodeReader.setListener(new BarcodeReader.BarcodeReaderListener() {
                        @Override
                        public void onScanned(Barcode barcode) {

                            addPhotographerToCommunity(barcode.displayValue,communityId,barcodeReader);

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
                            CFAlertDialog.Builder builder = new CFAlertDialog.Builder(QRCodeReader.this)
                                    .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                                    .setTitle("Camera Permission")
                                    .setIcon(R.drawable.ic_info)
                                    .setDialogBackgroundColor(cf_bg_color)
                                    .setTextColor(colorPrimary)
                                    .setMessage("InLens require camera permission to scan QR code. Please enable it and try again.")
                                    .setCancelable(false)
                                    .addButton("OK",
                                            colorPrimary,
                                            cf_alert_dialogue_dim_bg,
                                            CFAlertDialog.CFAlertActionStyle.DEFAULT,
                                            CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    ActivityCompat.requestPermissions(QRCodeReader.this, new String[]{Manifest.permission.CAMERA},MY_PERMISSIONS_REQUEST_CAMERA);
                                                    dialog.dismiss();

                                                }
                                            })
                                    .addButton("CANCEL", red_inlens,
                                            cf_alert_dialogue_dim_bg,
                                            CFAlertDialog.CFAlertActionStyle.DEFAULT,
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
                    });

                }
                else
                {
                    Toast.makeText(QRCodeReader.this, "You are not participating in any album.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        findViewById(R.id.closeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }



    private void addPhotographerToCommunity(String newUserId, final String communityId,BarcodeReader barcodeReader) {

        userRef.child(newUserId).child(FirebaseConstants.NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    String newUserName = dataSnapshot.getValue().toString();
                    String userModifyPath =FirebaseConstants.USERS+"/"+newUserId+"/";
                    String participantPath = FirebaseConstants.PARTICIPANTS+"/"+communityId+"/";

                    // info:
                    // if user is already present in an album permission is denied from the server
                    // we can also check that manually
                    // advantage of manual check => prevent write and delete operations on the db
                    // disadvantage of manual check => user need to perform one network operation

                    tempAccessRef.child(newUserId).child(FirebaseConstants.TEMP_ACCESS_GRANTED_UID).setValue(currentUserId).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            // info : if user creates another album, the temp access should be cleared
                            Map addUserMap =new HashMap();
                            addUserMap.put(userModifyPath+FirebaseConstants.LIVECOMMUNITYID,communityId);
                            addUserMap.put(userModifyPath+FirebaseConstants.COMMUNITIES+"/"+communityId,ServerValue.TIMESTAMP);
                            addUserMap.put(participantPath+newUserId,ServerValue.TIMESTAMP);
                            FirebaseDatabase.getInstance().getReference().updateChildren(addUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                    // todo write cloud function to delete temp access


                                    if(databaseError!=null)
                                    {
                                        tempAccessRef.child(newUserId).removeValue();
                                        Toast.makeText(QRCodeReader.this, databaseError.getMessage()+"\nUser may already be in an album.", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        tempAccessRef.child(newUserId).removeValue();
                                         new SnackShow(relativeLayout,QRCodeReader.this)
                                                    .showSuccessSnack(" "+newUserName.split(" ")[0]+" can now upload photos to your album");
                                        barcodeReader.playBeep();
                                    }
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Log.i("qrreader","error "+e);

                        }
                    });




                }
                else
                {
                    Toast.makeText(QRCodeReader.this, "No user found.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

}



