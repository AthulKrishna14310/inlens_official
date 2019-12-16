package com.integrals.inlens.Helper;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.integrals.inlens.R;

import java.util.ArrayList;
import java.util.List;

public class ParticipantsBottomSheet {

    String name,imgurl;
    Context context;
    Dialog BottomSheetParticipantsDialog;
    RecyclerView ParticipantsRecyclerView;
    String postKeyForEdit;
    DatabaseReference getParticipantDatabaseReference;

    public ParticipantsBottomSheet(Context context, Dialog bottomSheetParticipantsDialog, RecyclerView participantsRecyclerView, String postKeyForEdit, DatabaseReference getParticipantDatabaseReference) {
        this.context = context;
        BottomSheetParticipantsDialog = bottomSheetParticipantsDialog;
        ParticipantsRecyclerView = participantsRecyclerView;
        this.postKeyForEdit = postKeyForEdit;
        this.getParticipantDatabaseReference = getParticipantDatabaseReference;
    }

    public void DisplayParticipants() {

        BottomSheetParticipantsDialog.setCancelable(true);
        BottomSheetParticipantsDialog.setCanceledOnTouchOutside(true);
        BottomSheetParticipantsDialog.setContentView(R.layout.participants_layout);
        BottomSheetParticipantsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        BottomSheetParticipantsDialog.getWindow().getAttributes().windowAnimations = R.style.BottomUpSlideDialogAnimation;

        Window BottomSheetParticipantsDialogWindow = BottomSheetParticipantsDialog.getWindow();
        BottomSheetParticipantsDialogWindow.setGravity(Gravity.BOTTOM);
        BottomSheetParticipantsDialogWindow.setLayout(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT);
        BottomSheetParticipantsDialogWindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        BottomSheetParticipantsDialogWindow.setDimAmount(0.75f);

        ParticipantsRecyclerView = BottomSheetParticipantsDialog.findViewById(R.id.main_bottomsheet_particpants_bottomsheet_recyclerview);
        ParticipantsRecyclerView.setHasFixedSize(true);
        GridLayoutManager Gridmanager = new GridLayoutManager(context, 3);
        ParticipantsRecyclerView.setLayoutManager(Gridmanager);

        ImageButton CloseBtn = BottomSheetParticipantsDialog.findViewById(R.id.main_bottomsheet_particpants_bottomsheet_closebtn);
        CloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BottomSheetParticipantsDialog.dismiss();

            }
        });

        final List<String> MemberImageList = new ArrayList<>();
        final List<String> MemberNamesList = new ArrayList<>();

        getParticipantDatabaseReference.child("Communities").child(postKeyForEdit).child("participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                MemberImageList.clear();
                MemberNamesList.clear();
                ParticipantsRecyclerView.removeAllViews();

                for(DataSnapshot snapshot : dataSnapshot.getChildren() )
                {
                    name="NA";
                    imgurl="NA";

                    getParticipantDatabaseReference.child("Users").child(snapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.hasChild("Name"))
                            {
                                name=dataSnapshot.child("Name").getValue().toString();
                                MemberNamesList.add(name);

                            }
                            else
                            {
                                MemberNamesList.add(name);
                            }
                            if(dataSnapshot.hasChild("Profile_picture"))
                            {
                                imgurl=dataSnapshot.child("Profile_picture").getValue().toString();
                                MemberImageList.add(imgurl);

                            }
                            else
                            {
                                MemberImageList.add(imgurl);

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }

                ParticipantsAdapter participantsAdapter = new ParticipantsAdapter(MemberImageList,MemberNamesList,context);
                ParticipantsRecyclerView.setAdapter(participantsAdapter);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

}
