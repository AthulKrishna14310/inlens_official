package com.integrals.inlens.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.integrals.inlens.R;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import info.androidhive.barcode.BarcodeReader;


public class QRCodeReader extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener {

    private TextView NewCommunityStatus;
    private BarcodeReader barcodeReader;
    private DatabaseReference Ref;

    private String CommunityStartTime="Not Available",CommunityEndTime="Not Available",CurrentUserName="Not Available";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_reader);
        getSupportActionBar().hide();

        barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_fragment);
        NewCommunityStatus =findViewById(R.id.NewCommunityStatus);
        Ref = FirebaseDatabase.getInstance().getReference();

    }

    @Override
    public void onScanned(final Barcode barcode) {
        barcodeReader.pauseScanning();
        // play beep sound
        barcodeReader.playBeep();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                AlertDialog.Builder builder = new AlertDialog.Builder(QRCodeReader.this);

                builder.setTitle("New Community")
                        .setMessage("Are you sure you want to join this new community? This means leaving the previous community by default.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                AddPhotographerToCommunity(barcode.displayValue);

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();


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



    private void AddPhotographerToCommunity(final String displayValue) {

        Ref.child("Communities").child(displayValue).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if(dataSnapshot.hasChild("endtime"))
                {
                    long endtime = Long.parseLong(dataSnapshot.child("endtime").getValue().toString());

                    if(endtime > System.currentTimeMillis())
                    {

                        Ref.child("Communities").child(displayValue).child("participants").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                                {
                                    Toast.makeText(getApplicationContext(), "Rejoined this community.", Toast.LENGTH_SHORT).show();
                                    Ref.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("live_community").setValue(displayValue);
                                    Ref.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("dead_community").removeValue();
                                }
                                else {

                                    Ref.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("dead_community").removeValue();
                                    Ref.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("live_community").setValue(displayValue);
                                    Ref.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Communities").child(displayValue).setValue(ServerValue.TIMESTAMP);
                                    Ref.child("Communities").child(displayValue).child("participants").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("time").setValue(ServerValue.TIMESTAMP);
                                    Ref.child("Communities").child(displayValue).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            if(dataSnapshot.hasChild("starttime"))
                                            {
                                                CommunityStartTime = dataSnapshot.child("starttime").getValue().toString();
                                            }
                                            if(dataSnapshot.hasChild("endtime"))
                                            {
                                                CommunityEndTime = dataSnapshot.child("endtime").getValue().toString();
                                            }


                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }


                            }


                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Community have expired. Create a new community.", Toast.LENGTH_SHORT).show();

                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Community not valid anymore.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


}



