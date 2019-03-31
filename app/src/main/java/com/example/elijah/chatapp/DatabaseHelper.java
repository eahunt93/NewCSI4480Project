package com.example.elijah.chatapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by elija on 3/25/2019.
 */

public class DatabaseHelper extends SQLiteOpenHelper {



    private static final String TABLE_NAME = "my_messages";

    private static final String COL1 = "sent_to";

    private static final String COL2 = "message";

    private static  final String COL3 = "time";



    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String createTable = "CREATE TABLE " + TABLE_NAME + " (" + COL1+ " TEXT," + COL2 + " TEXT," + COL3 + " INTEGER)";
        sqLiteDatabase.execSQL(createTable);



    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String drop = "DROP IF TABLE EXISTS " + TABLE_NAME;
        sqLiteDatabase.execSQL(drop);
        onCreate(sqLiteDatabase);
    }

    public boolean addData(String item1 , String item2, long item3){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1,item1);
        contentValues.put(COL2,item2);
        contentValues.put(COL3,item3);

        long result = db.insert(TABLE_NAME,null,contentValues);

        if(result == -1){
            return false;
        }else{
            return true;
        }


    }

    public ArrayList<Messages> getData(String UserName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "Select " + COL1 + ", " + COL2 + ", " + COL3 + " WHERE " + COL1 + " = " + UserName;
        Messages messages = new Messages();
        ArrayList<Messages> idk = new ArrayList<>();
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            messages.setMessage(COL2);
            messages.setFrom(COL1);
            messages.setTime(Long.parseLong(COL3));
            idk.add(messages);
        }
        return idk;


    }
}
