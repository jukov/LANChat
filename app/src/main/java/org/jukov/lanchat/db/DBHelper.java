package org.jukov.lanchat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by jukov on 13.03.2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static volatile DBHelper instance;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "LANChatDatabase";

    private SQLiteDatabase sqLiteDatabase;
    private ContentValues contentValues;

    private Lock databaseLock;

    private int peopleRows;
    private int privateMessagesRows;
    private int publicMessagesRows;

    public static DBHelper getInstance(Context context) {
        DBHelper localInstance = instance;
        if (localInstance == null) {
            synchronized (DBHelper.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new DBHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
                }
            }
        }
        return localInstance;
    }

    private DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        sqLiteDatabase = this.getReadableDatabase();
        contentValues = new ContentValues(1);
        databaseLock = new ReentrantLock();

//        peopleRows = (int) DatabaseUtils.queryNumEntries(sqLiteDatabase, Constants.DatabaseConstants.TABLE_PEOPLE);
//        privateMessagesRows = (int) DatabaseUtils.queryNumEntries(sqLiteDatabase, Constants.DatabaseConstants.TABLE_PRIVATE_MESSAGES);
//        publicMessagesRows = (int) DatabaseUtils.queryNumEntries(sqLiteDatabase, Constants.DatabaseConstants.TABLE_PUBLIC_MESSAGES);
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
        databaseLock.lock();
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
        cursor.close();
        databaseLock.unlock();
    }

    public void insertOrRenamePeople(PeopleData peopleData) {
        databaseLock.lock();
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
        databaseLock.unlock();
    }

    public List<String> getPublicMessages() {
        databaseLock.lock();

        Cursor messagesCursor = sqLiteDatabase.query(
                Constants.DatabaseConstants.TABLE_PUBLIC_MESSAGES,
                new String[] {Constants.DatabaseConstants.ID_PEOPLE , Constants.DatabaseConstants.KEY_MESSAGE},
                null, null, null, null, null);

        Cursor peopleCursor = sqLiteDatabase.query(
                Constants.DatabaseConstants.TABLE_PEOPLE,
                new String[] {"_id", Constants.DatabaseConstants.KEY_NAME},
                null, null, null, null, null);

        HashMap<Integer, String> peopleMap = new HashMap<>();
        peopleCursor.moveToFirst();
        while (peopleCursor.moveToNext()) {
            peopleMap.put(peopleCursor.getInt(0), peopleCursor.getString(1));
        }

        List<String> messagesList = new ArrayList<>();
        messagesCursor.moveToFirst();
        while (messagesCursor.moveToNext()) {
            messagesList.add(peopleMap.get(messagesCursor.getInt(0)) + ": " + messagesCursor.getString(1));
        }

        messagesCursor.close();
        peopleCursor.close();
        databaseLock.unlock();
        return messagesList;
    }

    public List<String> getPrivateMessages() {
        databaseLock.lock();

        Cursor messagesCursor = sqLiteDatabase.query(
                Constants.DatabaseConstants.TABLE_PUBLIC_MESSAGES,
                new String[] {Constants.DatabaseConstants.ID_PEOPLE , Constants.DatabaseConstants.KEY_MESSAGE},
                null, null, null, null, null);

        Cursor peopleCursor = sqLiteDatabase.query(
                Constants.DatabaseConstants.TABLE_PEOPLE,
                new String[] {"_id", Constants.DatabaseConstants.KEY_NAME},
                null, null, null, null, null);

        HashMap<Integer, String> peopleMap = new HashMap<>();
        peopleCursor.moveToFirst();
        while (peopleCursor.moveToNext()) {
            peopleMap.put(peopleCursor.getInt(0), peopleCursor.getString(1));
        }

        List<String> messagesList = new ArrayList<>();
        messagesCursor.moveToFirst();
        while (messagesCursor.moveToNext()) {
            messagesList.add(peopleMap.get(messagesCursor.getInt(0)) + ": " + messagesCursor.getString(1));
        }

        messagesCursor.close();
        peopleCursor.close();
        databaseLock.unlock();
        return messagesList;
    }

    @Override
    public synchronized void close() {
        databaseLock.lock();
        sqLiteDatabase.close();
        super.close();
        databaseLock.unlock();
    }
}
