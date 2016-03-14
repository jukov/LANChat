package org.jukov.lanchat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jukov.lanchat.util.Constants;

/**
 * Created by jukov on 13.03.2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "LANChatDatabase";

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Constants.DatabaseConstants.QUERY_CREATE_PEOPLE);
        db.execSQL(Constants.DatabaseConstants.QUERY_CREATE_PUBLIC_MESSAGES);
        db.execSQL(Constants.DatabaseConstants.QUERY_CREATE_PRIVATE_MESSAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
