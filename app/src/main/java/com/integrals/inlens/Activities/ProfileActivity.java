package com.integrals.inlens.Activities;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.integrals.inlens.Helper.PreOperationCheck;
import com.integrals.inlens.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView ProfileImageView;
    private TextView ProfileCommunityCountTextview, ProfileNameTextview, ProfileEmailTextview, BackgroundServiceTextView,BatteryOptimizationTextView;
    private DatabaseReference Ref;
    private int CCount;
    private ProgressBar ProfileCommunityProgressbar;
    private ImageButton ProfileCommunityImageChangeButton;
    private static boolean COVER_CHANGE = false, PROFILE_CHANGE = false;
    private static final int GALLERY_PICK = 1;
    private ImageButton ProfilebackButton;
    private List<String> AllCommunities;
    private List<String> AllImages;
    private RecyclerView ProfileRecyclerView;
    private Dialog AllImagesDialog;
    private ImageButton ProfileSingleBackButton, ProfileSingleDeleteButton, ProfileSingleShareButton;
    private ProgressBar ProfileSingleProgressbar;
    private ImageView ProfileSinglePhotoView;
    private TextView ProfileSingleTextView;
    private int index=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        AllImagesDialogInit();

        TextView HeaderTextview = findViewById(R.id.profile_activity_toolbar).findViewById(R.id.mytoolbar_textview);
        HeaderTextview.setText("Profile");

        Ref = FirebaseDatabase.getInstance().getReference();
        Ref.keepSynced(true);
        ProfileImageView = findViewById(R.id.profile_activity_user_info_imageview);
        ProfileCommunityCountTextview = findViewById(R.id.profile_activity_community_count);
        ProfileNameTextview = findViewById(R.id.profile_activity_username);
        ProfileEmailTextview = findViewById(R.id.profile_activity_useremail);
        BackgroundServiceTextView = findViewById(R.id.profile_preference_background_service);
        BatteryOptimizationTextView = findViewById(R.id.profile_preference_battery_optimization);
        ProfileCommunityProgressbar = findViewById(R.id.profile_activity_user_info_progressbar);
        ProfileCommunityImageChangeButton = findViewById(R.id.profile_activity_user_info_imagebutton);
        ProfilebackButton = findViewById(R.id.profile_activity_toolbar).findViewById(R.id.mytoolbar_back_button);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        ProfileRecyclerView = findViewById(R.id.profile_activity_recyclerview);
        ProfileRecyclerView.setHasFixedSize(true);
        ProfileRecyclerView.setLayoutManager(new GridLayoutManager(ProfileActivity.this,getHighestInt(dpWidth/50)));

        AllCommunities = new ArrayList<>();
        AllImages = new ArrayList<>();

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            BatteryOptimizationTextView.setVisibility(View.GONE);
        }
        else if(pm.isIgnoringBatteryOptimizations(getPackageName()))
        {
            BatteryOptimizationTextView.setVisibility(View.GONE);
        }

        BatteryOptimizationTextView.setText("Battery optimization required for background services");

        if(Build.BRAND.equalsIgnoreCase("xiaomi") ){

            BackgroundServiceTextView.setText(Build.BRAND+" require auto start permission. Click to enable autostart permission for better user experience.");


        }else if(Build.BRAND.equalsIgnoreCase("Letv")){

            BackgroundServiceTextView.setText(Build.BRAND+" require auto start permission. Click to enable autostart permission for better user experience.");


        }
        else if(Build.BRAND.equalsIgnoreCase("Honor")){

            BackgroundServiceTextView.setText(Build.BRAND+" require auto start permission. Click to enable autostart permission for better user experience.");


        }
        else if(Build.BRAND.equalsIgnoreCase("vivo"))
        {
            BackgroundServiceTextView.setText(Build.BRAND+" require auto start permission. Click to enable autostart permission for better user experience.");

        }
        else  if (Build.MANUFACTURER.equalsIgnoreCase("oppo")) {

            BackgroundServiceTextView.setText(Build.BRAND+" require auto start permission. Click to enable autostart permission for better user experience.");

        }
        else
        {
            BatteryOptimizationTextView.setVisibility(View.GONE);
            BackgroundServiceTextView.setVisibility(View.GONE);
            findViewById(R.id.profile_preference_header).setVisibility(View.GONE);
            findViewById(R.id.profile_activity_line2).setVisibility(View.GONE);
        }

        BatteryOptimizationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    BatteryOptimizationTextView.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }

            }
        });

        BackgroundServiceTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(Build.BRAND.equalsIgnoreCase("xiaomi") ){

                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                    startActivity(intent);


                }else if(Build.BRAND.equalsIgnoreCase("Letv")){

                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
                    startActivity(intent);

                }
                else if(Build.BRAND.equalsIgnoreCase("Honor")){

                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
                    startActivity(intent);

                }
                else if(Build.BRAND.equalsIgnoreCase("vivo"))
                {
                    try {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName("com.iqoo.secure",
                                "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"));
                        startActivity(intent);
                    } catch (Exception e) {
                        try {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.vivo.permissionmanager",
                                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                            startActivity(intent);
                        } catch (Exception ex) {
                            try {
                                Intent intent = new Intent();
                                intent.setClassName("com.iqoo.secure",
                                        "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager");
                                startActivity(intent);
                            } catch (Exception exx) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
                else  if (Build.MANUFACTURER.equalsIgnoreCase("oppo")) {
                    try {
                        Intent intent = new Intent();
                        intent.setClassName("com.coloros.safecenter",
                                "com.coloros.safecenter.permission.startup.StartupAppListActivity");
                        startActivity(intent);
                    } catch (Exception e) {
                        try {
                            Intent intent = new Intent();
                            intent.setClassName("com.oppo.safe",
                                    "com.oppo.safe.permission.startup.StartupAppListActivity");
                            startActivity(intent);

                        } catch (Exception ex) {
                            try {
                                Intent intent = new Intent();
                                intent.setClassName("com.coloros.safecenter",
                                        "com.coloros.safecenter.startupapp.StartupAppListActivity");
                                startActivity(intent);
                            } catch (Exception exx) {

                            }
                        }
                    }
                }

            }
        });




        Ref.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                CCount = 0;
                ProfileCommunityProgressbar.setVisibility(View.VISIBLE);

                if (dataSnapshot.hasChild("Communities")) {
                    for (DataSnapshot snapshot : dataSnapshot.child("Communities").getChildren()) {
                        CCount++;
                    }

                    ProfileCommunityCountTextview.setText("Communities : "+CCount);
                } else {
                    ProfileCommunityCountTextview.setText("Communities : -NA-");
                }

                if (dataSnapshot.hasChild("Name")) {
                    String name = dataSnapshot.child("Name").getValue().toString();
                    ProfileNameTextview.setText(name);
                } else {
                    ProfileNameTextview.setText("-NA-");

                }

                if (dataSnapshot.hasChild("Email")) {
                    String email = dataSnapshot.child("Email").getValue().toString();
                    ProfileEmailTextview.setText(email);
                } else {
                    ProfileEmailTextview.setText("-NA-");

                }

                if (dataSnapshot.hasChild("Profile_picture")) {
                    String image = dataSnapshot.child("Profile_picture").getValue().toString();

                    if (!image.equals("default")) {

                        Glide.with(getApplicationContext()).load(image).apply(new RequestOptions().placeholder(R.drawable.ic_account_circle)).listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                ProfileCommunityProgressbar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                ProfileCommunityProgressbar.setVisibility(View.GONE);
                                return false;
                            }
                        }).into(ProfileImageView);


                    }
                    else
                    {
                        ProfileCommunityProgressbar.setVisibility(View.GONE);
                    }

                }
                else
                {
                    ProfileCommunityProgressbar.setVisibility(View.GONE);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ProfilebackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onBackPressed();

            }
        });

        ProfileCommunityImageChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (new PreOperationCheck().checkInternetConnectivity(getApplicationContext())) {
                    COVER_CHANGE = false;
                    PROFILE_CHANGE = true;
                    GetStartedWithNewProfileImage();
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to connect to internet. Try again.", Toast.LENGTH_SHORT).show();

                }

            }
        });

        ShowAllImages();

    }


    private void AllImagesDialogInit() {

        AllImagesDialog = new Dialog(ProfileActivity.this, android.R.style.Theme_Light_NoTitleBar);
        AllImagesDialog.setContentView(R.layout.profile_singleitem_layout);
        ProfileSinglePhotoView = AllImagesDialog.findViewById(R.id.single_community_imageview);
        ProfileSingleProgressbar = AllImagesDialog.findViewById(R.id.single_community_progressbar);
        ProfileSingleShareButton = AllImagesDialog.findViewById(R.id.single_community_share_imagebutton);
        ProfileSingleShareButton.setVisibility(View.GONE);
        ProfileSingleDeleteButton = AllImagesDialog.findViewById(R.id.single_community_delete_imagebutton);
        ProfileSingleDeleteButton.setVisibility(View.GONE);
        ProfileSingleBackButton = AllImagesDialog.findViewById(R.id.single_community_back_imagebutton);
        ProfileSingleTextView = AllImagesDialog.findViewById(R.id.single_community_uploadby_textview);
        ProfileSingleTextView.setVisibility(View.GONE);
        ProfileSingleBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllImagesDialog.dismiss();
            }
        });


    }

    private int getHighestInt(float v) {

        if (v - (int) v > .65) {
            int val = (int) v;
            return val + 1;
        } else {
            return (int) v;
        }
    }

    private void ShowAllImages() {

        Ref.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Communities").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                AllCommunities.clear();
                AllImages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    String key = snapshot.getKey();

                    Ref.child("Communities").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {


                            if(dataSnapshot.hasChild("posts"))
                            {
                                for (DataSnapshot snapshot : dataSnapshot.child("posts").getChildren())
                                {
                                    String uri = snapshot.child("uri").getValue().toString();
                                    AllImages.add(uri);
                                }
                            }

                            ImageAdapter adapter = new ImageAdapter();
                            ProfileRecyclerView.setAdapter(adapter);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        if(ProfileCommunityProgressbar.isShown())
        {
            Toast.makeText(getApplicationContext(), "Wait until image upload is complete.", Toast.LENGTH_SHORT).show();

        }
        else
        {
            super.onBackPressed();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);
            finish();

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && !COVER_CHANGE && PROFILE_CHANGE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {


                ProfileCommunityProgressbar.setVisibility(View.VISIBLE);

                Uri resultUri = result.getUri();
                try {
                    InputStream stream = getContentResolver().openInputStream(resultUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(stream);
                    ProfileImageView.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


                File thumb_filePath = new File(resultUri.getPath());
                final String current_u_i_d = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(100)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();


                final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profile_images").child(current_u_i_d + ".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();


                            FirebaseDatabase.getInstance().getReference().child("Users").child(current_u_i_d).child("Profile_picture").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        {
                                            Toast.makeText(ProfileActivity.this, "SUCCESSFULLY UPLOADED", Toast.LENGTH_LONG).show();
                                            ProfileCommunityProgressbar.setVisibility(View.GONE);


                                        }
                                    } else {
                                        ProfileCommunityProgressbar.setVisibility(View.GONE);
                                        Toast.makeText(ProfileActivity.this, "FAILED TO SAVE TO DATABASE.MAKE SURE YOUR INTERNET IS CONNECTED AND TRY AGAIN.", Toast.LENGTH_LONG).show();

                                    }

                                }
                            });
                        } else {
                            ProfileCommunityProgressbar.setVisibility(View.GONE);
                            Toast.makeText(ProfileActivity.this, "FAILED TO UPLOAD", Toast.LENGTH_LONG).show();

                        }
                    }


                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(ProfileActivity.this, "FAILED TO UPLOAD", Toast.LENGTH_LONG).show();
            }
        }

    }

    public void GetStartedWithNewProfileImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(ProfileActivity.this);
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.single_imageview,parent,false);
            return new ImageAdapter.ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ImageViewHolder holder, final int position) {

            holder.SingleProgressbar.setVisibility(View.VISIBLE);

            Glide.with(getApplicationContext()).load(AllImages.get(position)).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    holder.SingleProgressbar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    holder.SingleProgressbar.setVisibility(View.GONE);
                    return false;
                }
            }).into(holder.SingleImageView);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AllImagesDialog.show();
                    index = position;


                    ProfileSinglePhotoView.setScaleType(ImageView.ScaleType.CENTER);

                    final RequestOptions requestOptions = new RequestOptions()
                            .fitCenter().placeholder(R.drawable.ic_album_cover_image_default);

                    try
                    {
                        Glide.with(getApplicationContext())
                                .load(AllImages.get(position))
                                .apply(requestOptions)
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        ProfileSingleProgressbar.setVisibility(View.GONE);
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        ProfileSingleProgressbar.setVisibility(View.GONE);
                                        return false;
                                    }
                                })
                                .into(ProfileSinglePhotoView);
                    }
                    catch (IndexOutOfBoundsException e)
                    {
                        ShowAllImages();
                    }


                    ProfileSinglePhotoView.setOnTouchListener(new ProfileActivity.OnSwipeTouchListener(ProfileActivity.this) {
                        public void onSwipeRight() {

                            ProfileSingleProgressbar.setVisibility(View.VISIBLE);

                            try {
                                index = index - 1;
                                Glide.with(getApplicationContext())
                                        .load(AllImages.get(index))
                                        .apply(requestOptions)
                                        .listener(new RequestListener<Drawable>() {
                                            @Override
                                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                ProfileSingleProgressbar.setVisibility(View.GONE);
                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                ProfileSingleProgressbar.setVisibility(View.GONE);
                                                return false;
                                            }
                                        })
                                        .into(ProfileSinglePhotoView);

                            } catch (IndexOutOfBoundsException e) {
                                index = index + 1;
                                ProfileSingleProgressbar.setVisibility(View.GONE);
                                Toast.makeText(ProfileActivity.this, "first post.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        public void onSwipeLeft() {

                            ProfileSingleProgressbar.setVisibility(View.VISIBLE);

                            try {
                                index = index + 1;
                                Glide.with(getApplicationContext())
                                        .load(AllImages.get(index))
                                        .apply(requestOptions)
                                        .listener(new RequestListener<Drawable>() {
                                            @Override
                                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                ProfileSingleProgressbar.setVisibility(View.GONE);
                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                ProfileSingleProgressbar.setVisibility(View.GONE);
                                                return false;
                                            }
                                        })
                                        .into(ProfileSinglePhotoView);

                            } catch (IndexOutOfBoundsException e) {
                                index = index - 1;
                                ProfileSingleProgressbar.setVisibility(View.GONE);
                                Toast.makeText(ProfileActivity.this, "last post.", Toast.LENGTH_SHORT).show();
                            }
                        }

                    });



                }
            });

        }

        @Override
        public int getItemCount() {
            return AllImages.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder {

            ImageView SingleImageView;
            ProgressBar SingleProgressbar;

            public ImageViewHolder(View itemView) {
                super(itemView);

                SingleImageView = itemView.findViewById(R.id.single_imageview);
                SingleProgressbar =itemView.findViewById(R.id.single_imageview_progressbar);
            }
        }
    }

    public class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;

        public OnSwipeTouchListener(Context ctx) {
            gestureDetector = new GestureDetector(ctx, new ProfileActivity.OnSwipeTouchListener.GestureListener());
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
                    } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
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
