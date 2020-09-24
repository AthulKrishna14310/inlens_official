package com.integrals.inlens.Helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.integrals.inlens.Activities.QRCodeReader;
import com.integrals.inlens.BuildConfig;
import com.integrals.inlens.MainActivity;
import com.integrals.inlens.Models.PhotographerModel;
import com.integrals.inlens.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("ValidFragment")
public class QRCodeBottomSheet extends BottomSheetDialogFragment {

    String currentActiveCommunityId;
    DatabaseReference reqRef,userRef;
    View qrCodeView;
    int themeId;
    boolean initialStart,isAdmin;
    MainActivity activity;
    public TextView cancelButton;
    View rootView;
    List<PhotographerModel> membersList;
    RequestedUserAdapter requestedUserAdapter;

    public QRCodeBottomSheet(View rootView,String id, DatabaseReference rootRef, boolean initialStart, MainActivity activity, boolean isAdmin) {
        this.rootView=rootView;
        currentActiveCommunityId = id;
        this.reqRef = rootRef.child(FirebaseConstants.REQUESTS).child(currentActiveCommunityId);
        this.userRef = rootRef.child(FirebaseConstants.USERS);
        this.initialStart=initialStart;
        this.activity=activity;
        membersList = new ArrayList<>();
        this.isAdmin =isAdmin;
        requestedUserAdapter = new RequestedUserAdapter(membersList);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        SharedPreferences themePref = activity.getSharedPreferences(AppConstants.appDataPref, Context.MODE_PRIVATE);
        if (themePref.contains(AppConstants.appDataPref_theme)) {
            if (themePref.getString(AppConstants.appDataPref_theme, AppConstants.themeLight).equals(AppConstants.themeLight)) {
                themeId = R.style.AppTheme;
                ContextWrapper contextWrapper = new ContextWrapper(activity);
                contextWrapper.setTheme(R.style.AppTheme);
                qrCodeView = inflater.cloneInContext(contextWrapper).inflate(R.layout.qrcode_generator_layout, container, false);

            } else {
                themeId = R.style.DarkTheme;
                ContextWrapper contextWrapper = new ContextWrapper(activity);
                contextWrapper.setTheme(R.style.DarkTheme);
                qrCodeView = inflater.cloneInContext(contextWrapper).inflate(R.layout.qrcode_generator_layout, container, false);

            }
        } else {
            themeId = R.style.AppTheme;
            ContextWrapper contextWrapper = new ContextWrapper(activity);
            contextWrapper.setTheme(R.style.AppTheme);
            qrCodeView = inflater.cloneInContext(contextWrapper).inflate(R.layout.qrcode_generator_layout, container, false);

        }


        Button InviteLinkButton = qrCodeView.findViewById(R.id.InviteLinkButton);
        TextView QRCodeCloseBtn = qrCodeView.findViewById(R.id.cancelButton);

        RecyclerView reqRecyclerView = qrCodeView.findViewById(R.id.req_recyclerview);
        reqRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        reqRecyclerView.setHasFixedSize(true);

        if(isAdmin)
        {
//            Toast.makeText(activity, "visible recyclerview", Toast.LENGTH_SHORT).show();
            reqRecyclerView.setVisibility(View.VISIBLE);
            reqRecyclerView.setAdapter(requestedUserAdapter);
        }
        else
        {
//            Toast.makeText(activity, "gone recyclerview", Toast.LENGTH_SHORT).show();
            reqRecyclerView.setVisibility(View.GONE);
        }


        reqRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    userRef.child(snapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {

                            String name = AppConstants.NOT_AVALABLE, imgurl = AppConstants.NOT_AVALABLE, email = AppConstants.NOT_AVALABLE;

                            if (userSnapshot.hasChild("Name")) {
                                name = userSnapshot.child("Name").getValue().toString();
                            }

                            if (userSnapshot.hasChild("Profile_picture")) {
                                imgurl = userSnapshot.child("Profile_picture").getValue().toString();
                            }

                            if (userSnapshot.hasChild("Email")) {
                                email = userSnapshot.child("Email").getValue().toString();
                            }

                            if(!getPhotographerKeys(membersList).contains(snapshot.getKey()))
                            {
                                membersList.add(new PhotographerModel(name,snapshot.getKey(),imgurl,email));
                                requestedUserAdapter.notifyDataSetChanged();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        qrCodeView.findViewById(R.id.ScanButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                startActivity(new Intent(activity, QRCodeReader.class).putStringArrayListExtra(AppConstants.USER_ID_LIST, (ArrayList<String>)activity.getUserCommunityIdList()));
            }
        });
        QRCodeCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        InviteLinkButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
                showSnackbarMessage(rootView,"Preparing community");
                shareInviteLink(currentActiveCommunityId);

            }
        });

        cancelButton=qrCodeView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             dismiss();
            }
        });

        return qrCodeView;
    }

    public class RequestedUserAdapter extends RecyclerView.Adapter<RequestedUserAdapter.RequestedUserViewHolder>
    {

        List<PhotographerModel> participantList;

        public RequestedUserAdapter(List<PhotographerModel> participantList) {
            this.participantList = participantList;
        }

        @NonNull
        @Override
        public RequestedUserAdapter.RequestedUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RequestedUserViewHolder(LayoutInflater.from(activity).inflate(R.layout.req_user_layout,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull RequestedUserAdapter.RequestedUserViewHolder holder, int position) {

            holder.reqUserNameTextView.setText(" "+participantList.get(position).getName()+" requested to join your album.");

            holder.denyReqBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    reqRef.child(participantList.get(position).getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            participantList.remove(position);
                            requestedUserAdapter.notifyDataSetChanged();
                            try {
                                Toast.makeText(activity, "Rejected "+participantList.get(position).getName(), Toast.LENGTH_SHORT).show();

                            }catch (Exception e)
                            {
                                Toast.makeText(activity, "Rejected user", Toast.LENGTH_SHORT).show();

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(activity, "Failed to perform operation.", Toast.LENGTH_SHORT).show();

                        }
                    });

                }
            });

            holder.acceptReqBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // to convert these as transactions

                    reqRef.child(participantList.get(position).getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            String participantPath =  FirebaseConstants.PARTICIPANTS+"/"+currentActiveCommunityId+"/";
                            String userPath  =   FirebaseConstants.USERS+"/"+participantList.get(position).getId()+"/";
                            Map acceptedMap = new HashMap();
                            acceptedMap.put(participantPath+participantList.get(position).getId(), ServerValue.TIMESTAMP);
                            acceptedMap.put(userPath+FirebaseConstants.COMMUNITIES+"/"+currentActiveCommunityId,ServerValue.TIMESTAMP);
                            acceptedMap.put(userPath+FirebaseConstants.LIVECOMMUNITYID,currentActiveCommunityId);

                            FirebaseDatabase.getInstance().getReference().updateChildren(acceptedMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                    if(databaseError!=null)
                                    {
                                        Toast.makeText(activity, "Failed to perform operation.", Toast.LENGTH_SHORT).show();
                                        reqRef.child(participantList.get(position).getId()).setValue(ServerValue.TIMESTAMP);
                                    }
                                    else
                                    {
                                        try {
                                            Toast.makeText(activity, "Added "+participantList.get(position).getName(), Toast.LENGTH_SHORT).show();

                                        }catch (Exception e)
                                        {
                                            Toast.makeText(activity, "Added user", Toast.LENGTH_SHORT).show();

                                        }
                                        participantList.remove(position);
                                        requestedUserAdapter.notifyDataSetChanged();
                                    }

                                }
                            });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(activity, "Failed to perform operation.", Toast.LENGTH_SHORT).show();

                        }
                    });

                }
            });


        }

        @Override
        public int getItemCount() {
            return participantList.size();
        }

        public class RequestedUserViewHolder extends RecyclerView.ViewHolder {

            TextView reqUserNameTextView;
            Button acceptReqBtn,denyReqBtn;

            public RequestedUserViewHolder(@NonNull View itemView) {
                super(itemView);

                reqUserNameTextView =  itemView.findViewById(R.id.req_user_name_textview);
                acceptReqBtn=itemView.findViewById(R.id.req_user_accept_btn);
                denyReqBtn = itemView.findViewById(R.id.req_user_reject_btn);
            }
        }
    }

    private List<String> getPhotographerKeys(List<PhotographerModel> photographerList) {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < photographerList.size(); i++) {
            try {
                keys.add(photographerList.get(i).getId());
            } catch (NullPointerException e) {
                continue;
            }
        }

        return keys;
    }


    private void showSnackbarMessage(View rootView, String message) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
    }

    private void shareInviteLink(String CommunityID) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        String link = "https://inlens.com/?invitedby="+ uid+"&comId="+currentActiveCommunityId+"&time="+System.currentTimeMillis()+"&adminId="+FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(link))
                .setDomainUriPrefix("https://inlens.page.link")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder(BuildConfig.APPLICATION_ID)
                                .setMinimumVersion(10)
                                .build())
                .buildShortDynamicLink()
                .addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
                    @Override
                    public void onSuccess(ShortDynamicLink shortDynamicLink) {
                        String invitationUrl = shortDynamicLink.getShortLink().toString();
                        final Intent SharingIntent = new Intent(Intent.ACTION_SEND);
                        SharingIntent.setType("text/plain");
                        SharingIntent.putExtra(Intent.EXTRA_TEXT, "InLens Album \n" + invitationUrl);
                        activity.startActivity(SharingIntent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(activity, "Failed to create link.", Toast.LENGTH_SHORT).show();
                    }
                });



    }


}
