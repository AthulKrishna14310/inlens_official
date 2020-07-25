package com.integrals.inlens.Helper;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.google.firebase.database.DatabaseReference;
import com.integrals.inlens.Activities.QRCodeReader;
import com.integrals.inlens.MainActivity;
import com.integrals.inlens.Models.PhotographerModel;
import com.integrals.inlens.R;


import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ParticipantsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    int VIEW_TYPE_PHOTOGRAPHER = 0, VIEW_TYPE_ADD_BUTTON = 1;
    List<PhotographerModel> photographersList;
    QRCodeBottomSheet qrCodeBottomSheet;
    MainActivity activity;
    String adminId;
    DatabaseReference participantRef,userRef;
    String communityId;


    public ParticipantsAdapter(List<PhotographerModel> photographersList,
                               MainActivity activity, QRCodeBottomSheet qrcodeDialog, String adminId,DatabaseReference ref,String communityId) {
        this.photographersList = photographersList;
        this.qrCodeBottomSheet = qrcodeDialog;
        this.activity = activity;
        this.adminId = adminId;
        participantRef = ref.child(FirebaseConstants.PARTICIPANTS).child(communityId);
        userRef = ref.child(FirebaseConstants.USERS);
        this.communityId=communityId;
    }

    @Override
    public int getItemViewType(int position) {
        if (photographersList.get(position).getName().equals("add") && photographersList.get(position).getId().equals("add") && photographersList.get(position).getImgUrl().equals("add")) {
            return VIEW_TYPE_ADD_BUTTON;

        } else {
            return VIEW_TYPE_PHOTOGRAPHER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_PHOTOGRAPHER) {
            View view = LayoutInflater.from(activity).inflate(R.layout.member_card, parent, false);
            return new ParticipantsViewHolder(view);
        } else if (viewType == VIEW_TYPE_ADD_BUTTON) {
            View view = LayoutInflater.from(activity).inflate(R.layout.member_add_card, parent, false);
            return new AddParticipantsViewHolder(view);
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ParticipantsViewHolder) {
            ParticipantsViewHolder viewHolder = (ParticipantsViewHolder) holder;
            if (photographersList.get(position).getName().length() > 6) {
                String name = photographersList.get(position).getName().substring(0, 5) + "...";
                viewHolder.PName.setText(name);

            } else {
                viewHolder.PName.setText(photographersList.get(position).getName());

            }

            RequestOptions rq = new RequestOptions().placeholder(R.drawable.ic_member_card);
            Glide.with(activity).load(photographersList.get(position).getImgUrl()).apply(rq).into(viewHolder.PImage);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (activity.getCurrentUserId().equals(adminId)) {

                        if (photographersList.get(position).getId().equals(adminId)) {
                            PhotographerFragementBottomSheetAdmin photographerFragementBottomSheetAdmin = new PhotographerFragementBottomSheetAdmin(activity, photographersList.get(position),true);
                            photographerFragementBottomSheetAdmin.show(((FragmentActivity) activity).getSupportFragmentManager(), photographerFragementBottomSheetAdmin.getTag());

                        } else {
                            PhotographerFragementBottomSheetNA photographerFragementBottomSheetNA = new PhotographerFragementBottomSheetNA(activity, photographersList.get(position),true,userRef,participantRef,communityId);
                            photographerFragementBottomSheetNA.show(((FragmentActivity) activity).getSupportFragmentManager(), photographerFragementBottomSheetNA.getTag());

                        }
                    }
                    else
                    {
                        if (photographersList.get(position).getId().equals(adminId)) {
                            PhotographerFragementBottomSheetAdmin photographerFragementBottomSheetAdmin = new PhotographerFragementBottomSheetAdmin(activity, photographersList.get(position),false);
                            photographerFragementBottomSheetAdmin.show(((FragmentActivity) activity).getSupportFragmentManager(), photographerFragementBottomSheetAdmin.getTag());

                        } else {
                            PhotographerFragementBottomSheetNA photographerFragementBottomSheetNA = new PhotographerFragementBottomSheetNA(activity, photographersList.get(position),false,userRef,participantRef,communityId);
                            photographerFragementBottomSheetNA.show(((FragmentActivity) activity).getSupportFragmentManager(), photographerFragementBottomSheetNA.getTag());

                        }
                    }

                }
            });


        } else if (holder instanceof AddParticipantsViewHolder) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    qrCodeBottomSheet.show(activity.getSupportFragmentManager(),qrCodeBottomSheet.getTag());
                }
            });

        }


    }


    @Override
    public int getItemCount() {
        return photographersList.size();
    }


}

class ParticipantsViewHolder extends RecyclerView.ViewHolder {

    CircleImageView PImage;
    TextView PName;

    public ParticipantsViewHolder(View itemView) {
        super(itemView);

        PImage = itemView.findViewById(R.id.participants_profile_pic);
        PName = itemView.findViewById(R.id.participants_username);
    }
}

class AddParticipantsViewHolder extends RecyclerView.ViewHolder {

    public AddParticipantsViewHolder(View itemView) {
        super(itemView);

    }
}