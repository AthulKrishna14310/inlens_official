package com.integrals.inlens.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.integrals.inlens.Database.UploadQueueDB;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.DirectoryFragment;
import com.integrals.inlens.Helper.FirebaseConstants;
import com.integrals.inlens.Models.GalleryImageModel;
import com.integrals.inlens.Notification.NotificationHelper;
import com.integrals.inlens.R;
import com.integrals.inlens.WorkManager.UploadWorker;
import com.skyfishjy.library.RippleBackground;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import id.zelory.compressor.Compressor;

public class InlensGalleryActivity extends AppCompatActivity implements DirectoryFragment.ResumeCallback {

    private static final int PORTRAIT = 0;
    private static final int LANDSCAPE = 1;

    private List<GalleryImageModel> allCommunityImages;
    private RecyclerView galleryGridRecyclerView;
    private FloatingActionButton galleryUploadFab;
    int PROGRESS_CURRENT = 0;
    private List<String> allImagesInCurrentCommunity;
    private ImageButton galleryBackButton, galleryInfoButton;
    private TextView galleyHeaderTextView;
    private DatabaseReference currentUserRef, postRef;
    private StorageReference storageRef;


    private String communityID = AppConstants.NOT_AVALABLE;
    private String communityStartTime = AppConstants.NOT_AVALABLE;

    String currentUserId;
    ImageAdapter imageAdapter;
    int imagesToUpload = 0;
    RelativeLayout rootGalleryRelativeLayout;
    SwipeRefreshLayout gallerySwipeRefresh;
    ImageButton dirSelectionButton;
    DirectoryFragment directoryFragment;
    private RelativeLayout RootForMainActivity;
    private boolean snack = false;


    @SuppressLint("CutPasteId")
    String appTheme = "";
    List<GalleryImageModel> imagesQueue = new ArrayList<>();

    UploadQueueDB uploadQueueDB;
    int queuedCount = 0;
    RelativeLayout gallerySharedLoadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences appDataPref = getSharedPreferences(AppConstants.appDataPref, Context.MODE_PRIVATE);
        final SharedPreferences.Editor appDataPrefEditor = appDataPref.edit();
        if (appDataPref.contains(AppConstants.appDataPref_theme)) {
            appTheme = appDataPref.getString(AppConstants.appDataPref_theme, AppConstants.themeLight);
            if (appTheme.equals(AppConstants.themeLight)) {
                setTheme(R.style.AppTheme);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                setTheme(R.style.DarkTheme);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        } else {
            appTheme = AppConstants.themeLight;
            appDataPrefEditor.putString(AppConstants.appDataPref_theme, AppConstants.themeLight);
            appDataPrefEditor.commit();
            setTheme(R.style.AppThemeGalleryLight);

        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inlens_gallery);

        imagesToUpload = 0;
        gallerySharedLoadingLayout = findViewById(R.id.gallery_shared_image_loading_layout);
        uploadQueueDB = new UploadQueueDB(this);


        rootGalleryRelativeLayout = findViewById(R.id.root_for_gallery);
        gallerySwipeRefresh = findViewById(R.id.gallerySwipeRefreshLayout);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        allCommunityImages = new ArrayList<>();
        allImagesInCurrentCommunity = new ArrayList<>();


        postRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.POSTS);
        currentUserRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference().child(FirebaseConstants.COMMUNITIES_STORAGE);

        galleryBackButton = findViewById(R.id.gallery_toolbar).findViewById(R.id.mytoolbar_back_button);
        galleyHeaderTextView = findViewById(R.id.gallery_toolbar).findViewById(R.id.mytoolbar_textview);
        galleyHeaderTextView.setText("Your Gallery");
        galleryInfoButton = findViewById(R.id.gallery_toolbar).findViewById(R.id.mytoolbar_info_button);
        dirSelectionButton = findViewById(R.id.gallery_toolbar).findViewById(R.id.mytoolbar_dir_options);

        galleryBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onBackPressed();

            }
        });

        directoryFragment = new DirectoryFragment(InlensGalleryActivity.this);
        dirSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                directoryFragment.show(getSupportFragmentManager(), directoryFragment.getTag());

            }
        });

        galleryGridRecyclerView = findViewById(R.id.gallery_recyclerview);
        galleryGridRecyclerView.setHasFixedSize(true);

        gallerySwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                gallerySwipeRefresh.setRefreshing(true);
                initGallery();
            }
        });

        galleryInfoButton.setVisibility(View.VISIBLE);
        galleryInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View customLayout = getLayoutInflater().inflate(R.layout.dialog_layout_inlens_gallery, null);

                new AlertDialog.Builder(InlensGalleryActivity.this)
                        .setMessage(" ")
                        .setPositiveButton("Ok, I understand", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                dialogInterface.dismiss();

                            }
                        })
                        .setView(customLayout)
                        .setCancelable(true)
                        .create()
                        .show();
            }
        });


        galleryUploadFab = findViewById(R.id.gallery_upload_fab);

        galleryUploadFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (imageAdapter != null) {
                    List<GalleryImageModel> imageList = imageAdapter.getImageList();
                    galleryUploadFab.hide();
                    imagesToUpload = 0;
                    for (int i = 0; i < imageList.size(); i++) {
                        File imgFile = new File(imageList.get(i).getImageUri());
//                        Log.i("imgFile","imgFile "+imgFile);
                        if(imgFile.exists())
                        {
                            if (imageList.get(i).isSelected() && uploadQueueDB.insertData(Uri.fromFile(imgFile).getLastPathSegment(),imgFile.toString(),String.valueOf(imgFile.lastModified()))) {
                                imagesToUpload++;

                            }
                        }
                        else
                        {
                            imageAdapter.notifyItemRemoved(i);
                        }

                    }

//                    Log.i("galleryS","imagesToUpload "+imagesToUpload);
//                    Log.i("galleryS","allCommunityImages "+imageList.size());

                    if (imagesToUpload > 0) {

                        SharedPreferences notificationPref = getSharedPreferences(AppConstants.NOTIFICATION_PREF,Context.MODE_PRIVATE);
                        SharedPreferences.Editor notificationPrefEditor  = notificationPref.edit();
                        notificationPrefEditor.putString("id",String.valueOf(System.currentTimeMillis()));
                        notificationPrefEditor.commit();

                        Constraints uploadConstraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
                        OneTimeWorkRequest galleryUploader = new OneTimeWorkRequest.Builder(UploadWorker.class).addTag("uploadWorker").setConstraints(uploadConstraints).build();
                        WorkManager.getInstance(InlensGalleryActivity.this).cancelAllWorkByTag("uploadWorker");
                        WorkManager.getInstance(InlensGalleryActivity.this).enqueueUniqueWork("uploadWorker", ExistingWorkPolicy.REPLACE, galleryUploader);

                        SetGalleryImages setGalleryImages = new SetGalleryImages();
                        if (setGalleryImages.getStatus() != AsyncTask.Status.RUNNING) {
                            setGalleryImages.execute();
                        }

                    }

                    // todo remove qued images from gallery
                    Snackbar.make(rootGalleryRelativeLayout, "Queued " + imagesToUpload + " image.", BaseTransientBottomBar.LENGTH_SHORT).setAction("Learn more", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Toast.makeText(InlensGalleryActivity.this, "open link-> help@learnMore", Toast.LENGTH_SHORT).show();
                        }
                    }).show();

                }

            }
        });

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null && type.startsWith("image/")) {
            handleSingleImage(intent);

        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null && type.startsWith("image/")) {

            gallerySharedLoadingLayout.setVisibility(View.VISIBLE);
            handleMultipleImages(intent);

        } else {
            communityID = intent.getStringExtra("CommunityID");
            communityStartTime = intent.getStringExtra("CommunityStartTime");

            if (communityStartTime == null || communityStartTime.equals(AppConstants.NOT_AVALABLE)) {
                SharedPreferences CurrentActiveCommunity = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
                communityStartTime = CurrentActiveCommunity.getString("startAt", String.valueOf(System.currentTimeMillis()));
                communityID = CurrentActiveCommunity.getString("id", AppConstants.NOT_AVALABLE);
                onResume();
            } else {
                onResume();

            }
        }


    }

    public void initGallery() {

        gallerySwipeRefresh.setRefreshing(true);

        currentUserRef.child(FirebaseConstants.USERS).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(FirebaseConstants.LIVECOMMUNITYID)) {
                    communityID = dataSnapshot.child(FirebaseConstants.LIVECOMMUNITYID).getValue().toString();
                    communityStartTime = dataSnapshot.child(FirebaseConstants.COMMUNITIES).child(communityID).getValue().toString();

                    postRef.child(communityID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            allImagesInCurrentCommunity.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.hasChild(FirebaseConstants.POSTURL)) {
                                    String uri = snapshot.child(FirebaseConstants.POSTURL).getValue().toString();
                                    if (!allImagesInCurrentCommunity.contains(uri)) {
                                        allImagesInCurrentCommunity.add(uri);
                                    }
                                }
                            }
                            SetGalleryImages setGalleryImages = new SetGalleryImages();
                            if (setGalleryImages.getStatus() != AsyncTask.Status.RUNNING) {
                                setGalleryImages.execute();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void handleSingleImage(Intent intent) {

        queuedCount = 0;
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        String[] projection = {MediaStore.Images.Media.DATA};
        File imgFile = new File(getFilePathFromUri(projection, imageUri));

        SharedPreferences CurrentActiveCommunity = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
        if (CurrentActiveCommunity.contains("startAt") && CurrentActiveCommunity.contains("id")) {
            communityStartTime = CurrentActiveCommunity.getString("startAt", String.valueOf(System.currentTimeMillis()));
            communityID = CurrentActiveCommunity.getString("id", AppConstants.NOT_AVALABLE);
            String communityEndTime = CurrentActiveCommunity.getString("stopAt", AppConstants.NOT_AVALABLE);
            if (!communityEndTime.equals(AppConstants.NOT_AVALABLE) && System.currentTimeMillis() < Long.parseLong(communityEndTime)) {
                postRef.child(communityID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        allImagesInCurrentCommunity.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (snapshot.hasChild(FirebaseConstants.POSTURL)) {
                                String uri = snapshot.child(FirebaseConstants.POSTURL).getValue().toString();
                                String by = snapshot.child(FirebaseConstants.POSTBY).getValue().toString();
                                if (!allImagesInCurrentCommunity.contains(uri) && by.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    allImagesInCurrentCommunity.add(uri);
                                }
                            }
                        }
                        Cursor c = uploadQueueDB.getQueuedData();
                        while (c.moveToNext()) {
                            if (!allImagesInCurrentCommunity.contains(c.getString(1))) {
                                allImagesInCurrentCommunity.add(c.getString(1));

                            }
                        }
                        c.close();
                        if (imgFile.lastModified() > Long.parseLong(communityStartTime) && ImageNotAlreadyUploaded(Uri.fromFile(imgFile).getLastPathSegment())) {
                            if (uploadQueueDB.insertData(Uri.fromFile(imgFile).getLastPathSegment(), getFilePathFromUri(projection, imageUri).toString(), String.valueOf(System.currentTimeMillis()))) {
                                queuedCount++;
                            }
                        }
                        if (queuedCount > 0) {

                            SharedPreferences notificationPref = getSharedPreferences(AppConstants.NOTIFICATION_PREF,Context.MODE_PRIVATE);
                            SharedPreferences.Editor notificationPrefEditor  = notificationPref.edit();
                            notificationPrefEditor.putString("id",String.valueOf(System.currentTimeMillis()));
                            notificationPrefEditor.commit();

                            Constraints uploadConstraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
                            OneTimeWorkRequest galleryUploader = new OneTimeWorkRequest.Builder(UploadWorker.class).addTag("uploadWorker").setConstraints(uploadConstraints).build();
                            WorkManager.getInstance(InlensGalleryActivity.this).cancelAllWorkByTag("uploadWorker");
                            WorkManager.getInstance(InlensGalleryActivity.this).enqueueUniqueWork("uploadWorker", ExistingWorkPolicy.REPLACE, galleryUploader);

                        }

                        // todo remove qued images from gallery
                        Snackbar.make(rootGalleryRelativeLayout, "Queued " + queuedCount + " image.", BaseTransientBottomBar.LENGTH_SHORT).setAction("Learn more", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Toast.makeText(InlensGalleryActivity.this, "open link-> help@learnMore", Toast.LENGTH_SHORT).show();                            }
                        }).show();
                        initGallery();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            } else {
                Snackbar.make(rootGalleryRelativeLayout, "Cloud-album has expired. Create a new one and upload more.", BaseTransientBottomBar.LENGTH_SHORT).show();

            }

//        Log.i("gallerySend","id"+communityID);
//        Log.i("gallerySend","time"+communityStartTime);


        } else {
            Snackbar.make(rootGalleryRelativeLayout, "You have no active album at the moment.", BaseTransientBottomBar.LENGTH_SHORT).show();
        }

