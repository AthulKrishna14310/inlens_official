package com.integrals.inlens.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.integrals.inlens.Helper.Checker;
import com.integrals.inlens.Helper.NotificationHelper;
import com.integrals.inlens.Helper.PreOperationCheck;
import com.integrals.inlens.Models.GalleryImageModel;
import com.integrals.inlens.R;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class InlensGalleryActivity extends AppCompatActivity {

    private List<GalleryImageModel> AllCommunityImages;
    private static final String FILE_NAME = "UserInfo.ser";
    private RecyclerView GalleryGridView;
    private FloatingActionButton GalleryUploadFab;
    private SwipeRefreshLayout GallerySwipeRefreshLayout;
    final int PROGRESS_MAX = 100;
    int PROGRESS_CURRENT = 0;
    private static final String Channel_ID = "123";
    private List<String> AllImagesInCurrentCommunity;
    private ImageButton GallerBackButton, GalleryInfoButton;
    private TextView GalleyHeaderTextView;
    private Toast InternetCustomToast;
    private TextView ToastTitleTextView, ToastMessageTextview;
    private StorageTask UploadStorageTask;
    private DatabaseReference UserRef,Ref;
    private StorageReference StorageRef;

    //upload dialog items
    private Dialog UploadDialog;
    private RecyclerView UploadRecyclerView;
    private int UploadCount = 0;

    private String CommunityID = "Not Available";
    private String CommunityName = "Not Available";
    private String CommunityStartTime = "Not Available";
    private String CommunityEndTime = "Not Available";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_inlens_gallery);

        UploadDialogInit();

        AllCommunityImages = new ArrayList<>();
        AllImagesInCurrentCommunity = new ArrayList<>();

        CommunityID = getIntent().getStringExtra("CommunityID");
        CommunityName =  getIntent().getStringExtra("CommunityName");
        CommunityStartTime =  getIntent().getStringExtra("CommunityStartTime");
        CommunityEndTime =  getIntent().getStringExtra("CommunityEndTime");

        Ref = FirebaseDatabase.getInstance().getReference().child("Communities");
        UserRef =FirebaseDatabase.getInstance().getReference();
        StorageRef = FirebaseStorage.getInstance().getReference().child("situations");

        GallerBackButton = findViewById(R.id.gallery_toolbar).findViewById(R.id.mytoolbar_back_button);
        GalleyHeaderTextView = findViewById(R.id.gallery_toolbar).findViewById(R.id.mytoolbar_textview);
        GalleryInfoButton = findViewById(R.id.gallery_toolbar).findViewById(R.id.mytoolbar_info_button);
        GallerySwipeRefreshLayout = findViewById(R.id.gallery_swipe_refresh_layout);


        GallerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onBackPressed();

            }
        });


        if(CommunityID ==null && CommunityStartTime==null)
        {
            if(new Checker(InlensGalleryActivity.this).isConnectedToNet())
            {
                UserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("live_community"))
                        {
                            CommunityID  = dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("live_community").getValue().toString();
                            CommunityStartTime = dataSnapshot.child("Communities").child(CommunityID).child("starttime").getValue().toString();
                            DisplayImagesBasedOnTime();
                        }
                        else if(dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("dead_community"))
                        {
                            CommunityID  = dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("dead_community").getValue().toString();
                            CommunityStartTime = dataSnapshot.child("Communities").child(CommunityID).child("starttime").getValue().toString();
                            DisplayImagesBasedOnTime();
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                GalleyHeaderTextView.setText("Gallery");

            }
            else
            {
                SharedPreferences CurrentActiveCommunity = getSharedPreferences("CurrentCommunity.pref",Context.MODE_PRIVATE);
                CommunityID = CurrentActiveCommunity.getString("id","Unknown");
                CommunityStartTime=CurrentActiveCommunity.getString("time", String.valueOf(System.currentTimeMillis()));
                GalleyHeaderTextView.setText("Gallery");
                DisplayImagesBasedOnTime();
            }

        }
        else
        {
            DisplayImagesBasedOnTime();
            GalleyHeaderTextView.setText(CommunityName+" Gallery");

        }

        InternetCustomToast = new Toast(getApplicationContext());
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.custom_toast_layout, null);
        ToastTitleTextView = view.findViewById(R.id.custom_toast_title);
        ToastMessageTextview = view.findViewById(R.id.custom_toast_message);
        ToastTitleTextView.setText("No Internet Connection.");
        ToastMessageTextview.setText("Please connect to internet to perform new uploads.");
        InternetCustomToast.setView(view);
        InternetCustomToast.setGravity(Gravity.BOTTOM, 0, 40);


        GalleryInfoButton.setVisibility(View.VISIBLE);
        GalleryInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(InlensGalleryActivity.this).setTitle("Inlens Gallery")
                        .setMessage("This gallery shows all the images that can be upload to your current community. Swipe down to load new images.")
                        .setPositiveButton("Ok, I understand", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                dialogInterface.dismiss();

                            }
                        })
                        .setCancelable(true)
                        .create()
                        .show();

            }
        });


        GalleryUploadFab = findViewById(R.id.gallery_upload_fab);
        GalleryGridView = findViewById(R.id.gallery_recyclerview);
        GalleryGridView.setHasFixedSize(true);


        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        GalleryGridView.setLayoutManager(new GridLayoutManager(this, (int) (dpWidth / 100) + 1));
        final List<String> UploadQueue = new ArrayList<>();

        AllImagesInCurrentCommunity.clear();


        GallerySwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimaryDark), getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colordimAccent));

        GallerySwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                GetAllImagesFromDatabase();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        try
                        {
                            AllCommunityImages = getAllShownImagesPath(Long.parseLong(CommunityStartTime));
                            if(AllCommunityImages.size()>0)
                            {
                                SharedPreferences LastShownNotificationInfo = getSharedPreferences("LastNotification.pref",Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = LastShownNotificationInfo.edit();
                                editor.putString("time",AllCommunityImages.get(AllCommunityImages.size()-1).getCreatedTime());
                                editor.commit();
                            }

                            ImageAdapter adapter = new ImageAdapter(getApplicationContext(), AllCommunityImages);
                            GalleryGridView.removeAllViews();
                            GalleryGridView.setAdapter(adapter);
                            GallerySwipeRefreshLayout.setRefreshing(false);
                        }
                        catch (NumberFormatException e)
                        {
                            Toast.makeText(InlensGalleryActivity.this, "UserInfo sync required.", Toast.LENGTH_SHORT).show();
                        }



                    }
                }, 2000);
            }
        });

        GalleryUploadFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UploadQueue.clear();

                for (int i = 0; i < AllCommunityImages.size(); i++) {
                    if (AllCommunityImages.get(i).isSelected()) {
                        UploadQueue.add(AllCommunityImages.get(i).getImageUri());
                    }
                }

                if (UploadQueue.size() == 0) {
                    Toast.makeText(getApplicationContext(), "No images selected.", Toast.LENGTH_SHORT).show();
                } else {

                    UploadCount = 0;
                    UploadAdapter adapter = new UploadAdapter(UploadQueue);
                    UploadRecyclerView.setAdapter(adapter);
                    UploadDialog.show();
                }

            }
        });


    }

    private void UploadDialogInit() {

        UploadDialog = new Dialog(this);
        UploadDialog.setContentView(R.layout.inlens_gallery_upload_dialog_layout);
        UploadDialog.setCancelable(false);
        UploadDialog.setCanceledOnTouchOutside(false);
        UploadDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        UploadRecyclerView = UploadDialog.findViewById(R.id.inlensgallery_uploadqueue_recylerview);
        UploadRecyclerView.setHasFixedSize(true);
        UploadRecyclerView.setLayoutManager(new LinearLayoutManager(InlensGalleryActivity.this));

    }

    @Override
    public void onBackPressed() {
        if (GallerySwipeRefreshLayout.isRefreshing()) {
            Toast.makeText(this, "Wait until refresh is complete.", Toast.LENGTH_SHORT).show();
        } else if (UploadDialog.isShowing()) {
            Toast.makeText(this, "Wait until upload is complete.", Toast.LENGTH_SHORT).show();

        } else {
            super.onBackPressed();
        }
    }

    private void GetAllImagesFromDatabase() {

        AllImagesInCurrentCommunity.clear();

        if (!new PreOperationCheck().checkInternetConnectivity(getApplicationContext())) {
            InternetCustomToast.setDuration(Toast.LENGTH_LONG);
            InternetCustomToast.show();
        }


        FirebaseDatabase.getInstance().getReference().child("Communities").child(CommunityID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if (dataSnapshot.hasChild("posts")) {
                    for (DataSnapshot snapshot : dataSnapshot.child("posts").getChildren()) {
                        String uri = snapshot.child("uri").getValue().toString().toLowerCase();
                        AllImagesInCurrentCommunity.add(uri);
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void DisplayImagesBasedOnTime() {


        GetAllImagesFromDatabase();
        Toast.makeText(this, "Fetching all images. Please wait.", Toast.LENGTH_SHORT).show();
        GallerySwipeRefreshLayout.setRefreshing(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try
                {
                    AllCommunityImages = getAllShownImagesPath(Long.parseLong(CommunityStartTime));
                    ImageAdapter adapter = new ImageAdapter(getApplicationContext(), AllCommunityImages);
                    GalleryGridView.removeAllViews();
                    GalleryGridView.setAdapter(adapter);
                    GallerySwipeRefreshLayout.setRefreshing(false);
                }
                catch (NumberFormatException e)
                {
                    Toast.makeText(InlensGalleryActivity.this, "UserInfo sync required.", Toast.LENGTH_SHORT).show();

                }

                if (AllCommunityImages.size() == 0) {
                    Toast.makeText(InlensGalleryActivity.this, "No images found.", Toast.LENGTH_SHORT).show();

                }

            }
        }, 2000);
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

        for (int i = 0; i < AllImagesInCurrentCommunity.size(); i++) {
            if (AllImagesInCurrentCommunity.get(i).toLowerCase().contains(lastsegmentedpath.toLowerCase())) {
                return false;
            }

        }

        return true;

    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

        private Context context;
        private List<GalleryImageModel> ImageList;

        public ImageAdapter(Context context, List<GalleryImageModel> imageList) {
            this.context = context;
            ImageList = imageList;
        }


        @NonNull
        @Override
        public ImageAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ImageAdapter.ImageViewHolder(LayoutInflater.from(context).inflate(R.layout.gallery_item_card, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final ImageAdapter.ImageViewHolder holder, final int position) {


            Glide.with(context).load(ImageList.get(position).getImageUri())
                    .apply(new RequestOptions().placeholder(R.drawable.ic_photo_camera).centerCrop())
                    .into(holder.GallerImage);


            if (ImageList.get(position).isSelected()) {

                holder.GallerImage.setColorFilter(Color.argb(155, 185, 185, 185), PorterDuff.Mode.SRC_ATOP);
                holder.GalleryItemSelectedButton.setVisibility(View.VISIBLE);
                CheckFabVisibility();

            } else {
                holder.GallerImage.clearColorFilter();
                holder.GalleryItemSelectedButton.setVisibility(View.GONE);
                CheckFabVisibility();

            }

            holder.GallerImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    if (ImageList.get(position).isSelected()) {
                        ImageList.get(position).setSelected(false);
                        holder.GallerImage.clearColorFilter();
                        holder.GalleryItemSelectedButton.setVisibility(View.GONE);


                        CheckFabVisibility();

                    } else {
                        ImageList.get(position).setSelected(true);
                        holder.GallerImage.setColorFilter(Color.argb(155, 185, 185, 185), PorterDuff.Mode.SRC_ATOP);
                        holder.GalleryItemSelectedButton.setVisibility(View.VISIBLE);

                        CheckFabVisibility();

                    }


                }
            });

            holder.GalleryItemSelectedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (ImageList.get(position).isSelected()) {
                        ImageList.get(position).setSelected(false);
                        holder.GallerImage.clearColorFilter();
                        holder.GalleryItemSelectedButton.setVisibility(View.GONE);

                        CheckFabVisibility();

                    } else {
                        ImageList.get(position).setSelected(true);
                        holder.GallerImage.setColorFilter(Color.argb(155, 185, 185, 185), PorterDuff.Mode.SRC_ATOP);
                        holder.GalleryItemSelectedButton.setVisibility(View.VISIBLE);

                        CheckFabVisibility();

                    }

                }
            });


        }

        private void CheckFabVisibility() {

            for (int k = 0; k < ImageList.size(); k++) {
                if (ImageList.get(k).isSelected()) {
                    GalleryUploadFab.show();
                    GalleryUploadFab.setVisibility(View.VISIBLE);
                    break;
                } else {
                    if (k == ImageList.size() - 1) {
                        GalleryUploadFab.hide();
                        GalleryUploadFab.setVisibility(View.GONE);

                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return ImageList.size();
        }

        class ImageViewHolder extends RecyclerView.ViewHolder {

            ImageView GallerImage;
            ImageButton GalleryItemSelectedButton;

            public ImageViewHolder(View itemView) {
                super(itemView);

                GallerImage = itemView.findViewById(R.id.gallery_item_imageview);
                GalleryItemSelectedButton = itemView.findViewById(R.id.gallery_item_selected_imagebutton);

            }
        }
    }

    private class UploadAdapter extends RecyclerView.Adapter<UploadAdapter.UploadViewHolder> {

        List<String> uploadQueue;

        public UploadAdapter(List<String> uploadQueue) {
            this.uploadQueue = uploadQueue;
        }

        @NonNull
        @Override
        public UploadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.inlensgallery_single_item_upload_layout, parent, false);
            return new UploadAdapter.UploadViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final UploadViewHolder holder, final int position) {

            final int j = position + 1;


            if (CommunityID.equals("Not Available")) {
                Toast.makeText(InlensGalleryActivity.this, "No community currently active.", Toast.LENGTH_SHORT).show();
            }
            else if(CommunityID.equals("Unknown"))
            {
                Toast.makeText(InlensGalleryActivity.this, "Please connect to internet and try again.", Toast.LENGTH_SHORT).show();

            }
            else {
                holder.UploadFileName.setText(Uri.fromFile(new File(uploadQueue.get(position))).getLastPathSegment());
                holder.UploadProgressbar.setIndeterminate(true);
                holder.UploadPercentage.setText("0%");

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Bitmap bitmapAfterCompression = compressUploadFile(new File(uploadQueue.get(position)));

                holder.UploadPreview.setImageBitmap(bitmapAfterCompression);

                if (bitmapAfterCompression != null)
                {
                    bitmapAfterCompression.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] compressedImage = baos.toByteArray();


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        final NotificationHelper helper = new NotificationHelper(InlensGalleryActivity.this);
                        final Notification.Builder builder = helper.getNotificationBuilder("title", "Starting upload");
                        helper.getManager().notify(123, builder.build());


                        UploadStorageTask = StorageRef.child(Uri.fromFile(new File(uploadQueue.get(position))).getLastPathSegment().toLowerCase() + System.currentTimeMillis()).putBytes(compressedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {


                                if (task.isSuccessful()) {
                                    final String downloadUrl = task.getResult().getDownloadUrl().toString();
                                    String pushid = Ref.child(CommunityID).child("posts").push().getKey();
                                    Map uploadmap = new HashMap();
                                    uploadmap.put("uri", downloadUrl);
                                    uploadmap.put("by", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    uploadmap.put("time", ServerValue.TIMESTAMP);
                                    Ref.child(CommunityID).child("posts").child(pushid).setValue(uploadmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(), "Uploaded image.", Toast.LENGTH_SHORT).show();
                                                DisplayImagesBasedOnTime();
                                                helper.getManager().cancelAll();
                                                UploadCount++;
                                                holder.UploadPercentage.setText("Upload complete.");
                                                holder.UploadProgressbar.setProgress(100);

                                                if (UploadCount == uploadQueue.size()) {
                                                    UploadDialog.dismiss();
                                                }

                                            } else {
                                                helper.getManager().cancelAll();
                                                holder.UploadPercentage.setText("Some error was encountered.");
                                                UploadCount++;
                                                if (UploadCount == uploadQueue.size()) {
                                                    UploadDialog.dismiss();
                                                }
                                            }
                                        }
                                    });

                                } else {
                                    helper.getManager().cancelAll();
                                    holder.UploadPercentage.setText("Some error was encountered.");
                                    UploadCount++;
                                    if (UploadCount == uploadQueue.size()) {
                                        UploadDialog.dismiss();
                                    }
                                }
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {


                                PROGRESS_CURRENT = (int) (100.0 * ((double) (taskSnapshot.getBytesTransferred() / (1024 * 1024)) / ((double) taskSnapshot.getTotalByteCount() / (1024 * 1024))));
                                helper.getNotificationBuilder("Inlens", "Uploading post " + j).setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
                                helper.getManager().notify(123, builder.build());

                                holder.UploadProgressbar.setIndeterminate(false);
                                holder.UploadProgressbar.setProgress(PROGRESS_CURRENT, true);
                                holder.UploadPercentage.setText(String.format("%.2f / %.2f Mb", (double) taskSnapshot.getBytesTransferred() / (1024 * 1024), (double) taskSnapshot.getTotalByteCount() / (1024 * 1024)) + "  " + PROGRESS_CURRENT + " %");

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                helper.getManager().cancelAll();
                                UploadCount++;
                                if (UploadCount == uploadQueue.size()) {
                                    UploadDialog.dismiss();
                                }
                            }
                        });
                    }
                    else
                    {
                        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(InlensGalleryActivity.this);
                        final NotificationCompat.Builder builder = new NotificationCompat.Builder(InlensGalleryActivity.this, Channel_ID);
                        builder.setContentTitle("Inlens")
                                .setProgress(100, 0, true)
                                .setSmallIcon(R.drawable.inlens_logo_m)
                                .setPriority(NotificationCompat.PRIORITY_HIGH);
                        UploadStorageTask = StorageRef.child(Uri.fromFile(new File(uploadQueue.get(position))).getLastPathSegment() + System.currentTimeMillis()).putBytes(compressedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {
                                    final String downloadUrl = task.getResult().getDownloadUrl().toString();
                                    String pushid = Ref.child(CommunityID).child("posts").push().getKey();
                                    Map uploadmap = new HashMap();
                                    uploadmap.put("uri", downloadUrl);
                                    uploadmap.put("by", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    uploadmap.put("time", ServerValue.TIMESTAMP);
                                    Ref.child(CommunityID).child("posts").child(pushid).setValue(uploadmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(), "Uploaded image.", Toast.LENGTH_SHORT).show();
                                                DisplayImagesBasedOnTime();
                                                notificationManager.cancelAll();
                                                UploadCount++;
                                                holder.UploadProgressbar.setProgress(100);
                                                holder.UploadPercentage.setText("Upload complete.");
                                                if (UploadCount == uploadQueue.size()) {
                                                    UploadDialog.dismiss();
                                                }

                                            } else {
                                                notificationManager.cancelAll();
                                                UploadCount++;
                                                holder.UploadPercentage.setText("Some error was encountered.");
                                                if (UploadCount == uploadQueue.size()) {
                                                    UploadDialog.dismiss();
                                                }
                                            }
                                        }
                                    });

                                } else {
                                    notificationManager.cancelAll();
                                    holder.UploadPercentage.setText("Some error was encountered.");
                                    UploadCount++;
                                    if (UploadCount == uploadQueue.size()) {
                                        UploadDialog.dismiss();
                                    }
                                }
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {


                                PROGRESS_CURRENT = (int) (100.0 * ((double) (taskSnapshot.getBytesTransferred() / (1024 * 1024)) / (((double) taskSnapshot.getTotalByteCount() / (1024 * 1024)))));
                                builder.setContentText("Uploading post " + j).setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
                                notificationManager.notify(123, builder.build());

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    holder.UploadProgressbar.setIndeterminate(false);
                                    holder.UploadProgressbar.setProgress(PROGRESS_CURRENT, true);
                                }

                                holder.UploadPercentage.setText(String.format("%.2f / %.2f Mb", (double) taskSnapshot.getBytesTransferred() / (1024 * 1024), (double) taskSnapshot.getTotalByteCount() / (1024 * 1024)) + "  " + PROGRESS_CURRENT + " %");


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                notificationManager.cancelAll();
                                UploadCount++;
                                if (UploadCount == uploadQueue.size()) {
                                    UploadDialog.dismiss();
                                }
                            }
                        });
                    }


                }
                else
                {
                    Toast.makeText(InlensGalleryActivity.this, "Image compression failed. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }

            holder.UploadCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    UploadStorageTask.cancel();
                    holder.UploadPercentage.setText("Upload cancelled by user.");
                    holder.UploadCancel.setEnabled(false);

                }
            });

        }

        @Override
        public int getItemCount() {
            return uploadQueue.size();
        }

        public class UploadViewHolder extends RecyclerView.ViewHolder {

            ProgressBar UploadProgressbar;
            TextView UploadFileName, UploadPercentage;
            ImageButton UploadCancel;
            ImageView UploadPreview;

            public UploadViewHolder(View itemView) {
                super(itemView);

                UploadProgressbar = itemView.findViewById(R.id.inlens_gallery_dialog_progressbar);
                UploadFileName = itemView.findViewById(R.id.inlens_gallery_dialog_title);
                UploadPercentage = itemView.findViewById(R.id.inlens_gallery_dialog_percentage_message);
                UploadCancel = itemView.findViewById(R.id.inlens_gallery_dialog_cancel_btn);
                UploadPreview = itemView.findViewById(R.id.inlens_gallery_dialog_upload_imageview);
            }
        }
    }


    public Bitmap compressUploadFile(File bitmapFile) {
        try {
            Bitmap result = new Compressor(InlensGalleryActivity.this)
                    .setQuality(90)
                    .compressToBitmap(bitmapFile);

            return result;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }
}
