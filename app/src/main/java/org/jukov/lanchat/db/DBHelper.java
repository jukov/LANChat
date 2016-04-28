package org.jukov.lanchat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.dto.RoomData;

import java.util.AbstractCollection;
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

    public static final int DATABASE_VERSION = 9;
    public static final String DATABASE_NAME = "LANChatDatabase";

    private SQLiteDatabase sqLiteDatabase;
    private ContentValues contentValues;

    private Lock databaseLock;

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "key_name";
    public static final String KEY_UID = "key_uid";
    public static final String KEY_MESSAGE = "key_message";
    public static final String KEY_DATE = "key_date";
    public static final String KEY_ID_PEOPLE = "id_people";
    public static final String KEY_ID_RECEIVER = "id_receiver";
    public static final String KEY_ID_ROOM = "id_room";
    public static final String TABLE_PEOPLE = "people";
    public static final String TABLE_PRIVATE_MESSAGES = "private_messages";
    public static final String TABLE_PUBLIC_MESSAGES = "public_messages";
    public static final String TABLE_ROOMS = "rooms";
    public static final String TABLE_ROOMS_MESSAGES = "rooms_messages";


    public static final String QUERY_CREATE_PEOPLE = "CREATE TABLE " + TABLE_PEOPLE +
            "(" + KEY_ID +" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            KEY_NAME + " TEXT NOT NULL," +
            KEY_UID + " TEXT NOT NULL" +
            ");";

    public static final String QUERY_CREATE_PRIVATE_MESSAGES = "CREATE TABLE " + TABLE_PRIVATE_MESSAGES +
            "(" + KEY_ID +" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            KEY_ID_PEOPLE + " INTEGER NOT NULL," +
            KEY_ID_RECEIVER + " INTEGER NOT NULL," +
            KEY_MESSAGE + " TEXT NOT NULL," +
            KEY_DATE + " TEXT NOT NULL," +
            "FOREIGN KEY(" + KEY_ID_PEOPLE + ") REFERENCES " + TABLE_PEOPLE + "(" + KEY_ID + ")," +
            "FOREIGN KEY(" + KEY_ID_RECEIVER + ") REFERENCES " + TABLE_PEOPLE + "(" + KEY_ID + ")" +
            ");";

    public static final String QUERY_CREATE_PUBLIC_MESSAGES = "CREATE TABLE " + TABLE_PUBLIC_MESSAGES +
            "(" + KEY_ID +" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            KEY_ID_PEOPLE + " INTEGER NOT NULL," +
            KEY_MESSAGE + " TEXT NOT NULL," +
            KEY_DATE + " TEXT NOT NULL," +
            "FOREIGN KEY(" + KEY_ID_PEOPLE + ") REFERENCES " + TABLE_PEOPLE + "(" + KEY_ID + ")" +
            ");";

    public static final String QUERY_CREATE_ROOMS = "CREATE TABLE " + TABLE_ROOMS +
            "(" + KEY_ID +" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            KEY_NAME + " TEXT NOT NULL," +
            KEY_UID + " TEXT NOT NULL" +
            ");";

    public static final String QUERY_CREATE_ROOMS_MESSAGES = "CREATE TABLE " + TABLE_ROOMS_MESSAGES +
            "(" + KEY_ID +" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            KEY_ID_ROOM + " INTEGER NOT NULL," +
            KEY_ID_PEOPLE + " INTEGER NOT NULL," +
            KEY_MESSAGE + " TEXT NOT NULL," +
            KEY_DATE + " TEXT NOT NULL," +
            "FOREIGN KEY(" + KEY_ID_PEOPLE + ") REFERENCES " + TABLE_PEOPLE + "(" + KEY_ID + ")," +
            "FOREIGN KEY(" + KEY_ID_ROOM + ") REFERENCES " + TABLE_ROOMS + "(" + KEY_ID + ")" +
            ");";

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
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(QUERY_CREATE_PEOPLE);
        db.execSQL(QUERY_CREATE_PUBLIC_MESSAGES);
        db.execSQL(QUERY_CREATE_PRIVATE_MESSAGES);
        db.execSQL(QUERY_CREATE_ROOMS);
        db.execSQL(QUERY_CREATE_ROOMS_MESSAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE " + TABLE_PEOPLE);
//        db.execSQL("DROP TABLE " + TABLE_ROOMS);
//        db.execSQL("DROP TABLE " + TABLE_PRIVATE_MESSAGES);
//        db.execSQL("DROP TABLE " + TABLE_PUBLIC_MESSAGES);
//        db.execSQL("DROP TABLE " + TABLE_ROOMS_MESSAGES);
        onCreate(db);
    }

    public void insertMessage(ChatData chatData) {
        databaseLock.lock();
        Cursor cursorSender = sqLiteDatabase.query(
                TABLE_PEOPLE,
                new String[]{KEY_ID},
                KEY_UID + " = ?",
                new String[]{chatData.getUid()},
                null, null, null);

        cursorSender.moveToFirst();
        contentValues.put(KEY_ID_PEOPLE, cursorSender.getInt(0));
        contentValues.put(KEY_DATE, chatData.getSendDate());
        contentValues.put(KEY_MESSAGE, chatData.getText());

        switch (chatData.getMessageType()) {
            case GLOBAL:
                sqLiteDatabase.insert(TABLE_PUBLIC_MESSAGES, null, contentValues);
                break;
            case PRIVATE:
                Cursor cursorReceiver = sqLiteDatabase.query(
                        TABLE_PEOPLE,
                        new String[]{KEY_ID},
                        KEY_UID + " = ?",
                        new String[]{chatData.getReceiverUID()},
                        null, null, null);
                cursorReceiver.moveToFirst();
                contentValues.put(KEY_ID_RECEIVER, cursorReceiver.getInt(0));
                sqLiteDatabase.insert(TABLE_PRIVATE_MESSAGES, null, contentValues);
                cursorReceiver.close();
        }
        contentValues.clear();
        cursorSender.close();
        databaseLock.unlock();
    }

    public void insertMessages(AbstractCollection<ChatData> messages) {
        databaseLock.lock();
        Cursor cursor = sqLiteDatabase.query(
                TABLE_PEOPLE,
                new String[]{KEY_ID, KEY_UID},
                null, null, null, null, null);

        HashMap<String, Integer> peopleIDs = new HashMap<>();
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            do {
                peopleIDs.put(cursor.getString(1), cursor.getInt(0));
            } while (cursor.moveToNext());
        }
        cursor.close();

        for (ChatData chatData : messages) {
            contentValues.put(KEY_ID_PEOPLE, peopleIDs.get(chatData.getUid()));
            contentValues.put(KEY_MESSAGE, chatData.getText());
            contentValues.put(KEY_DATE, chatData.getSendDate());
            sqLiteDatabase.insert(TABLE_PUBLIC_MESSAGES, null, contentValues);
            contentValues.clear();
        }
        databaseLock.unlock();
    }


    public void insertRooms(AbstractCollection<RoomData> rooms) {
        databaseLock.lock();

        for (RoomData roomData : rooms) {
            contentValues.put(KEY_NAME, roomData.getName());
            contentValues.put(KEY_UID, roomData.getUid());
            if (sqLiteDatabase.update(
                    TABLE_ROOMS,
                    contentValues,
                    KEY_UID + " = ?",
                    new String[] {roomData.getUid()}) == 0) {
                sqLiteDatabase.insert(TABLE_ROOMS, null, contentValues);
            }
            contentValues.clear();
        }
        databaseLock.unlock();
    }

    public void insertOrRenameRoom(RoomData roomData) {
        databaseLock.lock();
        contentValues.put(KEY_NAME, roomData.getName());
        contentValues.put(KEY_UID, roomData.getUid());
        if (sqLiteDatabase.update(
                TABLE_ROOMS,
                contentValues,
                KEY_UID + " = ?",
                new String[] {roomData.getUid()}) == 0) {
            sqLiteDatabase.insert(TABLE_ROOMS, null, contentValues);
        }
        contentValues.clear();
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

    public List<RoomData> getRooms() {
        databaseLock.lock();

        Cursor roomsCursor = sqLiteDatabase.query(
                TABLE_ROOMS,
                new String[] {KEY_NAME, KEY_UID},
                null, null, null, null, null);

        List<RoomData> rooms = new ArrayList<>();

        roomsCursor.moveToFirst();
        if (roomsCursor.getCount() > 0 ) {
            do {
                rooms.add(new RoomData(roomsCursor.getString(0), roomsCursor.getString(1)));
            } while (roomsCursor.moveToNext());
        }
        roomsCursor.close();
        databaseLock.unlock();
        return rooms;
    }

    public List<String> getPublicMessages() {
        databaseLock.lock();

        Cursor peopleCursor = sqLiteDatabase.query(
                TABLE_PEOPLE,
                new String[] {KEY_ID, KEY_NAME},
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
                new String[] {KEY_ID_PEOPLE, KEY_MESSAGE},
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
                new String[] {KEY_ID, KEY_NAME},
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
                new String[] {KEY_ID_PEOPLE, KEY_MESSAGE},
                KEY_ID_PEOPLE + " = ? OR " + KEY_ID_PEOPLE + " = ?",
                new String[] {ids[0].toString(), ids[1].toString()},
                null, null, null);

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

    @Override
    public synchronized void close() {
        databaseLock.lock();
        sqLiteDatabase.close();
        super.close();
        instance = null;
//        databaseLock.unlock();
    }
}