//        Log.i("gallerySend","input stream "+imageUri.getPath());
//        Log.i("gallerySend","path "+getFilePathFromUri(projection,imageUri));


    }


    public class HandleQueuedImages extends AsyncTask<Void, Void, Integer> {
        String[] projection;
        List<Uri> imageUris;

        public HandleQueuedImages(List<Uri> imageUris) {
            this.projection = new String[]{MediaStore.Images.Media.DATA};
            this.imageUris = imageUris;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            Cursor c = uploadQueueDB.getQueuedData();
            while (c.moveToNext()) {
                if (!allImagesInCurrentCommunity.contains(c.getString(0))) {
                    allImagesInCurrentCommunity.add(c.getString(0));

                }
            }
            c.close();
            for (Uri imageUri : imageUris) {
                File imgFile = new File(getFilePathFromUri(projection, imageUri));
//                Log.i("imgFile","imgFile in share "+imgFile);
                if (imgFile.lastModified() > Long.parseLong(communityStartTime) && ImageNotAlreadyUploaded(Uri.fromFile(imgFile).getLastPathSegment())) {

                    if (uploadQueueDB.insertData(Uri.fromFile(imgFile).getLastPathSegment(), imgFile.toString(), String.valueOf(imgFile.lastModified()))) {
                        queuedCount++;

                    }

                }

            }
            if (queuedCount > 0) {

                SharedPreferences notificationPref = getSharedPreferences(AppConstants.NOTIFICATION_PREF,Context.MODE_PRIVATE);
                SharedPreferences.Editor notificationPrefEditor  = notificationPref.edit();
                notificationPrefEditor.putString("id",String.valueOf(System.currentTimeMillis()));
                notificationPrefEditor.commit();

                Constraints uploadConstraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
                OneTimeWorkRequest galleryUploader = new OneTimeWorkRequest.Builder(UploadWorker.class).addTag("uploadWorker").setConstraints(uploadConstraints).build();
                WorkManager.getInstance(InlensGalleryActivity.this).cancelAllWorkByTag("uploadWorker");
                WorkManager.getInstance(InlensGalleryActivity.this).enqueueUniqueWork("uploadWorker", ExistingWorkPolicy.REPLACE, galleryUploader);

            }

            return queuedCount;
        }

        @Override
        protected void onPostExecute(Integer queuedCount) {
            super.onPostExecute(queuedCount);


            //todo update the time in sharedpref to match with the system current time to avoid notifications about already updated images.
            gallerySharedLoadingLayout.setVisibility(View.GONE);
            initGallery();

            Snackbar.make(rootGalleryRelativeLayout, "Queued " + queuedCount + " images.", BaseTransientBottomBar.LENGTH_SHORT).setAction("Learn more", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Snackbar.make(rootGalleryRelativeLayout, imageUris.size() - queuedCount + " images skipped in order to avoid copies.", BaseTransientBottomBar.LENGTH_LONG).show();

                }
            }).show();
        }
    }

    private void handleMultipleImages(Intent intent) {

        queuedCount = 0;
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);

        if (imageUris != null) {
            SharedPreferences CurrentActiveCommunity = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
            if (CurrentActiveCommunity.contains("startAt") && CurrentActiveCommunity.contains("id")) {
                communityStartTime = CurrentActiveCommunity.getString("startAt", String.valueOf(System.currentTimeMillis()));
                communityID = CurrentActiveCommunity.getString("id", AppConstants.NOT_AVALABLE);
                String communityEndTime = CurrentActiveCommunity.getString("stopAt", AppConstants.NOT_AVALABLE);
                if (!communityEndTime.equals(AppConstants.NOT_AVALABLE) && System.currentTimeMillis() < Long.parseLong(communityEndTime)) {
                    postRef.child(communityID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            allImagesInCurrentCommunity.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.hasChild(FirebaseConstants.POSTURL)) {
                                    String uri = snapshot.child(FirebaseConstants.POSTURL).getValue().toString();
                                    String by = snapshot.child(FirebaseConstants.POSTBY).getValue().toString();
                                    if (!allImagesInCurrentCommunity.contains(uri) && by.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                        allImagesInCurrentCommunity.add(uri);
                                    }
                                }
                            }
                            gallerySharedLoadingLayout.setVisibility(View.GONE);
                            new AlertDialog.Builder(InlensGalleryActivity.this)
                                    .setMessage("Are you sure you want to upload these photos ?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            gallerySharedLoadingLayout.setVisibility(View.VISIBLE);

                                            HandleQueuedImages handleQueuedImages = new HandleQueuedImages(imageUris);
                                            if (handleQueuedImages.getStatus() != AsyncTask.Status.RUNNING) {
                                                handleQueuedImages.execute();
                                            }

                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            dialogInterface.dismiss();
                                            initGallery();

                                        }
                                    })
                                    .show();


