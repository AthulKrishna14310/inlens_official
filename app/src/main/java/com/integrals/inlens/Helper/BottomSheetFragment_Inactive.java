package com.integrals.inlens.Helper;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.integrals.inlens.Activities.ReportActivity;
import com.integrals.inlens.Activities.SplashScreenActivity;
import com.integrals.inlens.MainActivity;
import com.integrals.inlens.Models.CommunityModel;
import com.integrals.inlens.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@SuppressLint("ValidFragment")
public class BottomSheetFragment_Inactive extends BottomSheetDialogFragment {

    public View view;
    Context context;
    CommunityModel communityModel;
    int pos;
    DatabaseReference reportRef,communityReportRef;
    String currentUserId;

    public BottomSheetFragment_Inactive(Context applicationContext, CommunityModel communityModel, int position,String currentUserId,DatabaseReference ref) {
        // Required empty public constructor
        context = applicationContext;
        this.communityModel = communityModel;
        pos=position;
        this.currentUserId = currentUserId;
        DatabaseReference rootRef = ref;
        reportRef = rootRef.child(FirebaseConstants.REPORTED).child(communityModel.getCommunityID());
        communityReportRef = rootRef.child(FirebaseConstants.COMMUNITIES).child(communityModel.getCommunityID()).child(FirebaseConstants.COMMUNITY_REPORTED);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        MainActivity activity = (MainActivity) getActivity();

        view = inflater.inflate(R.layout.bottom_menu_inactive, container, false);

        TextView AlbumTitle = view.findViewById(R.id.AlbumTitleBottom);
        TextView AlbumDescription = view.findViewById(R.id.AlbumDescriptionBottom);
        TextView AlbumBottomStartDate = view.findViewById(R.id.albumstartdate);
        TextView AlbumBottomEndDate = view.findViewById(R.id.albumenddate);
        TextView AlbumType = view.findViewById(R.id.albumtype);

        AlbumTitle.setText(communityModel.getTitle());
        if (TextUtils.isEmpty(communityModel.getDescription())) {
            AlbumDescription.setVisibility(View.INVISIBLE);
        } else {
            AlbumDescription.setText(communityModel.getDescription());
        }
        AlbumBottomStartDate.setText(getDate(communityModel.getStartTime()));
        AlbumBottomEndDate.setText(getDate(communityModel.getEndTime()));
        AlbumType.setText(communityModel.getType());
        ////////////////////////////////////////////////////////////////////////////////////////////////////


        ////////////////////////////////////////////////////////////////////////////////////////////////////



        LinearLayout linearLayout2 = view.findViewById(R.id.item_change_cover);
        linearLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();
                activity.setCoverChange(true);
                activity.setProfileChange(false);
                activity.setCommunityKeyForEdit(communityModel.getCommunityID(),pos);

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio((int) 37, 29)
                        .setFixAspectRatio(true)
                        .start(getActivity());


            }
        });
        LinearLayout linearLayout3 = view.findViewById(R.id.item_reportabuse);
        TextView reportedTextView = view.findViewById(R.id.item_report_textview);
        if(communityModel.isReported())
        {
            reportedTextView.setText("REMOVE MY REPORT");
            reportRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    long reporterCount =  dataSnapshot.getChildrenCount();
                    linearLayout3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dismiss();
                            if(reporterCount==1)
                            {

                                reportRef.child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful())
                                        {
                                            communityReportRef.removeValue();
                                            Toast.makeText(activity, "Removed report.", Toast.LENGTH_SHORT).show();
                                            context.startActivity(new Intent(context, SplashScreenActivity.class));
                                            ((Activity)context).overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                            ((Activity)context).finish();
                                        }
                                        else
                                        {
                                            Toast.makeText(activity, "Failed to removed report.", Toast.LENGTH_SHORT).show();

                                        }

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(activity, "Failed to removed report.", Toast.LENGTH_SHORT).show();


                                    }
                                });

                            }
                            else
                            {
                                reportRef.child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful())
                                        {
                                            Toast.makeText(activity, "Removed report.", Toast.LENGTH_SHORT).show();
                                        }
                                        else
                                        {
                                            Toast.makeText(activity, "Failed to removed report.", Toast.LENGTH_SHORT).show();

                                        }

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(activity, "Failed to removed report.", Toast.LENGTH_SHORT).show();


                                    }
                                });
                            }


                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
        else
        {
            linearLayout3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    startActivity(new Intent(getContext(), ReportActivity.class)
                            .putExtra("Album_ID",communityModel.getCommunityID())
                            .putExtra("Album_Name",communityModel.getTitle())
                            .putExtra("Album_CreatedBy",communityModel.getAdmin()));
                    ((Activity)context).overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    ((Activity)context).finish();

                }
            });
        }
        return view;
    }

    public String getDate(String timestamp)
    {
        long time = Long.parseLong(timestamp);
        CharSequence Time = DateUtils.getRelativeDateTimeString(context, time, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
        String timesubstring = Time.toString().substring(Time.length() - 8);
        Date date = new Date(time);
        String dateformat = DateFormat.format("dd-MM-yyyy", date).toString();
        return dateformat + " ," + timesubstring;
    }
}