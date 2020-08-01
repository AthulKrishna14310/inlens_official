package com.integrals.inlens.WorkManager;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.integrals.inlens.AsynchTasks.HandleQuit;
import com.integrals.inlens.Database.UploadQueueDB;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.FirebaseConstants;
import com.integrals.inlens.Models.GalleryImageModel;
import com.integrals.inlens.Notification.NotificationHelper;
import com.integrals.inlens.Notification.NotificationOreo;
import com.integrals.inlens.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class UploadWorker extends Worker {

    UploadQueueDB uploadQueueDB;
    StorageReference storageRef;
    Context context;
    DatabaseReference postRef;
    String communityStartTime, communityID, communityEndTime;
    int imageMissingCount, failedToUploadCount, skippedImageCount;

    // debugging
    int imgUploaded, failedCount;
    String TAG = "UploadWorker";
    public static final int PORTRAIT = 1, LANDSCAPE = 0;
    int imgPosition = -1;
    UploadTask uploadTask;
    String notificationId;


    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        SharedPreferences currentActiveCommunity = context.getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
        communityStartTime = currentActiveCommunity.getString("startAt", String.valueOf(System.currentTimeMillis()));
        communityID = currentActiveCommunity.getString("id", AppConstants.NOT_AVALABLE);
        communityEndTime = currentActiveCommunity.getString("stopAt", AppConstants.NOT_AVALABLE);

        postRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.POSTS);
        storageRef = FirebaseStorage.getInstance().getReference().child(FirebaseConstants.COMMUNITIES_STORAGE);
        uploadQueueDB = new UploadQueueDB(context);
        imageMissingCount = 0;
        failedToUploadCount = 0;
        skippedImageCount = 0;
        imgUploaded = 0;
        failedCount = 0;
        uploadTask = null;
    }

    @NonNull
    @Override
    public Result doWork() {

        UploadImages uploadImages = new UploadImages();
        if (uploadImages.getStatus() == AsyncTask.Status.RUNNING) {
            uploadImages.cancel(true);
            if (uploadTask != null) {
                uploadTask.cancel();
            }

        }
        uploadImages.execute();

        try {
            long endTime = Long.parseLong(communityEndTime);
            if (System.currentTimeMillis() >= endTime && uploadQueueDB.getQueuedData().getCount() == 0) {
                DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.USERS).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                DatabaseReference communityRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.COMMUNITIES);
                new HandleQuit(context, currentUserRef, communityRef, communityID).execute();
            }

        } catch (Exception e) {
            // catch error
        }
        return Result.success();

    }

    public class UploadImages extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Cursor cursor = uploadQueueDB.getQueuedData();
            List<GalleryImageModel> imageUri = new ArrayList<>();
            while (cursor.moveToNext()) {
                File imgFile = new File(cursor.getString(1));
                if (imgFile.exists()) {
                    Log.i("dbUpload", "uri to upload " + cursor.getString(1));
                    imageUri.add(new GalleryImageModel(cursor.getString(1), true, true, String.valueOf(imgFile.lastModified())));
                } else {
                    uploadQueueDB.deleteData(cursor.getString(0));
                    imageMissingCount++;
                    Log.i(TAG, "imageMissingCount " + imageMissingCount);
                }
            }
            cursor.close();
            if (imageUri.size() > 0) {
                    /*for (GalleryImageModel model : imageUri) {
                        uploadToFirebase(model.getImageUri(), model.getCreatedTime(), imageUri.size());

                    }*/
                NotificationHelper helper = new NotificationHelper(context);
                String title = "Upload Status";
                String message = imageUri.size() + " image(s) to upload";
                helper.displayTitleMesageNoti(title, message);

                SharedPreferences notificationPref = context.getSharedPreferences(AppConstants.NOTIFICATION_PREF, Context.MODE_PRIVATE);
                notificationId = notificationPref.getString("id", String.valueOf(System.currentTimeMillis()));
                uploadToFirebase(imageUri, notificationId);
            }
            return null;
        }
    }

    public void uploadToFirebase(List<GalleryImageModel> queuedImages, String id) {


        String uri;
        String createdTime;
        imgPosition = -1;
        for (int i = 0; i < queuedImages.size(); i++) {
            if (queuedImages.get(i).isQueued()) {
                imgPosition = i;
                break;
            }
        }
        uri = queuedImages.get(imgPosition).getImageUri();
        createdTime = queuedImages.get(imgPosition).getCreatedTime();

        File imgFile = new File(uri);
        if (imgFile.exists()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap bitmapAfterCompression = compressUploadFile(imgFile);
            if (bitmapAfterCompression != null) {

                String fileName = FirebaseAuth.getInstance().getCurrentUser().getUid() + "_uploaded_" + Uri.fromFile(imgFile).getLastPathSegment().toLowerCase();
                StorageReference filePath = storageRef.child(communityID).child(fileName);

                bitmapAfterCompression.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] compressedImage = baos.toByteArray();

                uploadTask = filePath.putBytes(compressedImage);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    NotificationOreo helper = new NotificationOreo(context);
                    Notification.Builder builderOreo = helper.getNotificationBuilder("Cloud Album Upload", "Starting upload to cloud album");
                    helper.getManager().notify(Integer.parseInt(id.substring(id.length() - 4)), builderOreo.build());

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadUri) {

                                    final String downloadUrl = String.valueOf(downloadUri);
                                    Log.i("uploading", "url " + downloadUrl);
                                    Map uploadmap = new HashMap();
                                    uploadmap.put(FirebaseConstants.POSTURL, downloadUrl);
                                    uploadmap.put(FirebaseConstants.POSTBY, FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    uploadmap.put(FirebaseConstants.POSTTIME, createdTime);

                                    postRef.child(communityID).child(fileName.replaceAll("[^a-zA-Z0-9]", "")).setValue(uploadmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                imgUploaded++;
//                                            allCommunityImages.get(imgPosition).setQueued(false);
                                                String fileName = Uri.fromFile(new File(uri)).getLastPathSegment();
                                                UploadQueueDB uploadQueueDB = new UploadQueueDB(getApplicationContext());
                                                boolean result = uploadQueueDB.deleteData(fileName);
                                                Log.i("uploading", "deleted " + fileName + " result " + result);


                                                if (imgUploaded == queuedImages.size()) {
                                                    helper.getManager().cancelAll();
                                                    NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
                                                    String message = "Uploaded " + imgUploaded + " out of " + queuedImages.size() + " photo(s). ";
                                                    notificationHelper.displayTitleMesageNoti("Photos Uploaded", message);
                                                } else if (failedCount > 0 && imgUploaded + failedCount == queuedImages.size()) {
                                                    helper.getManager().cancelAll();
                                                    NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
                                                    String message = "Uploaded " + imgUploaded + " out of " + queuedImages.size() + " photo(s). Remaining photos will be uploaded later.";
                                                    notificationHelper.displayTitleMesageNoti("Photos Uploaded", message);

                                                } else {
                                                    queuedImages.get(imgPosition).setQueued(false);
                                                    SharedPreferences notificationPref = context.getSharedPreferences(AppConstants.NOTIFICATION_PREF, Context.MODE_PRIVATE);
                                                    String repeatId = notificationPref.getString("id", String.valueOf(System.currentTimeMillis()));
                                                    if (id.equals(repeatId)) {
                                                        uploadToFirebase(queuedImages, id);

                                                    } else {
                                                        helper.getManager().cancel(Integer.parseInt(id.substring(id.length() - 4)));
                                                    }
                                                }


                                            } else {

                                                failedCount++;
//                                            allCommunityImages.get(imgPosition).setQueued(false);


                                                if (imgUploaded == queuedImages.size()) {
                                                    helper.getManager().cancelAll();
                                                    NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
                                                    String message = "Uploaded " + imgUploaded + " out of " + queuedImages.size() + " photo(s). ";
                                                    notificationHelper.displayTitleMesageNoti("Photos Uploaded", message);
                                                } else if (failedCount > 0 && imgUploaded + failedCount == queuedImages.size()) {
                                                    helper.getManager().cancelAll();
                                                    NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
                                                    String message = "Uploaded " + imgUploaded + " out of " + queuedImages.size() + " photo(s). Remaining photos will be uploaded later.";
                                                    notificationHelper.displayTitleMesageNoti("Photos Uploaded", message);

                                                } else {
                                                    queuedImages.get(imgPosition).setQueued(false);
                                                    SharedPreferences notificationPref = context.getSharedPreferences(AppConstants.NOTIFICATION_PREF, Context.MODE_PRIVATE);
                                                    String repeatId = notificationPref.getString("id", String.valueOf(System.currentTimeMillis()));
                                                    if (id.equals(repeatId)) {
                                                        uploadToFirebase(queuedImages, id);

                                                    } else {
                                                        helper.getManager().cancel(Integer.parseInt(id.substring(id.length() - 4)));
                                                    }
                                                }
                                            }
                                        }
                                    });

                                }
                            });

                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            int PROGRESS_CURRENT = (int) ((100.0 * ((double) (taskSnapshot.getBytesTransferred()))) / ((double) taskSnapshot.getTotalByteCount()));
                            Log.i("imguploader", "progress of " + imgUploaded + " : " + PROGRESS_CURRENT);

                            int uploadingCount = imgUploaded + 1;

                            builderOreo.setContentText("Uploading photo " + uploadingCount + " of " + queuedImages.size()).setProgress(100, PROGRESS_CURRENT, false);
                            helper.getManager().notify(Integer.parseInt(id.substring(id.length() - 4)), builderOreo.build());

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            failedCount++;

