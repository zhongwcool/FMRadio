package com.gst.fmradio.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by yyshang on 5/25/15.
 */
public class DBHelper extends SQLiteOpenHelper {
    //数据库的创建和增删改除
    private final static int VERSION = 1;
    private final static String DB_NAME = "fm.db";
    private final static String TABLE_NAME = "channels";
    private final static String CREATE_TAB_SHOW = "CREATE TABLE " + TABLE_NAME + "(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,channelnum INTEGER ,collectStatus INTEGER)";

    private SQLiteDatabase db;

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DBHelper(Context context) {
        this(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建表
        this.db = db;
        System.out.println("Create Database");
        db.execSQL(CREATE_TAB_SHOW);
        db.execSQL("insert into channels (name,channelnum,collectStatus) values('FmDfltSttbNm',870,1)");
        db.execSQL("insert into channels (name,channelnum,collectStatus) values('DISABLE',1,4)");
        db.execSQL("insert into channels (name,channelnum,collectStatus) values('DISABLE',2,4)");
        db.execSQL("insert into channels (name,channelnum,collectStatus) values('DISABLE',3,4)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        System.out.println("updata Database");

    }

    public void insert(ContentValues values) {
        //插入数据
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_NAME, null, values);

        db.close();
    }


    public Cursor query() {
        //查询数据
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null, null);
        return c;

    }

    public void delete(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(id)});
    }


    public void update(ContentValues values, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_NAME, values, whereClause, whereArgs);

        db.close();
    }

    public void close() {
        if (db != null) {
            db.close();
        }
    }

}
