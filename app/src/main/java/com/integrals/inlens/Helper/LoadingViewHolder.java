package com.integrals.inlens.Helper;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.integrals.inlens.R;

public class LoadingViewHolder extends RecyclerView.ViewHolder {
    public ProgressBar loadingProgressbar;

    public LoadingViewHolder(View itemView) {
        super(itemView);
        loadingProgressbar = itemView.findViewById(R.id.item_loadingprogressBar);
    }
}