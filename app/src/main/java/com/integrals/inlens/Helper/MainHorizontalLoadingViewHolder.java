package com.integrals.inlens.Helper;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.integrals.inlens.R;

public class MainHorizontalLoadingViewHolder extends RecyclerView.ViewHolder {

    public ProgressBar progressBar;
    public MainHorizontalLoadingViewHolder(View itemView) {
        super(itemView);
        progressBar = itemView.findViewById(R.id.horiozntal_item_loadingprogressbar);
    }
}
