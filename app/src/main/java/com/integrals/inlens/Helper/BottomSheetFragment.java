package com.integrals.inlens.Helper;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.integrals.inlens.MainActivity;
import com.integrals.inlens.Models.CommunityModel;
import com.integrals.inlens.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;
import java.util.TimeZone;

//// to do if

@SuppressLint("ValidFragment")
public class BottomSheetFragment extends BottomSheetDialogFragment {

    public View view;
    Context context;
    MainActivity activity;
    CommunityModel communityModel;
    int pos;

    public BottomSheetFragment(Context applicationContext, CommunityModel communityModel, int position) {

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
        activity = (MainActivity) getActivity();

        view = inflater.inflate(R.layout.bottom_menu_item_active, container, false);
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

        return view;
    }

    public String getDate(String timestamp) {
        try
        {
            long time = Long.parseLong(timestamp);
            TimeZone timeZone = TimeZone.getDefault();
            long offsetInMillis = timeZone.getOffset(Calendar.ZONE_OFFSET);
            time+=offsetInMillis;
            CharSequence Time = DateUtils.getRelativeDateTimeString(context, time, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
            return String.valueOf(Time);
        }
        catch (NumberFormatException e)
        {
            return "Nil";
        }
    }

}