package com.integrals.inlens.Activities;

import android.content.Context;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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

import java.util.ArrayList;

import com.integrals.inlens.Models.PostModel;
import com.integrals.inlens.R;
import uk.co.senab.photoview.PhotoViewAttacher;


public class PhotoView extends AppCompatActivity {
    public ArrayList<PostModel> blogArrayList = new ArrayList<>();
    private int Position;
    private Button OriginalImageButton;
    private PhotoViewAttacher photoViewAttacher;
    private CardView cardView;
    private ImageView imageView;
    private Button PreviousImage, AfterImage;
    private int TotalPosts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_photo_view);

        cardView = (CardView) findViewById(R.id.PhotoCardView);
        PreviousImage = (Button) findViewById(R.id.LeftPhotoSwipe);
        AfterImage = (Button) findViewById(R.id.RightPhotoSwipe);
        blogArrayList = getIntent().getExtras().getParcelableArrayList("data");
        TotalPosts = blogArrayList.size();
        Position = getIntent().getExtras().getInt("position");
        OriginalImageButton = (Button) findViewById(R.id.OriginalImageButton);
        AfterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetAfterPhoto();
            }
        });
        PreviousImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetBeforePhoto();
            }
        });
        SetThumbPhoto(getApplicationContext(),blogArrayList,Position);
    }




    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }




    public void SetThumbPhoto(Context context,
                              ArrayList<PostModel> models,
                              int position) {




        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.ImageProgress);
        final ImageView imageView = (ImageView) findViewById(R.id.PhotoCardImageView);

        progressBar.setVisibility(View.VISIBLE);
        AfterImage.setVisibility(View.INVISIBLE);
        Glide.with(context)
                .load(models.get(position).getUri())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(context, "Load Failed. Please check your internet connection. ", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        try {
                            DownloadImageCache(blogArrayList.get(position+1).getUri());
                        }catch (IndexOutOfBoundsException e){
                            e.printStackTrace();
                        }
                        return false;

                    }


                })
                .into(imageView);

        photoViewAttacher = new PhotoViewAttacher(imageView);

    }




    private void SetAfterPhoto() {
        Position += 1;
        try {
            SetThumbPhoto(getApplicationContext(),blogArrayList,Position);

        } catch (IndexOutOfBoundsException e) {
            Position -= 1;
            Toast.makeText(getApplicationContext(), "Last Post", Toast.LENGTH_SHORT).show();
            e.printStackTrace();

        }


    }

    private void SetBeforePhoto() {
        Position -= 1;
        try {

            SetThumbPhoto(getApplicationContext(),blogArrayList,Position);


        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            Position += 1;
            Toast.makeText(getApplicationContext(), "First Post", Toast.LENGTH_SHORT).show();

        }

    }

    private void DownloadImageCache(String Url){

        Glide.with(getApplicationContext())
                .load(Url)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        AfterImage.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        AfterImage.setVisibility(View.VISIBLE);
                        return false;
                    }
                }).preload();
    }


}