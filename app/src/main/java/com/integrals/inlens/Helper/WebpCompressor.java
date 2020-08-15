package com.integrals.inlens.Helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class WebpCompressor {
    private static final int THUMBNAIL_QUALITY = 0;
    private static final int COMPRESSED_QUALITY = 5;
    private static final int SIZE_RATIO = 3;
    private Bitmap originalBitmap;
    private Bitmap compressedBitmap=null;
    private Bitmap thumbnailBitmap=null;
    private Uri filePathUri;
    private Context context;
    private File compressedImageFile=null;
    private File thumbnailImageFile=null;
    private FileOutputStream outputStream=null;

    public WebpCompressor(Uri filePathUri,
                          Context context) {
        this.filePathUri = filePathUri;
        this.context = context;
        originalBitmap=uriToBitmap(filePathUri);

    }


    public String compressedImageUrl(){
        String imagePath=null;
        File filePath=context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File dir=new File(filePath.getAbsolutePath()+"/Demo/");
        dir.mkdir();
        compressedImageFile  =new File(dir,System.currentTimeMillis()+".webp");
        try {
            outputStream=new FileOutputStream(compressedImageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("WebpCompressor","error "+e);
        }
        compressedBitmap=originalBitmap;
        compressedBitmap.compress(Bitmap.CompressFormat.WEBP,COMPRESSED_QUALITY,outputStream);
        imagePath=compressedImageFile.getAbsolutePath();
        return imagePath;
    }

    public String thumbnailCompressedImageUrl(){
        String imagePath=null;
        File filePath=context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File dir=new File(filePath.getAbsolutePath()+"/Demo/");
        dir.mkdir();
        thumbnailImageFile =new File(dir,System.currentTimeMillis()+".webp");
        try {
            outputStream=new FileOutputStream(thumbnailImageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        thumbnailBitmap=originalBitmap;
        thumbnailBitmap=Bitmap.createScaledBitmap(originalBitmap, originalBitmap.getWidth()/SIZE_RATIO, originalBitmap.getHeight()/SIZE_RATIO, false);
        thumbnailBitmap.compress(Bitmap.CompressFormat.WEBP,THUMBNAIL_QUALITY,outputStream);
        imagePath=thumbnailImageFile.getAbsolutePath();
        return imagePath;
    }

    private boolean deleteCompressedFile(){
        boolean result=false;
        compressedImageFile.delete();
        result=true;
        return result;
    }

    private boolean deleteThumbnailFile(){
        boolean result=false;
        thumbnailImageFile.delete();
        result=true;
        return result;
    }

    private Bitmap uriToBitmap(Uri selectedFileUri) {
        Bitmap bitmap=null;
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(selectedFileUri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);

            parcelFileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("WebpCompressor","error is "+e);
        }
        return bitmap;
    }

    public static int getThumbnailQuality() {
        return THUMBNAIL_QUALITY;
    }

    public static int getCompressedQuality() {
        return COMPRESSED_QUALITY;
    }

    public static int getSizeRatio() {
        return SIZE_RATIO;
    }

    public Bitmap getOriginalBitmap() {
        return originalBitmap;
    }

    public void setOriginalBitmap(Bitmap originalBitmap) {
        this.originalBitmap = originalBitmap;
    }

    public Bitmap getCompressedBitmap() {
        return compressedBitmap;
    }

    public void setCompressedBitmap(Bitmap compressedBitmap) {
        this.compressedBitmap = compressedBitmap;
    }

    public Bitmap getThumbnailBitmap() {
        return thumbnailBitmap;
    }

    public void setThumbnailBitmap(Bitmap thumbnailBitmap) {
        this.thumbnailBitmap = thumbnailBitmap;
    }

    public Uri getFilePathUri() {
        return filePathUri;
    }

    public void setFilePathUri(Uri filePathUri) {
        this.filePathUri = filePathUri;
    }


    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public File getCompressedImageFile() {
        return compressedImageFile;
    }

    public void setCompressedImageFile(File compressedImageFile) {
        this.compressedImageFile = compressedImageFile;
    }

    public File getThumbnailImageFile() {
        return thumbnailImageFile;
    }

    public void setThumbnailImageFile(File thumbnailImageFile) {
        this.thumbnailImageFile = thumbnailImageFile;
    }

    public FileOutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(FileOutputStream outputStream) {
        this.outputStream = outputStream;
    }
}
