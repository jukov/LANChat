package org.jukov.lanchat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.util.Constants;

/**
 * Created by jukov on 13.03.2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "LANChatDatabase";

    private SQLiteDatabase sqLiteDatabase;
    private ContentValues contentValues;

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        sqLiteDatabase = this.getReadableDatabase();
        contentValues = new ContentValues(1);
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

    public void insertMessage(ChatData chatData) {
        Cursor cursor = sqLiteDatabase.query(
                Constants.DatabaseConstants.TABLE_PEOPLE,
                new String[]{"_id"},
                Constants.DatabaseConstants.KEY_MAC + " = ?",
                new String[]{chatData.getUid()},
                null, null, null);

        cursor.moveToFirst();
        contentValues.put(Constants.DatabaseConstants.ID_PEOPLE, cursor.getInt(0));
        contentValues.put(Constants.DatabaseConstants.KEY_DATE, chatData.getSendDate());
        contentValues.put(Constants.DatabaseConstants.KEY_MESSAGE, chatData.getText());

        switch (chatData.getMessageType()) {
            case GLOBAL:
                sqLiteDatabase.insert(Constants.DatabaseConstants.TABLE_PUBLIC_MESSAGES, null, contentValues);
                break;
            case PRIVATE:
                sqLiteDatabase.insert(Constants.DatabaseConstants.TABLE_PRIVATE_MESSAGES, null, contentValues);
        }
        contentValues.clear();
    }

    public void insertOrRenamePeople(PeopleData peopleData) {
        contentValues.put(Constants.DatabaseConstants.KEY_NAME, peopleData.getName());
        contentValues.put(Constants.DatabaseConstants.KEY_MAC, peopleData.getUid());
        if (sqLiteDatabase.update(
                Constants.DatabaseConstants.TABLE_PEOPLE,
                contentValues,
                Constants.DatabaseConstants.KEY_MAC + " = ?",
                new String[] {peopleData.getUid()}) == 0) {
            sqLiteDatabase.insert(Constants.DatabaseConstants.TABLE_PEOPLE, null, contentValues);
        }
        contentValues.clear();
    }
}