//                            Log.i("galleryS","communityStartTime "+communityStartTime);
//                            Log.i("galleryS","communityID "+communityID);
//                            Log.i("galleryS","communityEndTime "+communityEndTime);

//                            while(c.moveToNext())
//                            {
//                                Log.i("galleryS","row "+c.getString(0)+c.getString(1)+c.getString(2));
//                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                } else {
                    Snackbar.make(rootGalleryRelativeLayout, "Cloud-album has expired. Create a new one and upload more.", BaseTransientBottomBar.LENGTH_SHORT).show();

                }


            } else {
                Snackbar.make(rootGalleryRelativeLayout, "You have no active album at the moment.", BaseTransientBottomBar.LENGTH_SHORT).show();
            }

        }
    }


    public String getFilePathFromUri(String[] projection, Uri uri) {
        Cursor c = null;
        try {
            c = getContentResolver().query(uri, projection, null, null, null);
            int columnIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            c.moveToFirst();
            return c.getString(columnIndex);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent().getAction() == null) {
            initGallery();

        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    private boolean ImageNotAlreadyUploaded(String lastsegmentedpath) {

        for (int i = 0; i < allImagesInCurrentCommunity.size(); i++) {
            if (allImagesInCurrentCommunity.get(i).toLowerCase().contains(lastsegmentedpath.toLowerCase())) {
                return false;
            }

        }
        return true;

    }

    @Override
    public void reloadData() {

        if (directoryFragment != null) {
            directoryFragment.dismiss();
        }
        onResume();


    }

    public class EmptyGridViewHolder extends RecyclerView.ViewHolder {
        public EmptyGridViewHolder(View itemView) {
            super(itemView);

        }
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView galleryImage;
        ImageButton galleryItemSelectedButton;

        public ImageViewHolder(View itemView) {
            super(itemView);

            galleryImage = itemView.findViewById(R.id.gallery_item_imageview);
            galleryItemSelectedButton = itemView.findViewById(R.id.gallery_item_selected_imagebutton);

        }
    }

    private class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;
        private List<GalleryImageModel> ImageList;
        private int VIEW_TYPE_EMPTY = 1, VIEW_TYPE_IMAGE = 0;

        public ImageAdapter(Context context, List<GalleryImageModel> imageList) {
            this.context = context;
            ImageList = imageList;

        }

        public List<GalleryImageModel> getImageList() {
            return this.ImageList;
        }

        @Override
        public int getItemViewType(int position) {
            return ImageList.get(position) == null ? VIEW_TYPE_EMPTY : VIEW_TYPE_IMAGE;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_IMAGE) {
                return new ImageViewHolder(LayoutInflater.from(context).inflate(R.layout.gallery_item_card, parent, false));

            } else if (viewType == VIEW_TYPE_EMPTY) {
                View view = LayoutInflater.from(context).inflate(R.layout.empty_layout, parent, false);
                view.findViewById(R.id.CloseButtonTextView).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
                        notificationHelper.displayAlbumStartNotification("Go to Gallery.", "After taking photos tap here to upload.");
                        Intent cameraIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                        startActivity(cameraIntent);
                        finishAffinity();


                    }
                });
                return new EmptyGridViewHolder(view);
            }

            return null;
        }


        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {


            if (holder instanceof ImageViewHolder) {
                ImageViewHolder viewHolder = (ImageViewHolder) holder;

                Glide.with(context).load(ImageList.get(position).getImageUri()).apply(new RequestOptions().placeholder(R.drawable.ic_photo_camera).centerCrop()).into(viewHolder.galleryImage);
                if (ImageList.get(position).isSelected()) {

                    viewHolder.galleryImage.setColorFilter(Color.argb(155, 185, 185, 185), PorterDuff.Mode.SRC_ATOP);
                    viewHolder.galleryItemSelectedButton.setVisibility(View.VISIBLE);
                    checkFabVisibility();

                } else {
                    viewHolder.galleryImage.clearColorFilter();
                    viewHolder.galleryItemSelectedButton.setVisibility(View.GONE);
                    checkFabVisibility();

                }

                viewHolder.galleryImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        if (ImageList.get(position).isSelected()) {
                            ImageList.get(position).setSelected(false);
                            imageAdapter.notifyItemChanged(position);
                            checkFabVisibility();

                        } else {
                            ImageList.get(position).setSelected(true);
                            imageAdapter.notifyItemChanged(position);
                            checkFabVisibility();

                        }


                    }
                });

                viewHolder.galleryItemSelectedButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (ImageList.get(position).isSelected()) {
                            ImageList.get(position).setSelected(false);
                            imageAdapter.notifyItemChanged(position);
                            checkFabVisibility();

                        } else {
                            ImageList.get(position).setSelected(true);
                            imageAdapter.notifyItemChanged(position);
                            checkFabVisibility();

                        }

                    }
                });

            }
        }


        private void checkFabVisibility() {

            try {
                for (int k = 0; k < ImageList.size(); k++) {
                    if (ImageList.get(k).isSelected()) {
                        galleryUploadFab.show();
                        galleryUploadFab.show();
                        break;
                    } else {
                        if (k == ImageList.size() - 1) {
                            galleryUploadFab.hide();
                            galleryUploadFab.hide();

                        }
                    }
                }
            } catch (Exception e) {
                Log.i("gallery", "exception " + e);
            }
        }

        @Override
        public int getItemCount() {
            return ImageList.size();
        }


    }


    public Bitmap compressUploadFile(File bitmapFile) {
        try {
            Bitmap result = new Compressor(InlensGalleryActivity.this)
                    .compressToBitmap(bitmapFile);
            if (orientation(result) == PORTRAIT) {
                result = new Compressor(InlensGalleryActivity.this)
                        .setQuality(90)
                        .setMaxHeight(640)
                        .setMaxWidth(480)
                        .compressToBitmap(bitmapFile);

            } else {
                result = new Compressor(InlensGalleryActivity.this)
                        .setQuality(90)
                        .setMaxHeight(480)
                        .setMaxWidth(640)
                        .compressToBitmap(bitmapFile);
            }

            return result;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    private int orientation(Bitmap result) {

        if (result.getHeight() > result.getWidth()) {
            return PORTRAIT;
        } else {
            return LANDSCAPE;
        }

    }

    public class SetGalleryImages extends AsyncTask<Void, Void, List<GalleryImageModel>> {

        public SetGalleryImages() {
            super();
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SharedPreferences LastShownNotificationInfo = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
            if (LastShownNotificationInfo.contains("time")) {
                SharedPreferences.Editor editor = LastShownNotificationInfo.edit();
                editor.putString("time", String.valueOf(System.currentTimeMillis()));
                editor.commit();
            }


            Log.i("galleryS", "started setGallery execution  preexec");
        }

        @Override
        protected List<GalleryImageModel> doInBackground(Void... voids) {

            Cursor c = uploadQueueDB.getQueuedData();
            while (c.moveToNext()) {
                allImagesInCurrentCommunity.add(c.getString(0));
            }
            c.close();
            Uri uri;
            Cursor cursor;
            int column_index_data;
            List<String> listOfAllImages = new ArrayList<>();
            List<String> lastmodifieddate = new ArrayList<>();
            List<GalleryImageModel> AllImagesList = new ArrayList<>();

            String absolutePathOfImage = null;
            uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            String[] projection = {MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

            cursor = getContentResolver().query(uri, projection, null,
                    null, null);

            SharedPreferences dirPreference = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
            String directories = dirPreference.getString(AppConstants.SELECTED_DIRECTORIES, "");

            try {

                column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                while (cursor.moveToNext()) {
                    absolutePathOfImage = cursor.getString(column_index_data);
                    String[] pathSegments = absolutePathOfImage.split("/");
                    if (pathSegments.length > 4) {
                        if (!directories.contains(pathSegments[4].toLowerCase())) {
                            File img = new File(absolutePathOfImage);
                            //if (img.lastModified() > starttime && !absolutePathOfImage.toLowerCase().contains("screenshot") && !absolutePathOfImage.toLowerCase().contains("whatsapp")) {
                            if (img.lastModified() > Long.parseLong(communityStartTime)) {

                                String lastsegmentedpath = Uri.fromFile(new File(absolutePathOfImage)).getLastPathSegment();

                                if (!listOfAllImages.contains(lastsegmentedpath) && ImageNotAlreadyUploaded(lastsegmentedpath)) {

                                    listOfAllImages.add(absolutePathOfImage);
                                    lastmodifieddate.add(String.valueOf(img.lastModified()));

                                }


                            }
                        }
                    }


                }


                for (int i = 0; i < listOfAllImages.size(); i++) {
                    AllImagesList.add(new GalleryImageModel(listOfAllImages.get(i), false, false, lastmodifieddate.get(i)));
                }
                cursor.close();
                return AllImagesList;

            } catch (NumberFormatException e) {
                Log.i("galleryS", "exception" + e);

                SharedPreferences CurrentActiveCommunity = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
                String startTime = CurrentActiveCommunity.getString("startAt", String.valueOf(System.currentTimeMillis()));

                column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

                while (cursor.moveToNext()) {
                    absolutePathOfImage = cursor.getString(column_index_data);
                    String[] pathSegments = absolutePathOfImage.split("/");
                    if (pathSegments.length > 4) {
                        if (!directories.contains(pathSegments[4].toLowerCase())) {
                            File img = new File(absolutePathOfImage);
                            //if (img.lastModified() > starttime && !absolutePathOfImage.toLowerCase().contains("screenshot") && !absolutePathOfImage.toLowerCase().contains("whatsapp")) {
                            if (img.lastModified() > Long.parseLong(startTime)) {

                                String lastsegmentedpath = Uri.fromFile(new File(absolutePathOfImage)).getLastPathSegment();

                                if (!listOfAllImages.contains(lastsegmentedpath) && ImageNotAlreadyUploaded(lastsegmentedpath)) {

                                    listOfAllImages.add(absolutePathOfImage);
                                    lastmodifieddate.add(String.valueOf(img.lastModified()));

                                }


                            }
                        }
                    }


                }


                for (int i = 0; i < listOfAllImages.size(); i++) {
                    AllImagesList.add(new GalleryImageModel(listOfAllImages.get(i), false, false, lastmodifieddate.get(i)));
                }
                cursor.close();
                return AllImagesList;

            } catch (Exception e) {
                cursor.close();
                Log.i("galleryS", "exception" + e);
                return AllImagesList;
            }

        }

        @Override
        protected void onPostExecute(List<GalleryImageModel> allCommunityImages) {
            super.onPostExecute(allCommunityImages);

            Log.i("galleryS", "onPostExecute  " + allCommunityImages.size());


            if (allCommunityImages.size() == 0) {
                final RippleBackground rippleBackground = (RippleBackground) findViewById(R.id.content);
                rippleBackground.startRippleAnimation();
                allCommunityImages.add(null);
                galleryGridRecyclerView.setLayoutManager(new GridLayoutManager(InlensGalleryActivity.this, 1));
                imageAdapter = new ImageAdapter(InlensGalleryActivity.this, allCommunityImages);
                galleryGridRecyclerView.removeAllViews();
                galleryGridRecyclerView.setAdapter(imageAdapter);
                galleryUploadFab.hide();

            } else {
                galleryGridRecyclerView.setLayoutManager(new GridLayoutManager(InlensGalleryActivity.this, 3));
                Collections.reverse(allCommunityImages);
                imageAdapter = new ImageAdapter(InlensGalleryActivity.this, allCommunityImages);
                galleryGridRecyclerView.removeAllViews();
                galleryGridRecyclerView.setAdapter(imageAdapter);

                if (!snack) {
                    snack = true;
                }


            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    gallerySwipeRefresh.setRefreshing(false);

                }
            }, 2000);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uploadQueueDB.close();
    }
}