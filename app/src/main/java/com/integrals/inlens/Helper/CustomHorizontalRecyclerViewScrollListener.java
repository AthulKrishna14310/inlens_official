package com.integrals.inlens.Helper;

import android.support.v7.widget.RecyclerView;

public class CustomHorizontalRecyclerViewScrollListener extends RecyclerView.OnScrollListener {

    int scrollDist = 0;
    boolean isVisible = true;
    static final float MINIMUM = 25;
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);


        if(dx > MINIMUM)
        {
            loadMore();
        }



    }

    public void loadMore() {

    }
}
