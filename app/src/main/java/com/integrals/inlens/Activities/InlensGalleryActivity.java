package com.integrals.inlens.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.integrals.inlens.Database.UploadQueueDB;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.DirectoryFragment;
import com.integrals.inlens.Helper.FirebaseConstants;
import com.integrals.inlens.Helper.PreOperationCheck;
import com.integrals.inlens.Helper.ReadFirebaseData;
import com.integrals.inlens.Interface.FirebaseRead;
import com.integrals.inlens.Models.GalleryImageModel;
import com.integrals.inlens.Notification.NotificationHelper;
import com.integrals.inlens.R;
import com.integrals.inlens.WorkManager.UploadWorker;
import com.skyfishjy.library.RippleBackground;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    boolean isUploading = false;
    RelativeLayout rootGalleryRelativeLayout;
    SwipeRefreshLayout gallerySwipeRefresh;
    ImageButton dirSelectionButton;
    DirectoryFragment directoryFragment;
    private RelativeLayout RootForMainActivity;
    private boolean snack = false;

    int imgPosition, imgCount;
    Dialog uploadDialog;
    ProgressBar uploadProgressbar;
    TextView uploadTextView, uploadPorgressTextView;
    ImageView uploadImageview;

    @SuppressLint("CutPasteId")
    String appTheme = "";
    List<GalleryImageModel> imagesQueue = new ArrayList<>();

    UploadQueueDB uploadQueueDB;
    int queuedCount = 0;

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
        imgCount = 0;

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
                displayImagesBasedOnTime(communityID, communityStartTime);

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

                galleryUploadFab.hide();
                imagesToUpload = 0;
                for (int i = 0; i < allCommunityImages.size(); i++) {
                    if (allCommunityImages.get(i).isSelected()) {
                        imagesToUpload++;
                        allCommunityImages.get(i).setQueued(true);
                        imageAdapter.notifyItemChanged(i);
                    }
                }

                if (imagesToUpload > 0) {
                    isUploading = true;
                    imageAdapter.notifyDataSetChanged();
                    imgCount = 0;
                    uploadDialog = new Dialog(InlensGalleryActivity.this);
                    uploadDialog.setContentView(R.layout.gallery_upload_item_dialog);
                    uploadDialog.setCancelable(false);
                    uploadDialog.setCanceledOnTouchOutside(false);
                    uploadDialog.getWindow().getAttributes().windowAnimations = R.style.BottomUpSlideDialogAnimation;

                    Window window = uploadDialog.getWindow();
                    window.setGravity(Gravity.BOTTOM);
                    window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    window.setDimAmount(0.75f);
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                    uploadProgressbar = uploadDialog.findViewById(R.id.gallery_upload_dialog_progressbar);
                    uploadTextView = uploadDialog.findViewById(R.id.gallery_upload_dialog_textview);
                    uploadPorgressTextView = uploadDialog.findViewById(R.id.gallery_upload_dialog_progress_textview);
                    uploadImageview = uploadDialog.findViewById(R.id.gallery_upload_dialog_imageview);

                    uploadDialog.show();
                    firebaseUploader(allCommunityImages);
                }
            }
        });

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null && type.startsWith("image/")) {
            handleSingleImage(intent);

        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null && type.startsWith("image/")) {
            handleMultipleImages(intent);

        } else {
            communityID = intent.getStringExtra("CommunityID");
            communityStartTime = intent.getStringExtra("CommunityStartTime");

            if (communityStartTime == null || communityStartTime.equals(AppConstants.NOT_AVALABLE)) {
                SharedPreferences CurrentActiveCommunity = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
                communityStartTime = CurrentActiveCommunity.getString("startAt", String.valueOf(System.currentTimeMillis()));
                communityID = CurrentActiveCommunity.getString("id", AppConstants.NOT_AVALABLE);
            }
        }


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
            if( !communityEndTime.equals(AppConstants.NOT_AVALABLE) && System.currentTimeMillis()<Long.parseLong(communityEndTime))
            {
                postRef.child(communityID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        allImagesInCurrentCommunity.clear();
                        for (DataSnapshot snapshot:dataSnapshot.getChildren())
                        {
                            if(snapshot.hasChild(FirebaseConstants.POSTURL))
                            {
                                String uri=snapshot.child(FirebaseConstants.POSTURL).getValue().toString();
                                String by = snapshot.child(FirebaseConstants.POSTBY).getValue().toString();
                                if(!allImagesInCurrentCommunity.contains(uri) && by.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                                {
                                    allImagesInCurrentCommunity.add(uri);
                                }
                            }
                        }
                        if (imgFile.lastModified() > Long.parseLong(communityStartTime) && ImageNotAlreadyUploaded(Uri.fromFile(imgFile).getLastPathSegment())) {
                            if (uploadQueueDB.insertData(Uri.fromFile(imgFile).getLastPathSegment(), getFilePathFromUri(projection, imageUri).toString(), String.valueOf(System.currentTimeMillis()))) {
                                queuedCount++;
                            }
                        }
                        if(queuedCount>0)
                        {
                            Constraints uploadConstraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
                            OneTimeWorkRequest galleryUploader = new OneTimeWorkRequest.Builder(UploadWorker.class).setConstraints(uploadConstraints).build();
                            WorkManager.getInstance(InlensGalleryActivity.this).enqueue(galleryUploader);

                        }
                        Snackbar.make(rootGalleryRelativeLayout, "Queued " + queuedCount + " image.", BaseTransientBottomBar.LENGTH_SHORT).setAction("Upload now", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Toast.makeText(InlensGalleryActivity.this, "initiate work manager now", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
            else
            {
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

    private void handleMultipleImages(Intent intent) {

        queuedCount = 0;
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        String[] projection = {MediaStore.Images.Media.DATA};

        if (imageUris != null) {
            SharedPreferences CurrentActiveCommunity = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
            if (CurrentActiveCommunity.contains("startAt") && CurrentActiveCommunity.contains("id")) {
                communityStartTime = CurrentActiveCommunity.getString("startAt", String.valueOf(System.currentTimeMillis()));
                communityID = CurrentActiveCommunity.getString("id", AppConstants.NOT_AVALABLE);
                String communityEndTime = CurrentActiveCommunity.getString("stopAt", AppConstants.NOT_AVALABLE);
                if( !communityEndTime.equals(AppConstants.NOT_AVALABLE) && System.currentTimeMillis()<Long.parseLong(communityEndTime))
                {
                    postRef.child(communityID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            allImagesInCurrentCommunity.clear();
                            for (DataSnapshot snapshot:dataSnapshot.getChildren())
                            {
                                if(snapshot.hasChild(FirebaseConstants.POSTURL))
                                {
                                    String uri=snapshot.child(FirebaseConstants.POSTURL).getValue().toString();
                                    String by = snapshot.child(FirebaseConstants.POSTBY).getValue().toString();
                                    if(!allImagesInCurrentCommunity.contains(uri) && by.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                                    {
                                        allImagesInCurrentCommunity.add(uri);
                                    }
                                }
                            }
                            for (Uri imageUri : imageUris) {
                                File imgFile = new File(getFilePathFromUri(projection, imageUri));
                                if (imgFile.lastModified() > Long.parseLong(communityStartTime) && ImageNotAlreadyUploaded(Uri.fromFile(imgFile).getLastPathSegment())) {

//                        imagesQueue.add(new GalleryImageModel(imgFile.toString(),true,true,String.valueOf(imgFile.lastModified())));
//                                Log.i("gallerySend","uri "+Uri.fromFile(imgFile).getLastPathSegment());
//                                Log.i("gallerySend","ImageNotAlreadyUploaded(Uri.fromFile(imgFile).getLastPathSegment()) "+ImageNotAlreadyUploaded(Uri.fromFile(imgFile).getLastPathSegment()));

                                    if (uploadQueueDB.insertData(Uri.fromFile(imgFile).getLastPathSegment(), getFilePathFromUri(projection, imageUri).toString(), String.valueOf(System.currentTimeMillis()))) {
                                        queuedCount++;
//                                        Log.i("galleryS","imgFile "+imgFile+" true");

                                    }
//                                    Log.i("galleryS","imgFile "+imgFile+" false");

                                }

                            }

//                            Log.i("galleryS","communityStartTime "+communityStartTime);
//                            Log.i("galleryS","communityID "+communityID);
//                            Log.i("galleryS","communityEndTime "+communityEndTime);


                            Cursor c =uploadQueueDB.getQueuedData();
                            while(c.moveToNext())
                            {
                                Log.i("galleryS","row "+c.getString(0)+c.getString(1)+c.getString(2));

                            }

                            if(queuedCount>0)
                            {
                                Constraints uploadConstraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
                                OneTimeWorkRequest galleryUploader = new OneTimeWorkRequest.Builder(UploadWorker.class).setConstraints(uploadConstraints).build();
                                WorkManager.getInstance(InlensGalleryActivity.this).enqueue(galleryUploader);

                            }


                            //todo update the time in sharedpref to match with the system current time to avoid notifications about already updated images.

                            Snackbar.make(rootGalleryRelativeLayout, "Queued " + queuedCount + " images.", BaseTransientBottomBar.LENGTH_SHORT).setAction("Learn more", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    Snackbar.make(rootGalleryRelativeLayout, imageUris.size()-queuedCount+" images skipped in order to avoid copies.", BaseTransientBottomBar.LENGTH_LONG).show();

                                }
                            }).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }
                else
                {
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


    private synchronized void firebaseUploader(List<GalleryImageModel> allCommunityImages) {

        imgPosition = -1;

        for (int i = 0; i < allCommunityImages.size(); i++) {
            if (allCommunityImages.get(i).isQueued()) {
                imgPosition = i;
                break;
            }
        }

        if (imgPosition > -1 && imagesToUpload - imgCount > 0 && ImageNotAlreadyUploaded(Uri.fromFile(new File(allCommunityImages.get(imgPosition).getImageUri())).getLastPathSegment())) {
            imgCount++;

            Glide.with(this).load(allCommunityImages.get(imgPosition).getImageUri()).into(uploadImageview);
            uploadTextView.setText("Starting upload.");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap bitmapAfterCompression = compressUploadFile(new File(allCommunityImages.get(imgPosition).getImageUri()));

            if (bitmapAfterCompression != null) {

                uploadProgressbar.setVisibility(View.VISIBLE);
                uploadPorgressTextView.setVisibility(View.VISIBLE);

                uploadProgressbar.setIndeterminate(false);
                uploadProgressbar.setProgress(0);
                uploadProgressbar.setSecondaryProgress(100);
                uploadProgressbar.setMax(100);
                uploadProgressbar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_circle));
                uploadProgressbar.clearAnimation();
                uploadProgressbar.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate));
                uploadProgressbar.getAnimation().start();

                uploadTextView.setText("Uploading file " + imgCount + " of " + imagesToUpload);

                bitmapAfterCompression.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] compressedImage = baos.toByteArray();

                String fileName = +System.currentTimeMillis() + Uri.fromFile(new File(allCommunityImages.get(imgPosition).getImageUri())).getLastPathSegment().toLowerCase();
                StorageReference filePath = storageRef.child(communityID).child(fileName);

                filePath.putBytes(compressedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                final String downloadUrl = String.valueOf(uri);
                                Log.i("uploading", "url " + downloadUrl);
                                String pushid = postRef.child(communityID).push().getKey();
                                Map uploadmap = new HashMap();
                                uploadmap.put(FirebaseConstants.POSTURL, downloadUrl);
                                uploadmap.put(FirebaseConstants.POSTBY, FirebaseAuth.getInstance().getCurrentUser().getUid());
                                uploadmap.put(FirebaseConstants.POSTTIME, ServerValue.TIMESTAMP);
                                postRef.child(communityID).child(pushid).setValue(uploadmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            allImagesInCurrentCommunity.add(downloadUrl);
                                            allCommunityImages.get(imgPosition).setQueued(false);
                                            firebaseUploader(allCommunityImages);

                                        } else {
                                            allCommunityImages.get(imgPosition).setQueued(false);
                                            Snackbar.make(rootGalleryRelativeLayout, "Image failed to upload. Please try again later.", Snackbar.LENGTH_SHORT).show();
                                            imageAdapter.notifyItemChanged(imgPosition);
                                            firebaseUploader(allCommunityImages);
                                        }
                                    }
                                });

                            }
                        });

                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        PROGRESS_CURRENT = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                        uploadProgressbar.setProgress(PROGRESS_CURRENT);
                        uploadPorgressTextView.setText(PROGRESS_CURRENT + "%");

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        allCommunityImages.get(imgPosition).setQueued(false);
                        Snackbar.make(rootGalleryRelativeLayout, "Image failed to upload. Please try again later.", Snackbar.LENGTH_SHORT).show();
                        firebaseUploader(allCommunityImages);
                    }
                });


            } else {
                allCommunityImages.get(imgPosition).setQueued(false);
                Snackbar.make(rootGalleryRelativeLayout, "Image compression failed. Please try again later.", Snackbar.LENGTH_SHORT).show();
                firebaseUploader(allCommunityImages);
            }

        } else {

            uploadDialog.dismiss();
            gallerySwipeRefresh.setRefreshing(true);
            displayImagesBasedOnTime(communityID, communityStartTime);
            isUploading = false;
//            if(queuedImageCount>0 && imagesToUpload != queuedImageCount)
//            {
//                Snackbar.make(rootGalleryRelativeLayout,queuedImageCount-imagesToUpload+" photo(s) were skipped.",BaseTransientBottomBar.LENGTH_LONG).setAction("Learn more", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//                        Toast.makeText(InlensGalleryActivity.this, "Avoid copies.", Toast.LENGTH_SHORT).show();
//                        Toast.makeText(InlensGalleryActivity.this, "Photos where taken prior to this album.", Toast.LENGTH_SHORT).show();
//                    }
//                }).show();
//            }
//            else
//            {
//
//            }
            Snackbar.make(rootGalleryRelativeLayout, "Upload complete.", Snackbar.LENGTH_SHORT).show();

        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences LastShownNotificationInfo = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
        if (LastShownNotificationInfo.contains("time")) {
            SharedPreferences.Editor editor = LastShownNotificationInfo.edit();
            editor.putString("time", String.valueOf(System.currentTimeMillis()));
            editor.commit();
        }

        gallerySwipeRefresh.setRefreshing(true);

        if (communityID == null && communityStartTime == null) {
            if (new PreOperationCheck().checkInternetConnectivity(InlensGalleryActivity.this)) {
                currentUserRef.child(FirebaseConstants.USERS).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(FirebaseConstants.LIVECOMMUNITYID)) {
                            communityID = dataSnapshot.child(FirebaseConstants.LIVECOMMUNITYID).getValue().toString();
                            communityStartTime = dataSnapshot.child(FirebaseConstants.COMMUNITIES).child(communityID).getValue().toString();
                            displayImagesBasedOnTime(communityID, communityStartTime);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                galleyHeaderTextView.setText("Gallery");

            }

        } else {
            displayImagesBasedOnTime(communityID, communityStartTime);
        }
    }


    @Override
    public void onBackPressed() {
        if (isUploading) {
            Snackbar.make(rootGalleryRelativeLayout, "Wait until upload is complete.", Snackbar.LENGTH_SHORT).show();
        } else {
            startActivity(new Intent(InlensGalleryActivity.this, SplashScreenActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }

    private void displayImagesBasedOnTime(String communityID, String communityStartTime) {

//        Log.i(AppConstants.MORE_OPTIONS,communityID);
//        Log.i(AppConstants.MORE_OPTIONS,communityStartTime);
//        Log.i(AppConstants.MORE_OPTIONS,"allImagesInCurrentCommunity-> " +allImagesInCurrentCommunity.size());
//        Log.i(AppConstants.MORE_OPTIONS,"allCommunityImages-> "+ allCommunityImages.size());

        ReadFirebaseData readFirebaseData = new ReadFirebaseData();
        readFirebaseData.readData(postRef.child(communityID), new FirebaseRead() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                allImagesInCurrentCommunity.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.hasChild(FirebaseConstants.POSTURL)) {
                        String uri = snapshot.child(FirebaseConstants.POSTURL).getValue().toString();
                        if (!allImagesInCurrentCommunity.contains(uri)) {
                            allImagesInCurrentCommunity.add(uri);
                        }
                    }
                }

                try {
                    allCommunityImages = getAllShownImagesPath(Long.parseLong(communityStartTime));
                } catch (NumberFormatException e) {

                    SharedPreferences CurrentActiveCommunity = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
                    String startTime = CurrentActiveCommunity.getString("startAt", String.valueOf(System.currentTimeMillis()));
                    allCommunityImages = getAllShownImagesPath(Long.parseLong(startTime));

                }

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

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure(DatabaseError databaseError) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        gallerySwipeRefresh.setRefreshing(false);

                    }
                }, 2000);
            }
        });

    }


    private List<GalleryImageModel> getAllShownImagesPath(long starttime) {

        Uri uri;
        Cursor cursor;
        int column_index_data;
        List<String> listOfAllImages = new ArrayList<>();
        List<String> lastmodifieddate = new ArrayList<>();
        List<GalleryImageModel> AllImagesList = new ArrayList<>();
        listOfAllImages.clear();
        AllImagesList.clear();
        lastmodifieddate.clear();

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
                        if (img.lastModified() > starttime) {

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
            return AllImagesList;

        } catch (Exception e) {
            return AllImagesList;

        }


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
                    CheckFabVisibility();

                } else {
                    viewHolder.galleryImage.clearColorFilter();
                    viewHolder.galleryItemSelectedButton.setVisibility(View.GONE);
                    CheckFabVisibility();

                }

                viewHolder.galleryImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        try {
                            if (ImageList.get(position).isSelected()) {
                                ImageList.get(position).setSelected(false);
                                imageAdapter.notifyItemChanged(position);
                                CheckFabVisibility();

                            } else {
                                ImageList.get(position).setSelected(true);
                                imageAdapter.notifyItemChanged(position);
                                CheckFabVisibility();

                            }
                        } catch (IndexOutOfBoundsException e) {

                        }


                    }
                });

                viewHolder.galleryItemSelectedButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (ImageList.get(position).isSelected()) {
                            ImageList.get(position).setSelected(false);
                            imageAdapter.notifyItemChanged(position);
                            CheckFabVisibility();

                        } else {
                            ImageList.get(position).setSelected(true);
                            imageAdapter.notifyItemChanged(position);
                            CheckFabVisibility();

                        }

                    }
                });

            }
        }


        private void CheckFabVisibility() {

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
}