package com.integrals.inlens.Activities;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.integrals.inlens.Helper.FirebaseConstants;
import com.integrals.inlens.Helper.HttpHandler;
import com.integrals.inlens.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import info.androidhive.barcode.BarcodeReader;


public class QRCodeReader extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener {

    private BarcodeReader barcodeReader;
    private DatabaseReference communityRef,userRef,participantRef;
    String currentUserId;
    ProgressBar qrcodeReaderProgressbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_reader);
        getSupportActionBar().hide();

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
                                        AddPhotographerToCommunity(barcode.displayValue);
                                        qrcodeReaderProgressbar.setVisibility(View.VISIBLE);
                                        dialog.dismiss();
                                    }
                                })
                        .addButton("NO", -1,getResources().getColor( R.color.deep_orange_A400), CFAlertDialog.CFAlertActionStyle.NEGATIVE,
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

    }

    private class checkAlbumExpired extends AsyncTask<Void, Void, Void> {
        long endtTime;
        long serverTime;
        String communityId;

        public checkAlbumExpired(long endtTime,String comId) {
            this.endtTime = endtTime;
            communityId = comId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            String jsonStr = httpHandler.makeServiceCall("http://worldtimeapi.org/api/ip");

            if (jsonStr != null) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    String serverTime = jsonObject.getString("unixtime");
                    this.serverTime = Long.parseLong(serverTime) * 1000;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (this.serverTime < endtTime)
            {
                userRef.child(FirebaseConstants.COMMUNITIES).child(communityId).setValue(ServerValue.TIMESTAMP);
                participantRef.child(communityId).child(currentUserId).setValue(ServerValue.TIMESTAMP);
                showDialogMessage("New Community","You have been added to a new community.");

            }
            else
            {
                showDialogMessage("Album Inactive","The album has expired or admin has made the album inactive.");

            }

            qrcodeReaderProgressbar.setVisibility(View.GONE);

        }
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
                                dialog.dismiss();

                            }
                        });
        builder.show();
    }
    private void AddPhotographerToCommunity(final String communityId) {

        communityRef.child(communityId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(FirebaseConstants.COMMUNITYSTATUS)) {
                    long endtime = Long.parseLong(dataSnapshot.child("endtime").getValue().toString());
                    new checkAlbumExpired(endtime,communityId).execute();
                }
                else
                {
                    showDialogMessage("Album Inactive","The album has expired or admin has made the album inactive.");

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


}



