package com.integrals.inlens.Activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ViewFlipper;


import com.integrals.inlens.MainActivity;
import com.integrals.inlens.R;

import com.squareup.picasso.Picasso;

public class WorkingIntroActivity extends AppCompatActivity {

    private ViewFlipper TourViewFlipper;
    private Boolean SHOW_TOUR = true;
    private String StringShowTour;
    private ImageButton CloseTourImageButton;
    private int Images[] = {R.drawable.intro_slide_1,R.drawable.intro_slide_2,R.drawable.intro_slide_3,R.drawable.intro_slide_4};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_working_intro);

        getSupportActionBar().hide();
        TourViewFlipper = findViewById(R.id.tour_viewflipper);
        CloseTourImageButton = findViewById(R.id.close_tour_imagebutton);

        StringShowTour = getIntent().getStringExtra("ShowTour");
        if(StringShowTour.equals("no"))
        {
            SHOW_TOUR = false;
        }
        else
        {
            SHOW_TOUR=true;
        }

        CloseTourImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onBackPressed();
            }
        });

        for(int i=0; i< Images.length;i++)
        {
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Picasso.get().load(Images[i]).into(imageView);
            TourViewFlipper.addView(imageView);

            TourViewFlipper.setInAnimation(getApplicationContext(),R.anim.cloud_album_fade_in);
            TourViewFlipper.setOutAnimation(getApplicationContext(),R.anim.cloud_album_fade_out);
        }


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        super.onBackPressed();
    }
}
