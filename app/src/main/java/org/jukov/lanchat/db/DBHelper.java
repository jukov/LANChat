package org.jukov.lanchat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.PeopleData;

import java.util.ArrayList;
import java.util.Arrays;
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

//    private int peopleRows;
//    private int privateMessagesRows;
//    private int publicMessagesRows;

    public static final String KEY_NAME = "key_name";
    public static final String KEY_UID = "key_uid";
    public static final String KEY_MESSAGE = "key_message";
    public static final String KEY_DATE = "key_date";
    public static final String ID_PEOPLE = "id_people";
    public static final String TABLE_PEOPLE = "people";
    public static final String TABLE_PRIVATE_MESSAGES = "private_messages";
    public static final String TABLE_PUBLIC_MESSAGES = "public_messages";

    public static final String QUERY_CREATE_PEOPLE = "CREATE TABLE " + TABLE_PEOPLE +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_NAME + " TEXT, " +
            KEY_UID + " TEXT);";

    public static final String QUERY_CREATE_PRIVATE_MESSAGES = "CREATE TABLE " + TABLE_PRIVATE_MESSAGES +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ID_PEOPLE + " INTEGER, " +
            KEY_MESSAGE + " TEXT, " +
            KEY_DATE + " TEXT);";

    public static final String QUERY_CREATE_PUBLIC_MESSAGES = "CREATE TABLE " + TABLE_PUBLIC_MESSAGES +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ID_PEOPLE + " INTEGER, " +
            KEY_MESSAGE + " TEXT, " +
            KEY_DATE + " TEXT);";

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
        db.execSQL(QUERY_CREATE_PEOPLE);
        db.execSQL(QUERY_CREATE_PUBLIC_MESSAGES);
        db.execSQL(QUERY_CREATE_PRIVATE_MESSAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertMessage(ChatData chatData) {
        databaseLock.lock();
        Cursor cursor = sqLiteDatabase.query(
                TABLE_PEOPLE,
                new String[]{"_id"},
                KEY_UID + " = ?",
                new String[]{chatData.getUid()},
                null, null, null);

        cursor.moveToFirst();
        contentValues.put(ID_PEOPLE, cursor.getInt(0));
        contentValues.put(KEY_DATE, chatData.getSendDate());
        contentValues.put(KEY_MESSAGE, chatData.getText());

        switch (chatData.getMessageType()) {
            case GLOBAL:
                sqLiteDatabase.insert(TABLE_PUBLIC_MESSAGES, null, contentValues);
                break;
            case PRIVATE:
                sqLiteDatabase.insert(TABLE_PRIVATE_MESSAGES, null, contentValues);
        }
        contentValues.clear();
        cursor.close();
        databaseLock.unlock();
    }

    public void insertOrRenamePeople(PeopleData peopleData) {
        databaseLock.lock();
        contentValues.put(KEY_NAME, peopleData.getName());
        contentValues.put(KEY_UID, peopleData.getUid());
        if (sqLiteDatabase.update(
                TABLE_PEOPLE,
                contentValues,
                KEY_UID + " = ?",
                new String[] {peopleData.getUid()}) == 0) {
            sqLiteDatabase.insert(TABLE_PEOPLE, null, contentValues);
        }
        contentValues.clear();
        databaseLock.unlock();
    }

    public List<String> getPublicMessages() {
        databaseLock.lock();

        Cursor peopleCursor = sqLiteDatabase.query(
                TABLE_PEOPLE,
                new String[] {"_id", KEY_NAME},
                null, null, null, null, null);

        HashMap<Integer, String> peopleMap = new HashMap<>();
        peopleCursor.moveToFirst();
        if (peopleCursor.getCount() > 0 ) {
            do {
                peopleMap.put(peopleCursor.getInt(0), peopleCursor.getString(1));
            } while (peopleCursor.moveToNext());
            }
        peopleCursor.close();

        Cursor messagesCursor = sqLiteDatabase.query(
                TABLE_PUBLIC_MESSAGES,
                new String[] {ID_PEOPLE , KEY_MESSAGE},
                null, null, null, null, null);

        List<String> messagesList = new ArrayList<>();
        messagesCursor.moveToFirst();
        if (messagesCursor.getCount() > 0 ) {
            do {
                messagesList.add(peopleMap.get(messagesCursor.getInt(0)) + ": " + messagesCursor.getString(1));
            } while (messagesCursor.moveToNext());
        }
        messagesCursor.close();

        databaseLock.unlock();
        return messagesList;
    }

    public List<String> getPrivateMessages(String myUID, String companionUID) {
        databaseLock.lock();

        Cursor peopleCursor = sqLiteDatabase.query(
                TABLE_PEOPLE,
                new String[] {"_id, " + KEY_NAME},
                KEY_UID + " = ? OR " + KEY_UID + " = ?",
                new String[] {myUID, companionUID},
                null, null, null);

        HashMap<Integer, String> peopleMap = new HashMap<>();
        peopleCursor.moveToFirst();
        if (peopleCursor.getCount() > 0 ) {
            do {
                peopleMap.put(peopleCursor.getInt(0), peopleCursor.getString(1));
            } while (peopleCursor.moveToNext());
        }

        Object[] objectIds = peopleMap.keySet().toArray();
        Integer[] ids = Arrays.copyOf(objectIds, objectIds.length, Integer[].class);
        peopleCursor.close();

        Cursor messagesCursor = sqLiteDatabase.query(
                TABLE_PRIVATE_MESSAGES,
                new String[] {ID_PEOPLE , KEY_MESSAGE},
                ID_PEOPLE + " = ? OR " + ID_PEOPLE + " = ?",
                new String[] {ids[0].toString(), ids[1].toString()},
                null, null, null);

        List<String> messagesList = new ArrayList<>();
        messagesCursor.moveToFirst();
        if (messagesCursor.getCount() > 0 ) {
            do {
                messagesList.add(peopleMap.get(messagesCursor.getInt(0)) + ": " + messagesCursor.getString(1));
            } while (messagesCursor.moveToNext());
        }

        databaseLock.unlock();
        return messagesList;
    }

    @Override
    public synchronized void close() {
        databaseLock.lock();
        sqLiteDatabase.close();
        super.close();
        instance = null;
//        databaseLock.unlock();
    }
}
