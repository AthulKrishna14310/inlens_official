package com.integrals.inlens.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.FirebaseConstants;
import com.integrals.inlens.Helper.PreOperationCheck;
import com.integrals.inlens.Helper.ReadFirebaseData;
import com.integrals.inlens.Interface.FirebaseRead;
import com.integrals.inlens.Models.GalleryImageModel;
import com.integrals.inlens.R;
import com.skyfishjy.library.RippleBackground;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import id.zelory.compressor.Compressor;

public class InlensGalleryActivity extends AppCompatActivity {

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
    int imagesToUpload=0;
    boolean isUploading=false;
    RelativeLayout rootGalleryRelativeLayout;
    SwipeRefreshLayout gallerySwipeRefresh;


    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_inlens_gallery);


        rootGalleryRelativeLayout = findViewById(R.id.root_for_gallery);
        gallerySwipeRefresh = findViewById(R.id.gallerySwipeRefreshLayout);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        allCommunityImages = new ArrayList<>();
        allImagesInCurrentCommunity = new ArrayList<>();

        communityID = getIntent().getStringExtra("CommunityID");
        communityStartTime =  getIntent().getStringExtra("CommunityStartTime");

        postRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.POSTS);
        currentUserRef =FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference().child("situations");

        galleryBackButton = findViewById(R.id.gallery_toolbar).findViewById(R.id.mytoolbar_back_button);
        galleyHeaderTextView = findViewById(R.id.gallery_toolbar).findViewById(R.id.mytoolbar_textview);
        galleyHeaderTextView.setText("Your Gallery");
        galleryInfoButton = findViewById(R.id.gallery_toolbar).findViewById(R.id.mytoolbar_info_button);

        galleryBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onBackPressed();

            }
        });

        galleryGridRecyclerView = findViewById(R.id.gallery_recyclerview);
        galleryGridRecyclerView.setHasFixedSize(true);

        gallerySwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                gallerySwipeRefresh.setRefreshing(true);
                displayImagesBasedOnTime(communityID,communityStartTime);

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

                galleryUploadFab.setVisibility(View.GONE);
                imagesToUpload = 0;
                for(int i=0;i<allCommunityImages.size();i++)
                {
                    if(allCommunityImages.get(i).isSelected())
                    {
                        imagesToUpload++;
                        allCommunityImages.get(i).setQueued(true);
                        imageAdapter.notifyItemChanged(i);
                    }
                }
            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();

        gallerySwipeRefresh.setRefreshing(true);

        if(communityID==null && communityStartTime==null)
        {
            if(new PreOperationCheck().checkInternetConnectivity(InlensGalleryActivity.this))
            {
                currentUserRef.child(FirebaseConstants.USERS).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(FirebaseConstants.LIVECOMMUNITYID))
                        {
                            communityID  = dataSnapshot.child(FirebaseConstants.LIVECOMMUNITYID).getValue().toString();
                            communityStartTime = dataSnapshot.child(FirebaseConstants.COMMUNITIES).child(communityID).getValue().toString();
                            displayImagesBasedOnTime(communityID,communityStartTime);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                galleyHeaderTextView.setText("Gallery");

            }

        }
        else
        {
            displayImagesBasedOnTime(communityID,communityStartTime);
        }
    }

    private String getOffsetDeletedTime(String timeStamp) {
        TimeZone timeZone = TimeZone.getDefault();
        long offsetInMillis = timeZone.getOffset(Calendar.ZONE_OFFSET);
        long givenTime = Long.parseLong(timeStamp);
        long offsetDeletedTime = givenTime-offsetInMillis;
        return String.valueOf(offsetDeletedTime);
    }

    @Override
    public void onBackPressed() {
        if (isUploading) {
            Snackbar.make(rootGalleryRelativeLayout,"Wait until upload is complete.",Snackbar.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    private void displayImagesBasedOnTime(String communityID, String communityStartTime) {


        ReadFirebaseData readFirebaseData = new ReadFirebaseData();
        readFirebaseData.readData(postRef.child(communityID), new FirebaseRead() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                allImagesInCurrentCommunity.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    if(snapshot.hasChild(FirebaseConstants.POSTURL))
                    {
                        String uri=snapshot.child(FirebaseConstants.POSTURL).getValue().toString();
                        if(!allImagesInCurrentCommunity.contains(uri))
                        {
                            allImagesInCurrentCommunity.add(uri);
                        }
                    }
                }

                allCommunityImages = getAllShownImagesPath(Long.parseLong(communityStartTime));
                if (allCommunityImages.size() == 0) {
                    final RippleBackground rippleBackground=(RippleBackground)findViewById(R.id.content);
                    rippleBackground.startRippleAnimation();

                    allCommunityImages.add(null);
                    galleryGridRecyclerView.setLayoutManager(new GridLayoutManager(InlensGalleryActivity.this,  1));
                    imageAdapter = new ImageAdapter(getApplicationContext(), allCommunityImages);
                    galleryGridRecyclerView.removeAllViews();
                    galleryGridRecyclerView.setAdapter(imageAdapter);
                    galleryUploadFab.setVisibility(View.GONE);
                }
                else
                {

                    galleryGridRecyclerView.setLayoutManager(new GridLayoutManager(InlensGalleryActivity.this, 3));
                    Collections.reverse(allCommunityImages);
                    imageAdapter = new ImageAdapter(getApplicationContext(), allCommunityImages);
                    galleryGridRecyclerView.removeAllViews();
                    galleryGridRecyclerView.setAdapter(imageAdapter);
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        gallerySwipeRefresh.setRefreshing(false);

                    }
                },2000);
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
                },2000);
            }
        });

    }




    private List<GalleryImageModel> getAllShownImagesPath(long starttime) {

        TimeZone timeZone = TimeZone.getDefault();
        long offsetInMillis = timeZone.getOffset(Calendar.ZONE_OFFSET);
        starttime+=offsetInMillis;
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

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            File img = new File(absolutePathOfImage);
            if (img.lastModified() > starttime && !absolutePathOfImage.toLowerCase().contains("screenshot") && !absolutePathOfImage.toLowerCase().contains("whatsapp")) {

                String lastsegmentedpath = Uri.fromFile(new File(absolutePathOfImage)).getLastPathSegment();

                if (!listOfAllImages.contains(lastsegmentedpath) && ImageNotAlreadyUploaded(lastsegmentedpath)) {

                    listOfAllImages.add(absolutePathOfImage);
                    lastmodifieddate.add(String.valueOf(img.lastModified()));

                }


            }
        }

        for (int i = 0; i < listOfAllImages.size(); i++) {
            AllImagesList.add(new GalleryImageModel(listOfAllImages.get(i), false,lastmodifieddate.get(i)));
        }


        return AllImagesList;
    }

    private boolean ImageNotAlreadyUploaded(String lastsegmentedpath) {

        for (int i = 0; i < allImagesInCurrentCommunity.size(); i++) {
            if (allImagesInCurrentCommunity.get(i).toLowerCase().contains(lastsegmentedpath.toLowerCase())) {
                return false;
            }

        }
        return true;

    }

    public class EmptyGridViewHolder extends RecyclerView.ViewHolder {
        public EmptyGridViewHolder(View itemView) {
            super(itemView);

        }
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView galleryImage;
        ImageButton galleryItemSelectedButton;
        ProgressBar progressBar;
        TextView progressTextView;
        RelativeLayout progressWrapper,textviewWrapper;

        public ImageViewHolder(View itemView) {
            super(itemView);

            galleryImage = itemView.findViewById(R.id.gallery_item_imageview);
            galleryItemSelectedButton = itemView.findViewById(R.id.gallery_item_selected_imagebutton);
            progressBar = itemView.findViewById(R.id.gallery_item_progressbar);
            progressTextView  =itemView.findViewById(R.id.gallery_item_progress_textview);
            progressWrapper = itemView.findViewById(R.id.gallery_item_progress_wrapper);
            textviewWrapper =itemView.findViewById(R.id.gallery_item_textview_wrapper);
        }
    }

    private class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;
        private List<GalleryImageModel> ImageList;
        private int VIEW_TYPE_EMPTY=1,VIEW_TYPE_IMAGE=0;
        private int uploadCount;

        public ImageAdapter(Context context, List<GalleryImageModel> imageList) {
            this.context = context;
            ImageList = imageList;
            uploadCount=0;

        }


        @Override
        public int getItemViewType(int position) {
            return ImageList.get(position)==null?VIEW_TYPE_EMPTY:VIEW_TYPE_IMAGE;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if(viewType==VIEW_TYPE_IMAGE)
            {
                return new ImageViewHolder(LayoutInflater.from(context).inflate(R.layout.gallery_item_card, parent, false));

            }
            else if(viewType==VIEW_TYPE_EMPTY)
            {
                View view = LayoutInflater.from(context).inflate(R.layout.empty_layout, parent, false);
                return new EmptyGridViewHolder(view);
            }

            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {

            if (holder instanceof ImageViewHolder) {
                ImageViewHolder viewHolder = (ImageViewHolder) holder;


                Glide.with(context).load(ImageList.get(position).getImageUri())
                        .apply(new RequestOptions().placeholder(R.drawable.ic_photo_camera).centerCrop())
                        .into(viewHolder.galleryImage);

                if (ImageList.get(position).isSelected() && ImageList.get(position).isQueued()) {
                    viewHolder.galleryImage.setColorFilter(Color.argb(155, 185, 185, 185), PorterDuff.Mode.SRC_ATOP);
                    viewHolder.galleryItemSelectedButton.setVisibility(View.VISIBLE);
                    isUploading = true;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Bitmap bitmapAfterCompression = compressUploadFile(new File(allCommunityImages.get(position).getImageUri()));
                    if (bitmapAfterCompression != null) {

                        viewHolder.progressBar.setVisibility(View.VISIBLE);
                        viewHolder.textviewWrapper.setVisibility(View.VISIBLE);

                        viewHolder.progressBar.setIndeterminate(false);
                        viewHolder.progressBar.setProgress(0);
                        viewHolder.progressBar.setSecondaryProgress(100);
                        viewHolder.progressBar.setMax(100);
                        viewHolder.progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_circle));

                        bitmapAfterCompression.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] compressedImage = baos.toByteArray();


                        storageRef.child(Uri.fromFile(new File(allCommunityImages.get(position).getImageUri())).getLastPathSegment().toLowerCase() + System.currentTimeMillis()).putBytes(compressedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {


                                if (task.isSuccessful()) {
                                    final String downloadUrl = task.getResult().getDownloadUrl().toString();
                                    String pushid = postRef.child(communityID).push().getKey();
                                    Map uploadmap = new HashMap();
                                    uploadmap.put(FirebaseConstants.POSTURL, downloadUrl);
                                    uploadmap.put(FirebaseConstants.POSTBY, FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    uploadmap.put(FirebaseConstants.POSTTIME, getOffsetDeletedTime(String.valueOf(System.currentTimeMillis())));
                                    postRef.child(communityID).child(pushid).setValue(uploadmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                gallerySwipeRefresh.setRefreshing(true);
                                                uploadCount++;
                                                viewHolder.galleryItemSelectedButton.setVisibility(View.VISIBLE);
                                                viewHolder.progressBar.setVisibility(View.GONE);


                                                if (uploadCount == imagesToUpload || imagesToUpload == 0) {
                                                    isUploading = false;
                                                    onResume();
                                                }

                                            } else {
                                                uploadCount++;
                                                if (uploadCount == imagesToUpload) {
                                                    isUploading = false;
                                                }
                                            }
                                        }
                                    });

                                } else {
                                    uploadCount++;
                                    if (uploadCount == imagesToUpload) {
                                        isUploading = false;
                                    }
                                }
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                                PROGRESS_CURRENT = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                                viewHolder.progressBar.setProgress(PROGRESS_CURRENT);
                                viewHolder.progressTextView.setText(PROGRESS_CURRENT + "%");

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                uploadCount++;
                                if (uploadCount == imagesToUpload) {
                                    isUploading = false;
                                }
                                viewHolder.galleryItemSelectedButton.setVisibility(View.VISIBLE);
                                viewHolder.galleryItemSelectedButton.setBackgroundResource(R.drawable.ic_info);
                                viewHolder.progressBar.setVisibility(View.GONE);

                            }
                        });


                    } else {
                        Snackbar.make(rootGalleryRelativeLayout,"Image compression failed. Please try again later.",Snackbar.LENGTH_SHORT).show();
                    }
                }
                else {

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


                            try
                            {
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
                            catch (IndexOutOfBoundsException e)
                            {

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
        }


        private void CheckFabVisibility() {

            for (int k = 0; k < ImageList.size(); k++) {
                if (ImageList.get(k).isSelected()) {
                    galleryUploadFab.show();
                    galleryUploadFab.setVisibility(View.VISIBLE);
                    break;
                } else {
                    if (k == ImageList.size() - 1) {
                        galleryUploadFab.hide();
                        galleryUploadFab.setVisibility(View.GONE);

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
            if(orientation(result)==PORTRAIT){
                result = new Compressor(InlensGalleryActivity.this)
                        .setQuality(90)
                        .setMaxHeight(640)
                        .setMaxWidth(480)
                        .compressToBitmap(bitmapFile);

            }else{
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

        if(result.getHeight()>result.getWidth()){
            return PORTRAIT;
        }else{
            return LANDSCAPE;
        }

    }
}