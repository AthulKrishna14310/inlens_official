package com.integrals.inlens.Helper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.integrals.inlens.Models.PhotographerModel;
import com.integrals.inlens.R;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressLint("ValidFragment")

public class PhotographerFragementBottomSheetNA extends BottomSheetDialogFragment {

    private View view;

    Context context;
    PhotographerModel photographerModel;
    boolean isAdmin;
    DatabaseReference userRef,photographersRef;
    String communityId;
    int themeId;

    public PhotographerFragementBottomSheetNA(Context context,PhotographerModel photographerModel,boolean isAdmin,DatabaseReference userRef,DatabaseReference photographersRef,String communityId) {
        this.photographerModel = photographerModel;
        this.context=context;
        this.isAdmin = isAdmin;
        this.userRef = userRef;
        this.photographersRef=photographersRef;
        this.communityId=communityId;
        themeId = R.style.AppTheme;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view ;

        SharedPreferences themePref =context.getSharedPreferences(AppConstants.appDataPref, Context.MODE_PRIVATE);
        if(themePref.contains(AppConstants.appDataPref_theme))
        {
            if(themePref.getString(AppConstants.appDataPref_theme,AppConstants.themeLight).equals(AppConstants.themeLight))
            {
                ContextWrapper contextWrapper = new ContextWrapper(context);
                contextWrapper.setTheme(R.style.AppTheme);
                view= inflater.cloneInContext(contextWrapper).inflate(R.layout.photographer_display_layout_not_admin, container, false);

            }
            else
            {
                themeId=R.style.DarkTheme;
                ContextWrapper contextWrapper = new ContextWrapper(context);
                contextWrapper.setTheme(R.style.DarkTheme);
                view = inflater.cloneInContext(contextWrapper).inflate(R.layout.photographer_display_layout_not_admin, container, false);

            }
        }
        else
        {
            ContextWrapper contextWrapper = new ContextWrapper(context);
            contextWrapper.setTheme(R.style.AppTheme);
            view= inflater.cloneInContext(contextWrapper).inflate(R.layout.photographer_display_layout_not_admin, container, false);

        }



        CircleImageView userImageview = view.findViewById(R.id.imageView_photographer);
        ProgressBar progressBar = view.findViewById(R.id.photographer_progressbar);

        if(!photographerModel.getImgUrl().equals(AppConstants.NOT_AVALABLE))
        {

            Glide.with(context).load(photographerModel.getImgUrl()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            }).into(userImageview);
        }else {
            progressBar.setVisibility(View.GONE);
            Glide.with(context)
                    .load(context.getDrawable(R.drawable.ic_member_card))
                    .into(userImageview);
        }
        TextView nameTextView,emailTextView,removeLayoutTextView;
        nameTextView = view.findViewById(R.id.name_photographer);
        emailTextView = view.findViewById(R.id.name_photographer_email);
        nameTextView.setText(photographerModel.getName());
        emailTextView.setText(photographerModel.getEmail());

        RelativeLayout removeLayout =  view.findViewById(R.id.remove_layout);
        removeLayoutTextView = view.findViewById(R.id.remove_layout_textview);

        if(!isAdmin)
        {
            view.findViewById(R.id.divider).setVisibility(View.INVISIBLE);
            removeLayout.setVisibility(View.GONE);
        }
        else
        {

            AlertDialog.Builder removeDialog = new AlertDialog.Builder(context);
            removeDialog.setMessage("Are you sure you want to remove "+photographerModel.getName()+" from this album?");
            removeDialog.setTitle("Removing User");
            removeDialog.setCancelable(true);


            removeDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {


                    userRef.child(photographerModel.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            try
                            {
                                String currentLiveCommunityId = dataSnapshot.child(FirebaseConstants.LIVECOMMUNITYID).getValue().toString();
                                Log.i("comid","live "+currentLiveCommunityId);
                                Log.i("comid","id "+communityId);

                                if(currentLiveCommunityId.equals(communityId))
                                {
                                    userRef.child(photographerModel.getId()).child(FirebaseConstants.LIVECOMMUNITYID).removeValue();
                                }
                            }
                            catch (Exception e)
                            {
                                Log.i("comid","error  "+e.toString());

                            }

                            userRef.child(photographerModel.getId()).child(FirebaseConstants.COMMUNITIES).child(communityId).removeValue();
                            photographersRef.child(photographerModel.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(context, "User removed.", Toast.LENGTH_SHORT).show();
                                        dismiss();
                                    }
                                    else
                                    {
                                        Toast.makeText(context, "Failed to remove user.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });



                }
            });
            removeDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });


            removeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    removeDialog.show();

                }
            });
        }

        return view;
    }
}


