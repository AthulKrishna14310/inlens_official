package com.integrals.inlens.Helper;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.integrals.inlens.R;

public class MainHorizontalOptionsViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageview;
    public MainHorizontalOptionsViewHolder(View itemView) {
        super(itemView);
        imageview = itemView.findViewById(R.id.albumcard_options_image_view);
    }
}
