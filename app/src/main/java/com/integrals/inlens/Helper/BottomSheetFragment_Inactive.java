package com.integrals.inlens.Helper;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.integrals.inlens.Activities.InlensGalleryActivity;
import com.integrals.inlens.MainActivity;
import com.integrals.inlens.Models.CommunityModel;
import com.integrals.inlens.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressLint("ValidFragment")
public class BottomSheetFragment_Inactive extends BottomSheetDialogFragment {

    public View view;
    Context context;
    CommunityModel communityModel;
    public BottomSheetFragment_Inactive(Context applicationContext, CommunityModel communityModel) {
        // Required empty public constructor
        context = applicationContext;
        this.communityModel = communityModel;

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

        AlbumTitle.setText(communityModel.getTitle());
        if (TextUtils.isEmpty(communityModel.getDescription())) {
            AlbumDescription.setVisibility(View.INVISIBLE);
        } else {
            AlbumDescription.setText(communityModel.getDescription());
        }
        AlbumBottomStartDate.setText(getDate(communityModel.getStartTime()));
        AlbumBottomEndDate.setText(getDate(communityModel.getEndTime()));

        ////////////////////////////////////////////////////////////////////////////////////////////////////


        ////////////////////////////////////////////////////////////////////////////////////////////////////



        LinearLayout linearLayout2 = view.findViewById(R.id.item_change_cover);
        linearLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();
                activity.setCoverChange(true);
                activity.setProfileChange(false);

                activity.setPostKeyForEdit(communityModel.getCommunityID());

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio((int) 360, 180)
                        .setFixAspectRatio(true)
                        .start(getActivity());


            }
        });

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