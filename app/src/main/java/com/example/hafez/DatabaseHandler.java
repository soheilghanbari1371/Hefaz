package com.example.hafez;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.util.ArrayList;

public class DatabaseHandler {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;


    public DatabaseHandler(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    //methods for all table
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void clearTable(String tableName) {
        database.delete( tableName, null, null);
    }

    //news table method

    public void insertNewsInfo(NewsInformation newsInfo) {
        ContentValues cv = new ContentValues();
        cv.put("title"          ,  newsInfo.title );
        cv.put("link"           ,  newsInfo.link );
        cv.put("pubdate"        ,  newsInfo.pubdate );
        cv.put("description"       ,  newsInfo.description );
        cv.put("image"       ,  newsInfo.image );


        database.insert("news" , "writerName", cv);
    }


    public ArrayList<NewsInformation> getAllNews() {
        ArrayList<NewsInformation> NewsInfoList = new ArrayList<NewsInformation>();

        Cursor cursor = database.rawQuery("SELECT *  FROM  news" ,new String[]{});

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            NewsInformation newsInfo = new NewsInformation();

            newsInfo.title = cursor.getString(0);
            newsInfo.link = cursor.getString(1);
            newsInfo.pubdate = cursor.getString(2);
            newsInfo.description = cursor.getString(3);

            NewsInfoList.add(newsInfo);
            cursor.moveToNext();
        }

        // Make sure to close the cursor
        cursor.close();

        return NewsInfoList;
    }

    public String getBigBody(String completeBodyLink) {
        Cursor cursor = database.rawQuery("select bigBody FROM news where completeTextLink = ?", new String[]{completeBodyLink});
        cursor.moveToFirst();
        String bigBody = cursor.getString(0);
        cursor.close();
        return bigBody;
    }
}
