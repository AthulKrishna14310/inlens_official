package com.integrals.inlens.Helper;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import java.util.List;

public class BottomSheetFragment extends BottomSheetDialogFragment {

    public View view;
    DatabaseReference ParcicipantsRef;
    Context context;
    RecyclerView ParticipantsRecyclerView;
    Dialog BottomSheetParticipantsDialog;

    public BottomSheetFragment(Context applicationContext) {
        // Required empty public constructor
        context = applicationContext;
        ParcicipantsRef = FirebaseDatabase.getInstance().getReference();
        BottomSheetParticipantsDialog = new Dialog(context, android.R.style.Theme_Light_NoTitleBar);

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

        view= inflater.inflate(R.layout.bottom_menu_items, container, false);

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
                } else {
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


        LinearLayout linearLayout4 =  view.findViewById(R.id.item_view_participants);
        linearLayout4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ParticipantsBottomSheet participantsBottomSheet = new ParticipantsBottomSheet(context,BottomSheetParticipantsDialog,ParticipantsRecyclerView,activity.getMyCommunityDetails().get(activity.getPosition()).getCommunityID(), FirebaseDatabase.getInstance().getReference());
                participantsBottomSheet.DisplayParticipants();
                BottomSheetParticipantsDialog.show();
            }
        });



        return view;
    }




}