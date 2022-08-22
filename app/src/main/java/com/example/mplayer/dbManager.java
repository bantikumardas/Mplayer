package com.example.mplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class dbManager extends SQLiteOpenHelper {

    public static final String dbName="data.db";
    public  static  final int version=1;

    public dbManager(Context context)
    {
        super(context,dbName,null,version);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlQuery="create table student(id integer primary key autoincrement , position text)";
        db.execSQL(sqlQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS student");
        onCreate(db);
    }
    public int addrecord(int name)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("position",name+"");
        long res= db.insert("student",null,cv);
        if(res==-1) return 0;
        else return 1;
    }
    public Cursor getData()
    {
        Cursor cursor = null;
        SQLiteDatabase db=this.getWritableDatabase();
        String qry="SELECT *     FROM    student WHERE   ID = (SELECT MAX(ID)  FROM student)";
        try {
            cursor = db.rawQuery(qry,null);
        }catch (Exception exception){

        }

        return cursor;
    }
}
