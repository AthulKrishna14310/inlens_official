package com.integrals.inlens.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.integrals.inlens.Helper.AppConstants;

public class TagsQueueDB extends SQLiteOpenHelper {

    public TagsQueueDB(@Nullable Context context) {
        super(context, AppConstants.TAGS_SQLITE_DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("create table "+AppConstants.TAGS_SQLITE_TABLE_NAME +" ("+AppConstants.TAGS_SQLITE_COLUMN_FIELD_ID +" TEXT PRIMARY KEY )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("drop table if exists "+AppConstants.TAGS_SQLITE_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean insertData(String tag)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(AppConstants.TAGS_SQLITE_COLUMN_FIELD_ID,tag);
        long result = db.insert(AppConstants.TAGS_SQLITE_TABLE_NAME,null,contentValues);
        return result != -1;
    }

    public Cursor getQueuedData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from "+ AppConstants.TAGS_SQLITE_TABLE_NAME,null);
    }
    public boolean deleteData(String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(AppConstants.TAGS_SQLITE_TABLE_NAME,AppConstants.TAGS_SQLITE_COLUMN_FIELD_ID +"=?",new String[]{name});
        return result != 0;
    }
    
}
