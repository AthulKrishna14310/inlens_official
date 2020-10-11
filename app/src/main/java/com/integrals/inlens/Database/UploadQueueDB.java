package com.integrals.inlens.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.integrals.inlens.Helper.AppConstants;

public class UploadQueueDB extends SQLiteOpenHelper {

    public UploadQueueDB(@Nullable Context context) {
        super(context, AppConstants.UPLOAD_SQLITE_DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("create table "+AppConstants.UPLOAD_SQLITE_TABLE_NAME +" ("+AppConstants.UPLOAD_SQLITE_COLUMN_FIELD_ID +" TEXT PRIMARY KEY ,"+AppConstants.UPLOAD_SQLITE_COLUMN_FIELD_URI +" TEXT, "+AppConstants.UPLOAD_SQLITE_COLUMN_FIELD_QUEUED_TIME +" TEXT )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("drop table if exists "+AppConstants.UPLOAD_SQLITE_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean insertData(String name,String uri,String time)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(AppConstants.UPLOAD_SQLITE_COLUMN_FIELD_ID,name);
        contentValues.put(AppConstants.UPLOAD_SQLITE_COLUMN_FIELD_URI,uri);
        contentValues.put(AppConstants.UPLOAD_SQLITE_COLUMN_FIELD_QUEUED_TIME,time);
        long result = db.insert(AppConstants.UPLOAD_SQLITE_TABLE_NAME,null,contentValues);
        return result != -1;
    }

    public Cursor getQueuedData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from "+ AppConstants.UPLOAD_SQLITE_TABLE_NAME,null);
    }

    public boolean deleteData(String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(AppConstants.UPLOAD_SQLITE_TABLE_NAME,AppConstants.UPLOAD_SQLITE_COLUMN_FIELD_ID +"=?",new String[]{name});
        return result != 0;
    }

    public void deleteAllData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+AppConstants.UPLOAD_SQLITE_TABLE_NAME);
    }
    
}
