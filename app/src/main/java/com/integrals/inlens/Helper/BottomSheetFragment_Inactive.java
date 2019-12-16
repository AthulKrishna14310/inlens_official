package com.integrals.inlens.Helper;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.integrals.inlens.Activities.InlensGalleryActivity;
import com.integrals.inlens.MainActivity;
import com.integrals.inlens.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

@SuppressLint("ValidFragment")
public class BottomSheetFragment_Inactive extends BottomSheetDialogFragment {

    public View view;
    DatabaseReference ParcicipantsRef;
    Context context;
    RecyclerView ParticipantsRecyclerView;
    Dialog BottomSheetParticipantsDialog;

    public BottomSheetFragment_Inactive(Context applicationContext) {
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

        view= inflater.inflate(R.layout.bottom_menu_inactive, container, false);



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


        LinearLayout linearLayout3 = view.findViewById(R.id.item_view_participants);
        linearLayout3.setOnClickListener(new View.OnClickListener() {
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