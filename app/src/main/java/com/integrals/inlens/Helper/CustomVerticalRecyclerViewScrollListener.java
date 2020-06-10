package com.integrals.inlens.Helper;

import androidx.recyclerview.widget.RecyclerView;

public class CustomVerticalRecyclerViewScrollListener extends RecyclerView.OnScrollListener {

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
        else  if(dy > MINIMUM)
        {
            loadMore();
        }

        if ((isVisible && dy > 0) || (!isVisible && dy < 0)) {
            scrollDist += dy;
        }


    }

    public void show() {

    }

    public void hide() {

    }

    public void loadMore() {

    }
}
