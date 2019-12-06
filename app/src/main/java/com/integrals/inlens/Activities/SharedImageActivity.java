package com.integrals.inlens.Activities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import com.integrals.inlens.Models.PostModel;
import com.integrals.inlens.R;

import java.util.List;

import uk.co.senab.photoview.PhotoView;

public class SharedImageActivity extends AppCompatActivity {

    private ImageView ShareImageView;
    private ProgressBar ShareImageProgressbar;
    private RelativeLayout RootForSharedImage;
    private List<PostModel> PostImageList;
    private int PostIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_image);

        ShareImageView = findViewById(R.id.shareimage_photoview);
        ShareImageProgressbar = findViewById(R.id.shareimage_progressbar);
        RootForSharedImage = findViewById(R.id.rootforsharedimage);

        String ImageUrl = getIntent().getStringExtra("url");
        Bundle bundle = getIntent().getExtras();
        PostImageList = bundle.getParcelableArrayList("situation_images");
        PostIndex =bundle.getInt("index");

        if(!TextUtils.isEmpty(ImageUrl))
        {
            RequestOptions requestOptions = new RequestOptions()
                    .fitCenter();

            Glide.with(getApplicationContext())
                    .load(ImageUrl)
                    .apply(requestOptions)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            ShareImageProgressbar.setVisibility(View.GONE);
                            Snackbar.make(RootForSharedImage,"Failed to load image",Snackbar.LENGTH_SHORT).show();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            ShareImageProgressbar.setVisibility(View.GONE);
                            Snackbar.make(RootForSharedImage,"Loaded image.",Snackbar.LENGTH_SHORT).show();
                            return false;
                        }
                    })
                    .into(ShareImageView);
        }
        else if(PostImageList.size()!=0 && PostIndex >= 0)
        {
            RequestOptions requestOptions = new RequestOptions()
                    .fitCenter().placeholder(R.drawable.ic_album_cover_image_default);

            Glide.with(getApplicationContext())
                    .load(PostImageList.get(PostIndex).getUri())
                    .apply(requestOptions)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            ShareImageProgressbar.setVisibility(View.GONE);
                            Snackbar.make(RootForSharedImage,"Failed to load image",Snackbar.LENGTH_SHORT).show();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            ShareImageProgressbar.setVisibility(View.GONE);
                            Snackbar.make(RootForSharedImage,"Loaded image.",Snackbar.LENGTH_SHORT).show();
                            return true;
                        }
                    })
                    .into(ShareImageView);

            ShareImageView.setOnTouchListener(new OnSwipeTouchListener(SharedImageActivity.this) {
                public void onSwipeTop() {
                    Toast.makeText(SharedImageActivity.this, "top", Toast.LENGTH_SHORT).show();
                }
                public void onSwipeRight() {
                    Toast.makeText(SharedImageActivity.this, "right", Toast.LENGTH_SHORT).show();
                }
                public void onSwipeLeft() {
                    Toast.makeText(SharedImageActivity.this, "left", Toast.LENGTH_SHORT).show();
                }
                public void onSwipeBottom() {
                    Toast.makeText(SharedImageActivity.this, "bottom", Toast.LENGTH_SHORT).show();
                }

            });
        }
        else
        {
            Snackbar.make(RootForSharedImage,"Unable to view image",Snackbar.LENGTH_SHORT).show();
        }

    }

    public class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;

        public OnSwipeTouchListener (Context ctx){
            gestureDetector = new GestureDetector(ctx, new GestureListener());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                            result = true;
                        }
                    }
                    else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                        result = true;
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }

        public void onSwipeRight() {
        }

        public void onSwipeLeft() {
        }

        public void onSwipeTop() {
        }

        public void onSwipeBottom() {
        }
    }
}