//                        allCommunityImages.get(imgPosition).setQueued(false);


                            if (imgUploaded == queuedImages.size()) {
                                helper.getManager().cancelAll();
                                NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
                                String message = "Uploaded " + imgUploaded + " out of " + queuedImages.size() + " photo(s). ";
                                notificationHelper.displayTitleMesageNoti("Photos Uploaded", message);
                            } else if (failedCount > 0 && imgUploaded + failedCount == queuedImages.size()) {
                                helper.getManager().cancelAll();
                                NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
                                String message = "Uploaded " + imgUploaded + " out of " + queuedImages.size() + " photo(s). ";
                                notificationHelper.displayTitleMesageNoti("Photos Uploaded", message);

                            } else {
                                queuedImages.get(imgPosition).setQueued(false);
                                SharedPreferences notificationPref = context.getSharedPreferences(AppConstants.NOTIFICATION_PREF, Context.MODE_PRIVATE);
                                String repeatId = notificationPref.getString("id", String.valueOf(System.currentTimeMillis()));
                                if (id.equals(repeatId)) {
                                    uploadToFirebase(queuedImages, id);

                                } else {
                                    helper.getManager().cancel(Integer.parseInt(id.substring(id.length() - 4)));
                                }
                            }
                        }
                    });


                } else {
                    final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                    final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), id.substring(id.length()-4));
                    builder.setContentTitle("Cloud Album Upload")
                            .setContentText("Starting upload to cloud album")
                            .setProgress(100, 0, true)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setPriority(NotificationCompat.DEFAULT_ALL);

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadUri) {

                                    final String downloadUrl = String.valueOf(downloadUri);
                                    Log.i("uploading", "url " + downloadUrl);
                                    String pushid = postRef.child(communityID).push().getKey();
                                    Map uploadmap = new HashMap();
                                    uploadmap.put(FirebaseConstants.POSTURL, downloadUrl);
                                    uploadmap.put(FirebaseConstants.POSTBY, FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    uploadmap.put(FirebaseConstants.POSTTIME, createdTime);

                                    postRef.child(communityID).child(fileName.replaceAll("[^a-zA-Z0-9]", "")).setValue(uploadmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                imgUploaded++;
//                                            allCommunityImages.get(imgPosition).setQueued(false);
                                                String fileName = Uri.fromFile(new File(uri)).getLastPathSegment();
                                                UploadQueueDB uploadQueueDB = new UploadQueueDB(getApplicationContext());
                                                boolean result = uploadQueueDB.deleteData(fileName);
                                                Log.i("uploading", "deleted " + fileName + " result " + result);


                                                if (imgUploaded == queuedImages.size()) {
                                                    notificationManager.cancel(Integer.parseInt(id.substring(id.length()-4)));
                                                    NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
                                                    String message = "Uploaded " + imgUploaded + " out of " + queuedImages.size() + " photo(s). ";
                                                    notificationHelper.displayTitleMesageNoti("Photos Uploaded", message);
                                                } else if (failedCount > 0 && imgUploaded + failedCount == queuedImages.size()) {
                                                    notificationManager.cancel(Integer.parseInt(id.substring(id.length()-4)));
                                                    NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
                                                    String message = "Uploaded " + imgUploaded + " out of " + queuedImages.size() + " photo(s). Remaining photos will be uploaded later.";
                                                    notificationHelper.displayTitleMesageNoti("Photos Uploaded", message);

                                                } else {
                                                    queuedImages.get(imgPosition).setQueued(false);
                                                    SharedPreferences notificationPref = context.getSharedPreferences(AppConstants.NOTIFICATION_PREF, Context.MODE_PRIVATE);
                                                    String repeatId = notificationPref.getString("id", String.valueOf(System.currentTimeMillis()));
                                                    if (id.equals(repeatId)) {
                                                        uploadToFirebase(queuedImages, id);

                                                    } else {
                                                        notificationManager.cancel(Integer.parseInt(id.substring(id.length() - 4)));
                                                    }
                                                }

                                            } else {

                                                failedCount++;
//                                            allCommunityImages.get(imgPosition).setQueued(false);


                                                if (imgUploaded == queuedImages.size()) {
                                                    notificationManager.cancel(Integer.parseInt(id.substring(id.length()-4)));
                                                    NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
                                                    String message = "Uploaded " + imgUploaded + " out of " + queuedImages.size() + " photo(s). ";
                                                    notificationHelper.displayTitleMesageNoti("Photos Uploaded", message);
                                                } else if (failedCount > 0 && imgUploaded + failedCount == queuedImages.size()) {
                                                    notificationManager.cancel(Integer.parseInt(id.substring(id.length()-4)));
                                                    NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
                                                    String message = "Uploaded " + imgUploaded + " out of " + queuedImages.size() + " photo(s). Remaining photos will be uploaded later.";
                                                    notificationHelper.displayTitleMesageNoti("Photos Uploaded", message);

                                                } else {
                                                    queuedImages.get(imgPosition).setQueued(false);
                                                    SharedPreferences notificationPref = context.getSharedPreferences(AppConstants.NOTIFICATION_PREF, Context.MODE_PRIVATE);
                                                    String repeatId = notificationPref.getString("id", String.valueOf(System.currentTimeMillis()));
                                                    if (id.equals(repeatId)) {
                                                        uploadToFirebase(queuedImages, id);

                                                    } else {
                                                        notificationManager.cancel(Integer.parseInt(id.substring(id.length() - 4)));
                                                    }
                                                }

                                            }
                                        }
                                    });

                                }
                            });

                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            int PROGRESS_CURRENT = (int) ((100.0 * ((double) (taskSnapshot.getBytesTransferred()))) / ((double) taskSnapshot.getTotalByteCount()));
                            Log.i("imguploader", "progress of " + imgUploaded + " : " + PROGRESS_CURRENT);

                            int uploadingCount = imgUploaded + 1;

                            builder.setContentText("Uploading photo " + uploadingCount + " of " + queuedImages.size()).setProgress(100, PROGRESS_CURRENT, false);
                            notificationManager.notify(Integer.parseInt(id.substring(id.length()-4)), builder.build());

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            failedCount++;

