package com.integrals.inlens.Helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.integrals.inlens.Models.PhotographerModel;
import com.integrals.inlens.R;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressLint("ValidFragment")

public class PhotographerFragementBottomSheetAdmin extends BottomSheetDialogFragment {

    private View view;
    Context context;
    PhotographerModel photographerModel;
    boolean isAdmin;

    public PhotographerFragementBottomSheetAdmin(Context context,PhotographerModel photographerModel,boolean isAdmin) {
        this.photographerModel = photographerModel;
        this.context=context;
        this.isAdmin = isAdmin;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.photographer_display_layout_admin, container, false);

        CircleImageView userImageview = view.findViewById(R.id.imageView_photographer);
        ProgressBar progressBar = view.findViewById(R.id.photographer_progressbar);

        if(!photographerModel.getImgUrl().equals(AppConstants.NOT_AVALABLE))
        {
            Glide.with(context).load(photographerModel.getImgUrl()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    Log.i("photographer","error :  "+e);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            }).into(userImageview);
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
            removeLayoutTextView.setText("ADMIN");
        }

        return view;
    }
}


