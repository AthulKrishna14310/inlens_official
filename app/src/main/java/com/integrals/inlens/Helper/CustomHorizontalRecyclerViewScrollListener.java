package com.integrals.inlens.Helper;

import android.support.v7.widget.RecyclerView;

public class CustomHorizontalRecyclerViewScrollListener extends RecyclerView.OnScrollListener {

    int scrollDist = 0;
    boolean isVisible = true;
    static final float MINIMUM = 25;
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (isVisible && scrollDist > MINIMUM) {
            hide();
            scrollDist = 0;
            isVisible = false;
        }
        else if (!isVisible && scrollDist < -MINIMUM) {
            show();
            scrollDist = 0;
            isVisible = true;
        }
        else  if(dx > MINIMUM)
        {
            loadMore();
        }

        if ((isVisible && dx > 0) || (!isVisible && dx < 0)) {
            scrollDist += dx;
        }


    }

    public void show() {

    }

    public void hide() {

    }

    public void loadMore() {

    }
}