//                        allCommunityImages.get(imgPosition).setQueued(false);


                            if (imgUploaded == queuedImages.size()) {
                                notificationManager.cancel(Integer.parseInt(id.substring(id.length()-4)));
                                NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
                                String message = "Uploaded " + imgUploaded + " out of " + queuedImages.size() + " photo(s). ";
                                notificationHelper.displayTitleMesageNoti("Photos Uploaded", message);
                            } else if (failedCount > 0 && imgUploaded + failedCount == queuedImages.size()) {
                                notificationManager.cancel(Integer.parseInt(id.substring(id.length()-4)));
                                NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
                                String message = "Uploaded " + imgUploaded + " out of " + queuedImages.size() + " photo(s). ";
                                notificationHelper.displayTitleMesageNoti("Photos Uploaded", message);

                            } else {
                                queuedImages.get(imgPosition).setQueued(false);
                                SharedPreferences notificationPref = context.getSharedPreferences(AppConstants.NOTIFICATION_PREF, Context.MODE_PRIVATE);
                                String repeatId = notificationPref.getString("id", String.valueOf(System.currentTimeMillis()));
                                if (id.equals(repeatId)) {
                                    uploadToFirebase(queuedImages, id);

                                } else {
                                    notificationManager.cancel(Integer.parseInt(id.substring(id.length() - 4)));
                                }
                            }
                        }
                    });

                }


            }
        }


    }

    private int orientation(Bitmap result) {

        if (result.getHeight() > result.getWidth()) {
            return PORTRAIT;
        } else {
            return LANDSCAPE;
        }

    }

    public Bitmap compressUploadFile(File bitmapFile) {
        try {
            Bitmap result = new Compressor(getApplicationContext())
                    .compressToBitmap(bitmapFile);
            if (orientation(result) == PORTRAIT) {
                result = new Compressor(getApplicationContext())
                        .setQuality(90)
                        .setMaxHeight(640)
                        .setMaxWidth(480)
                        .compressToBitmap(bitmapFile);

            } else {
                result = new Compressor(getApplicationContext())
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


}
