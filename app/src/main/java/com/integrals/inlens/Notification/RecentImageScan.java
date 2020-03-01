package com.integrals.inlens.Notification;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.integrals.inlens.Models.GalleryImageModel;
import com.integrals.inlens.Models.UnNotifiedImageModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecentImageScan {

    Context context;
    long lastnotifiedtime;

    public RecentImageScan() {
    }

    public RecentImageScan(Context context, long lastnotifiedtime) {
        this.context = context;
        this.lastnotifiedtime = lastnotifiedtime;
    }

    public UnNotifiedImageModel checkForNotifiedImageExist()
    {
        UnNotifiedImageModel unNotifiedImageModel=new UnNotifiedImageModel(null,null);
        Uri uri;
        Cursor cursor;
        int column_index_data;
        String absolutePathOfImage = null;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        cursor = context.getContentResolver().query(uri, projection, null, null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToLast();

        // currently detect all images we need to modify it to detect only images in camera

        absolutePathOfImage = cursor.getString(column_index_data);
        File img = new File(absolutePathOfImage);
        if (img.lastModified() > lastnotifiedtime) {

            unNotifiedImageModel.setUri(absolutePathOfImage);
            unNotifiedImageModel.setCreatedTime(String.valueOf(img.lastModified()));
        }

        return unNotifiedImageModel;
    }

    public List<GalleryImageModel> getAllShownImagesPath() {

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
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        cursor = context.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            File img = new File(absolutePathOfImage);
            if (img.lastModified() > lastnotifiedtime && !absolutePathOfImage.toLowerCase().contains("screenshot") && !absolutePathOfImage.toLowerCase().contains("whatsapp")) {

                String lastsegmentedpath = Uri.fromFile(new File(absolutePathOfImage)).getLastPathSegment();
                Toast.makeText(context, "last path : "+lastsegmentedpath, Toast.LENGTH_SHORT).show();
                if (!listOfAllImages.contains(lastsegmentedpath)) {

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
}
