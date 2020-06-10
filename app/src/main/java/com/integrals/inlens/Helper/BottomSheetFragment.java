package com.integrals.inlens.Helper;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.integrals.inlens.Activities.ReportActivity;
import com.integrals.inlens.Activities.SplashScreenActivity;
import com.integrals.inlens.MainActivity;
import com.integrals.inlens.Models.CommunityModel;
import com.integrals.inlens.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

//// to do if

@SuppressLint("ValidFragment")
public class BottomSheetFragment extends BottomSheetDialogFragment {

    public View view;
    Context context;
    MainActivity activity;
    CommunityModel communityModel;
    int pos;
    DatabaseReference reportRef,communityReportRef;
    String currentUserId;

    Handler customHandler = new Handler();
    TextView endTimeTextView;

    final Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {

            if (endTimeTextView != null) {
                if(System.currentTimeMillis()-Long.parseLong(communityModel.getEndTime())<60000)
                {
                    CharSequence Time = DateUtils.getRelativeDateTimeString(context, Long.parseLong(communityModel.getEndTime()), DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
                    String timesubstring = Time.toString().substring(Time.length() - 8);
                    endTimeTextView.setText(String.format("few seconds left, %s", timesubstring));
                    customHandler.removeCallbacks(updateTimerThread);
                }
                {
                    endTimeTextView.setText(getDate(communityModel.getEndTime()));
                }
                Log.i("timer","run");
            }
            customHandler.postDelayed(this, 60*1000);

        }
    };


    public BottomSheetFragment(Context applicationContext, CommunityModel communityModel, int position,String currentUserId,DatabaseReference ref) {

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
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        customHandler.removeCallbacks(updateTimerThread);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        activity = (MainActivity) getActivity();

        view = inflater.inflate(R.layout.bottom_menu_item_active, container, false);
        TextView AlbumTitle = view.findViewById(R.id.AlbumTitleBottom);
        TextView AlbumDescription = view.findViewById(R.id.AlbumDescriptionBottom);
        TextView AlbumBottomStartDate = view.findViewById(R.id.albumstartdate);
        TextView AlbumBottomEndDate = view.findViewById(R.id.albumenddate);
        TextView AlbumType = view.findViewById(R.id.albumtype);
        endTimeTextView = AlbumBottomEndDate;

        AlbumTitle.setText(communityModel.getTitle());
        if (TextUtils.isEmpty(communityModel.getDescription())) {
            AlbumDescription.setVisibility(View.INVISIBLE);
        } else {
            AlbumDescription.setText(communityModel.getDescription());
        }
        AlbumBottomStartDate.setText(getDate(communityModel.getStartTime()));
        if(System.currentTimeMillis()-Long.parseLong(communityModel.getEndTime())<60000)
        {
            CharSequence Time = DateUtils.getRelativeDateTimeString(context, Long.parseLong(communityModel.getEndTime()), DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
            String timesubstring = Time.toString().substring(Time.length() - 8);
            AlbumBottomEndDate.setText(String.format("few seconds left, %s", timesubstring));

        }
        {
            AlbumBottomEndDate.setText(getDate(communityModel.getEndTime()));
            customHandler.postDelayed(updateTimerThread, 60*1000);

        }
        AlbumType.setText(communityModel.getType());



////////////////////////////////////////////////////////////////////////////////////////////////////


////////////////////////////////////////////////////////////////////////////////////////////////////

       /*
        LinearLayout AddPhotos = view.findViewById(R.id.item_add_photos);
        AddPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                Intent intent = new Intent(getContext(), InlensGalleryActivity.class);
                intent.putExtra("CommunityID", activity.getMyCommunityDetails().get(activity.getPosition()).getCommunityID());
                intent.putExtra("CommunityName", activity.getMyCommunityDetails().get(activity.getPosition()).getTitle());
                intent.putExtra("CommunityStartTime", activity.getMyCommunityDetails().get(activity.getPosition()).getStartTime());
                intent.putExtra("CommunityEndTime", activity.getMyCommunityDetails().get(activity.getPosition()).getEndTime());
                startActivity(intent);
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        */


        LinearLayout ChangeCover = view.findViewById(R.id.item_change_cover);
        ChangeCover.setOnClickListener(new View.OnClickListener() {
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

        LinearLayout QuitCloudAlbum = view.findViewById(R.id.item_quit_cloud_album);
        QuitCloudAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                activity.quitCloudAlbum(false);


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

    public String getDate(String timestamp) {
        try
        {
            long time = Long.parseLong(timestamp);
            /*TimeZone timeZone = TimeZone.getDefault();
            long offsetInMillis = timeZone.getOffset(Calendar.ZONE_OFFSET);
            time+=offsetInMillis;*/
            CharSequence Time = DateUtils.getRelativeDateTimeString(context, time, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
            return String.valueOf(Time);
        }
        catch (NumberFormatException e)
        {
            return "Nil";
        }
    }

}