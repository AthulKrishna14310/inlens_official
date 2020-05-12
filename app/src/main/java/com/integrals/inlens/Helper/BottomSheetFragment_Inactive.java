package com.integrals.inlens.Helper;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.integrals.inlens.Activities.ReportActivity;
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
    public BottomSheetFragment_Inactive(Context applicationContext, CommunityModel communityModel, int position) {
        // Required empty public constructor
        context = applicationContext;
        this.communityModel = communityModel;
        pos=position;
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
        linearLayout3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                startActivity(new Intent(getContext(), ReportActivity.class)
                        .putExtra("Album_ID",communityModel.getCommunityID())
                        .putExtra("Album_Name",communityModel.getTitle())
                        .putExtra("Album_CreatedBy",communityModel.getAdmin()));

            }
        });
        return view;
    }

    public String getDate(String timestamp)
    {
        long time = Long.parseLong(timestamp);
        TimeZone timeZone = TimeZone.getDefault();
        long offsetInMillis = timeZone.getOffset(Calendar.ZONE_OFFSET);
        time+=offsetInMillis;
        CharSequence Time = DateUtils.getRelativeDateTimeString(context, time, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
        String timesubstring = Time.toString().substring(Time.length() - 8);
        Date date = new Date(time);
        String dateformat = DateFormat.format("dd-MM-yyyy", date).toString();
        return dateformat + " ," + timesubstring;
    }
}