package com.integrals.inlens.Helper;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.integrals.inlens.Activities.InlensGalleryActivity;
import com.integrals.inlens.MainActivity;
import com.integrals.inlens.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressLint("ValidFragment")
public class BottomSheetFragment extends BottomSheetDialogFragment {

    public View view;
    DatabaseReference ParcicipantsRef;
    Context context;
    RecyclerView ParticipantsRecyclerView;
    MainActivity activity;
    List <String> ParticipantIdList;



    String name,imgurl;
    String postKeyForEdit;
    DatabaseReference getParticipantDatabaseReference;



    public BottomSheetFragment(Context applicationContext, List<String> participantIDs) {

        context = applicationContext;
        ParcicipantsRef = FirebaseDatabase.getInstance().getReference();
        getParticipantDatabaseReference=FirebaseDatabase.getInstance().getReference();
        ParticipantIdList = participantIDs;

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

        view= inflater.inflate(R.layout.bottom_menu_items, container, false);
        TextView AlbumTitle =view.findViewById(R.id.AlbumTitleBottom);
        TextView AlbumDescription=view.findViewById(R.id.AlbumDescriptionBottom);
        TextView AlbumBottomDate=view.findViewById(R.id.AlbumDateBottom);

        AlbumTitle.setText(activity.getMyCommunityDetails().get(activity.getPosition()).getTitle());
        AlbumDescription.setText(activity.getMyCommunityDetails().get(activity.getPosition()).getDescription());
        AlbumBottomDate.setText("Album started on " + getDate(activity.getMyCommunityDetails().get(activity.getPosition()).getStartTime()) + " till " + getDate(activity.getMyCommunityDetails().get(activity.getPosition()).getEndTime()));


////////////////////////////////////////////////////////////////////////////////////////////////////


        ParticipantsRecyclerView = view.findViewById(R.id.main_bottomsheet_particpants_bottomsheet_recyclerview);
        ParticipantsRecyclerView.setHasFixedSize(true);
        GridLayoutManager Gridmanager = new GridLayoutManager(context, 3);
        ParticipantsRecyclerView.setLayoutManager(Gridmanager);
        postKeyForEdit=activity.getMyCommunityDetails().get(activity.getPosition()).getCommunityID();


        final List<String> MemberImageList = new ArrayList<>();
        final List<String> MemberNamesList = new ArrayList<>();
        ParticipantsRecyclerView.removeAllViews();
        for (String id : ParticipantIdList)
        {
            name="NA";
            imgurl="NA";

            getParticipantDatabaseReference.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.hasChild("Name"))
                    {
                        name=dataSnapshot.child("Name").getValue().toString();
                        if(!MemberNamesList.contains(name) || !MemberNamesList.contains(id))
                        {
                            MemberNamesList.add(name);

                        }
                    }
                    else
                    {
                        MemberNamesList.add(id);
                    }
                    if(dataSnapshot.hasChild("Profile_picture"))
                    {
                        imgurl=dataSnapshot.child("Profile_picture").getValue().toString();
                        if(!MemberImageList.contains(imgurl) || !MemberImageList.contains(id))
                        {
                            MemberImageList.add(imgurl);

                        }
                    }
                    else
                    {
                        MemberImageList.add(id);

                    }



                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        ParticipantsAdapter adapter = new ParticipantsAdapter(MemberImageList,MemberNamesList,context);
        ParticipantsRecyclerView.setAdapter(adapter);


////////////////////////////////////////////////////////////////////////////////////////////////////

        LinearLayout linearLayout=view.findViewById(R.id.item_add_photos);
        linearLayout.setOnClickListener(new View.OnClickListener() {
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

        LinearLayout linearLayout1=view.findViewById(R.id.item_add_participants);
        linearLayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (activity.getMyCommunityDetails().get(activity.getPosition()).getCommunityID().equals(activity.getCurrentActiveCommunityID())
                        || activity.getMyCommunityDetails().get(activity.getPosition()).getCommunityID().equals(activity.getCurrentActiveCommunityID())) {
                    activity.QRCodeInit(activity.getMyCommunityDetails().get(activity.getPosition()).getCommunityID());

                }
                else {
                    Toast.makeText(getContext(), "Inactive album.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        LinearLayout linearLayout2=view.findViewById(R.id.item_change_cover);
        linearLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();
                activity.setCoverChange(true);
                activity.setProfileChange(false);
                activity.setPostKeyForEdit( activity.getMyCommunityDetails().get(activity.getPosition()).getCommunityID());

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio((int) 360, 180)
                        .setFixAspectRatio(true)
                        .start(getActivity());



            }
        });

        LinearLayout linearLayout3=view.findViewById(R.id.item_quit_cloud_album);
        linearLayout3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
               activity.quitCloudAlbum(0);


            }
        });

//
//        LinearLayout linearLayout4 =  view.findViewById(R.id.item_view_participants);
//        linearLayout4.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                ParticipantsBottomSheet participantsBottomSheet = new ParticipantsBottomSheet(context,BottomSheetParticipantsDialog,ParticipantsRecyclerView,activity.getMyCommunityDetails().get(activity.getPosition()).getCommunityID(), FirebaseDatabase.getInstance().getReference());
//                participantsBottomSheet.DisplayParticipants();
//                BottomSheetParticipantsDialog.show();
//            }
//        });



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