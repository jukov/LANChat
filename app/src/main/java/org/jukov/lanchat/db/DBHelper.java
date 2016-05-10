package org.jukov.lanchat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.dto.RoomData;
import org.jukov.lanchat.util.Utils;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by jukov on 13.03.2016.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String TAG = DBHelper.class.getSimpleName();

    private static volatile DBHelper instance;

    private Context context;

    public static final int DATABASE_VERSION = 13;
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
    public static final String TABLE_PRIVATE_ROOM_PARTICIPANTS = "private_room_participants";



    public static final String QUERY_CREATE_PEOPLE = "CREATE TABLE " + TABLE_PEOPLE +
            "(" + KEY_ID +" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            KEY_NAME + " TEXT NOT NULL," +
            KEY_UID + " TEXT NOT NULL UNIQUE" +
            ");";

    public static final String QUERY_CREATE_PRIVATE_MESSAGES = "CREATE TABLE " + TABLE_PRIVATE_MESSAGES +
            "(" + KEY_ID +" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            KEY_ID_PEOPLE + " INTEGER NOT NULL," +
            KEY_ID_RECEIVER + " INTEGER NOT NULL," +
            KEY_MESSAGE + " TEXT NOT NULL," +
            KEY_DATE + " INTEGER NOT NULL," +
            "FOREIGN KEY(" + KEY_ID_PEOPLE + ") REFERENCES " + TABLE_PEOPLE + "(" + KEY_ID + ")," +
            "FOREIGN KEY(" + KEY_ID_RECEIVER + ") REFERENCES " + TABLE_PEOPLE + "(" + KEY_ID + ")" +
            ");";

    public static final String QUERY_CREATE_PUBLIC_MESSAGES = "CREATE TABLE " + TABLE_PUBLIC_MESSAGES +
            "(" + KEY_ID +" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            KEY_ID_PEOPLE + " INTEGER NOT NULL," +
            KEY_MESSAGE + " TEXT NOT NULL," +
            KEY_DATE + " INTEGER NOT NULL," +
            "FOREIGN KEY(" + KEY_ID_PEOPLE + ") REFERENCES " + TABLE_PEOPLE + "(" + KEY_ID + ")" +
            ");";

    public static final String QUERY_CREATE_ROOMS = "CREATE TABLE " + TABLE_ROOMS +
            "(" + KEY_ID +" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            KEY_NAME + " TEXT NOT NULL," +
            KEY_UID + " TEXT NOT NULL UNIQUE" +
            ");";

    public static final String QUERY_CREATE_PRIVATE_ROOM_PARTCIPANTS = "CREATE TABLE " + TABLE_PRIVATE_ROOM_PARTICIPANTS +
            "(" + KEY_ID +" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            KEY_ID_ROOM + " INTEGER NOT NULL," +
            KEY_ID_PEOPLE + " INTEGER NOT NULL," +
            "FOREIGN KEY(" + KEY_ID_ROOM + ") REFERENCES " + TABLE_ROOMS + "(" + KEY_ID + ")," +
            "FOREIGN KEY(" + KEY_ID_PEOPLE + ") REFERENCES " + TABLE_PEOPLE + "(" + KEY_ID + ")" +
            ");";

    public static final String QUERY_CREATE_ROOMS_MESSAGES = "CREATE TABLE " + TABLE_ROOMS_MESSAGES +
            "(" + KEY_ID +" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            KEY_ID_ROOM + " INTEGER NOT NULL," +
            KEY_ID_PEOPLE + " INTEGER NOT NULL," +
            KEY_MESSAGE + " TEXT NOT NULL," +
            KEY_DATE + " INTEGER NOT NULL," +
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

    /*
    * Init methods
    */

    private DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
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
        db.execSQL(QUERY_CREATE_PRIVATE_ROOM_PARTCIPANTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PEOPLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRIVATE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PUBLIC_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOMS_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRIVATE_ROOM_PARTICIPANTS);
        onCreate(db);
    }

    @Override
    public synchronized void close() {
        databaseLock.lock();
        sqLiteDatabase.close();
        super.close();
        instance = null;
//        databaseLock.unlock();
    }

    /*
    * Get-id methods
    */

    private int getRoomID(String uid) {
        Cursor roomIdCursor = sqLiteDatabase.query(
                TABLE_ROOMS,
                new String[] {KEY_ID},
                KEY_UID + " = ?",
                new String[] {uid},
                null, null, null);

        if (roomIdCursor.getCount() > 0) {
            roomIdCursor.moveToFirst();
            int id = roomIdCursor.getInt(0);
            roomIdCursor.close();
            return id;
        }
        roomIdCursor.close();
        throw new NoSuchElementException("Room not found " + uid);
    }


    private String getPeopleUID(int id) {
        Cursor peopleIdCursor = sqLiteDatabase.query(
                TABLE_PEOPLE,
                new String[] {KEY_UID},
                KEY_ID + " = ?",
                new String[] {Integer.toString(id)},
                null, null, null);

        if (peopleIdCursor.getCount() > 0) {
            peopleIdCursor.moveToFirst();
            String uid = peopleIdCursor.getString(0);
            peopleIdCursor.close();
            return uid;
        }
        peopleIdCursor.close();
        throw new NoSuchElementException("People not found " + id);
    }

    private int getPeopleID(String uid) {
        Cursor peopleIdCursor = sqLiteDatabase.query(
                TABLE_PEOPLE,
                new String[] {KEY_ID},
                KEY_UID + " = ?",
                new String[] {uid},
                null, null, null);

        if (peopleIdCursor.getCount() > 0) {
            peopleIdCursor.moveToFirst();
            int id = peopleIdCursor.getInt(0);
            peopleIdCursor.close();
            return id;
        }
        peopleIdCursor.close();
        throw new NoSuchElementException("People not found " + uid);
    }

    private int getPeopleID(PeopleData peopleData) {
        Log.d(TAG, "getPeopleID()");
        Cursor peopleIdCursor = sqLiteDatabase.query(
                TABLE_PEOPLE,
                new String[] {KEY_ID},
                KEY_UID + " = ?",
                new String[] {peopleData.getUid()},
                null, null, null);

        if (peopleIdCursor.getCount() > 0) {
            peopleIdCursor.moveToFirst();
            int id = peopleIdCursor.getInt(0);
            peopleIdCursor.close();
            return id;
        } else {
            databaseLock.unlock();
            insertOrUpdatePeople(peopleData);
            databaseLock.lock();
            return getPeopleID(peopleData);
        }
    }

    private PeopleData getPeople(int id) {
        Cursor peopleCursor = sqLiteDatabase.query(
                TABLE_PEOPLE,
                new String[] {KEY_NAME, KEY_UID},
                KEY_ID + " = ?",
                new String[] {Integer.toString(id)},
                null, null, null);

        if (peopleCursor.getCount() > 0 ) {
            peopleCursor.moveToFirst();
            PeopleData peopleData = new PeopleData(peopleCursor.getString(0), peopleCursor.getString(1));
            peopleCursor.close();
            return peopleData;
        }
        throw new NoSuchElementException("People not found " + id);
    }

    /*
    * Insert methods
    */

    public void insertMessage(ChatData chatData) {
        databaseLock.lock();
        Cursor cursorSender = sqLiteDatabase.query(
                TABLE_PEOPLE,
                new String[]{KEY_ID},
                KEY_UID + " = ?",
                new String[]{chatData.getUid()},
                null, null, null);

        if (cursorSender.getCount() > 0) {
            cursorSender.moveToFirst();
            contentValues.put(KEY_ID_PEOPLE, cursorSender.getInt(0));
        } else {
            databaseLock.unlock();
            insertOrUpdatePeople(new PeopleData(chatData.getName(), chatData.getUid()));
            databaseLock.lock();
            contentValues.put(KEY_ID_PEOPLE, getPeopleID(chatData.getUid()));
        }
            contentValues.put(KEY_DATE, chatData.getSendDate());
            contentValues.put(KEY_MESSAGE, chatData.getText());
        Cursor cursorReceiver;
        switch (chatData.getMessageType()) {
            case GLOBAL:
                sqLiteDatabase.insert(TABLE_PUBLIC_MESSAGES, null, contentValues);
                break;
            case PRIVATE:
                cursorReceiver = sqLiteDatabase.query(
                        TABLE_PEOPLE,
                        new String[]{KEY_ID},
                        KEY_UID + " = ?",
                        new String[]{chatData.getDestinationUID()},
                        null, null, null);
                cursorReceiver.moveToFirst();
                contentValues.put(KEY_ID_RECEIVER, cursorReceiver.getInt(0));
                sqLiteDatabase.insert(TABLE_PRIVATE_MESSAGES, null, contentValues);
                cursorReceiver.close();
                break;
            case ROOM:
                cursorReceiver = sqLiteDatabase.query(
                        TABLE_ROOMS,
                        new String[]{KEY_ID},
                        KEY_UID + " = ?",
                        new String[]{chatData.getDestinationUID()},
                        null, null, null);
                cursorReceiver.moveToFirst();
                contentValues.put(KEY_ID_ROOM, cursorReceiver.getInt(0));
                sqLiteDatabase.insert(TABLE_ROOMS_MESSAGES, null, contentValues);
                cursorReceiver.close();
                break;
        }
        contentValues.clear();
        cursorSender.close();
        databaseLock.unlock();
    }

    public void insertMessages(AbstractCollection<ChatData> messages) {
        for (ChatData chatData : messages) {
            insertMessage(chatData);
        }
//        databaseLock.lock();
//        Cursor cursor = sqLiteDatabase.query(
//                TABLE_PEOPLE,
//                new String[]{KEY_ID, KEY_UID},
//                null, null, null, null, null);
//
//        HashMap<String, Integer> peopleIDs = new HashMap<>();
//        if (cursor.getCount() > 0) {
//            cursor.moveToFirst();
//            do {
//                peopleIDs.put(cursor.getString(1), cursor.getInt(0));
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//
//        for (ChatData chatData : messages) {
//            contentValues.put(KEY_ID_PEOPLE, peopleIDs.get(chatData.getRoomUid()));
//            contentValues.put(KEY_MESSAGE, chatData.getText());
//            contentValues.put(KEY_DATE, chatData.getSendDate());
//            sqLiteDatabase.insert(TABLE_PUBLIC_MESSAGES, null, contentValues);
//            contentValues.clear();
//        }
//        databaseLock.unlock();
    }

    public void insertRooms(AbstractCollection<RoomData> rooms) {
        for (RoomData roomData : rooms) {
            insertOrUpdateRoom(roomData);
        }
//        databaseLock.lock();
//
//        for (RoomData roomData : rooms) {
//            contentValues.put(KEY_NAME, roomData.getName());
//            contentValues.put(KEY_UID, roomData.getRoomUid());
//            if (sqLiteDatabase.update(
//                    TABLE_ROOMS,
//                    contentValues,
//                    KEY_UID + " = ?",
//                    new String[] {roomData.getRoomUid()}) == 0) {
//                sqLiteDatabase.insert(TABLE_ROOMS, null, contentValues);
//            }
//            contentValues.clear();
//        }
//        databaseLock.unlock();
    }

    public void insertOrUpdateRoom(RoomData roomData) {
        boolean isParticipant = false;
        if (roomData.getParticipants() != null && roomData.getParticipants().size() > 0) {
            Log.d(TAG, Integer.toString(roomData.getParticipants().size()));
            for (PeopleData peopleData1 : roomData.getParticipants()) {
                if (peopleData1.getUid().contains(Utils.getAndroidID(context))) {
                    isParticipant = true;
                    break;
                }
            }
        } else {
            isParticipant = true;
        }
        if (!isParticipant)
            return;
        databaseLock.lock();
        contentValues.put(KEY_NAME, roomData.getName());
        contentValues.put(KEY_UID, roomData.getUid());
        if (sqLiteDatabase.update(
                TABLE_ROOMS,
                contentValues,
                KEY_UID + " = ?",
                new String[] {roomData.getUid()}
        ) == 0) {
            sqLiteDatabase.insert(TABLE_ROOMS, null, contentValues);
        }
        contentValues.clear();

        int id = getRoomID(roomData.getUid());

        if (roomData.getParticipants() != null) {
            for (PeopleData peopleData : roomData.getParticipants()) {
                contentValues.put(KEY_ID_PEOPLE, getPeopleID(peopleData));
                contentValues.put(KEY_ID_ROOM, id);
                if (sqLiteDatabase.update(
                        TABLE_PRIVATE_ROOM_PARTICIPANTS,
                        contentValues,
                        KEY_ID_ROOM + " = ? AND " + KEY_ID_PEOPLE + " = ?",
                        new String[] {
                                Integer.toString(getRoomID(roomData.getUid())),
                                Integer.toString(getPeopleID(peopleData))}
                ) == 0) {
                    sqLiteDatabase.insert(TABLE_PRIVATE_ROOM_PARTICIPANTS, null, contentValues);
                }
                contentValues.clear();
            }
        }
        contentValues.clear();
        databaseLock.unlock();
    }

    public void insertOrUpdatePeople(PeopleData peopleData) {
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

    /*
    * Get-data methods
    */

    public List<RoomData> getRooms() {
        databaseLock.lock();

        Cursor roomsCursor = sqLiteDatabase.query(
                TABLE_ROOMS,
                new String[] {KEY_ID, KEY_NAME, KEY_UID},
                null, null, null, null, null);

        List<RoomData> rooms = new ArrayList<>();
        Log.d(TAG, Integer.toString(roomsCursor.getCount()));
        if (roomsCursor.getCount() > 0 ) {
            roomsCursor.moveToFirst();
            do {
                Cursor participantsCursor = sqLiteDatabase.query(
                        TABLE_PRIVATE_ROOM_PARTICIPANTS,
                        new String[] {KEY_ID_PEOPLE},
                        KEY_ID_ROOM + " = ?",
                        new String[] {Integer.toString(roomsCursor.getInt(0))},
                        null, null, null);

                if (participantsCursor.getCount() > 0) {
                    participantsCursor.moveToFirst();
                    List<PeopleData> participants = new ArrayList<>();
                    do {
                        databaseLock.unlock();
                        participants.add(getPeople(participantsCursor.getInt(0)));
                        databaseLock.lock();
                    } while (participantsCursor.moveToNext());
                    rooms.add(new RoomData(roomsCursor.getString(1), roomsCursor.getString(2), participants));
                } else {
                    rooms.add(new RoomData(roomsCursor.getString(1), roomsCursor.getString(2)));
                }
                participantsCursor.close();
            } while (roomsCursor.moveToNext());
        }
        roomsCursor.close();
        databaseLock.unlock();
        return rooms;
    }

    public List<PeopleData> getPeople() {
        databaseLock.lock();

        Cursor peopleCursor = sqLiteDatabase.query(
                TABLE_PEOPLE,
                new String[] {KEY_NAME, KEY_UID},
                null, null, null, null, null);

        List<PeopleData> people = new ArrayList<>();

        if (peopleCursor.getCount() > 0 ) {
            peopleCursor.moveToFirst();
            do {
                people.add(new PeopleData(peopleCursor.getString(0), peopleCursor.getString(1)));
            } while (peopleCursor.moveToNext());
        }
        peopleCursor.close();

        databaseLock.unlock();
        return people;
    }

    public List<ChatData> getPublicMessages() {
        databaseLock.lock();

        Cursor messagesCursor = sqLiteDatabase.query(
                TABLE_PUBLIC_MESSAGES,
                new String[] {KEY_ID_PEOPLE, KEY_MESSAGE, KEY_DATE},
                null, null, null, null, null);

        List<ChatData> messagesList = new ArrayList<>();
        if (messagesCursor.getCount() > 0 ) {
            messagesCursor.moveToFirst();
            do {
                PeopleData peopleData = getPeople(messagesCursor.getInt(0));
                messagesList.add(new ChatData(
                        peopleData.getName(),
                        peopleData.getUid(),
                        ChatData.MessageType.GLOBAL,
                        messagesCursor.getString(1),
                        messagesCursor.getLong(2)));
            } while (messagesCursor.moveToNext());
        }
        messagesCursor.close();

        databaseLock.unlock();
        return messagesList;
    }

    public List<ChatData> getPrivateMessages(String myUID, String companionUID) {
        databaseLock.lock();

        int myId = getPeopleID(myUID);
        int companionId = getPeopleID(companionUID);

        Cursor messagesCursor = sqLiteDatabase.query(
                TABLE_PRIVATE_MESSAGES,
                new String[] {KEY_ID_PEOPLE, KEY_ID_RECEIVER, KEY_MESSAGE, KEY_DATE},
                "(" + KEY_ID_PEOPLE + " = ? AND " + KEY_ID_RECEIVER + " = ?) OR " +
                        "(" + KEY_ID_PEOPLE + " = ? AND " + KEY_ID_RECEIVER + " = ?)",
                new String[] {Integer.toString(myId), Integer.toString(companionId),
                        Integer.toString(companionId), Integer.toString(myId)},
                null, null, null);

        List<ChatData> messages = new ArrayList<>();
        if (messagesCursor.getCount() > 0 ) {
            messagesCursor.moveToFirst();
            do {
                PeopleData peopleData1 = getPeople(messagesCursor.getInt(0));
                PeopleData peopleData2 = getPeople(messagesCursor.getInt(1));
                messages.add(new ChatData(
                        peopleData1.getName(),
                        peopleData1.getUid(),
                        ChatData.MessageType.PRIVATE,
                        messagesCursor.getString(2),
                        messagesCursor.getLong(3),
                        peopleData2.getUid()));
            } while (messagesCursor.moveToNext());
        }
        messagesCursor.close();

        databaseLock.unlock();
        return messages;
    }

    public List<ChatData> getRoomMessages(String roomUID) {
        databaseLock.lock();

        int roomId = getRoomID(roomUID);

        Cursor messagesCursor = sqLiteDatabase.query(
                TABLE_ROOMS_MESSAGES,
                new String[] {KEY_ID_ROOM, KEY_ID_PEOPLE, KEY_MESSAGE, KEY_DATE},
                KEY_ID_ROOM + " = ?",
                new String[] {Integer.toString(roomId)},
                null, null, null);

        List<ChatData> messagesList = new ArrayList<>();
        if (messagesCursor.getCount() > 0 ) {
            messagesCursor.moveToFirst();
            do {
                PeopleData peopleData = getPeople(messagesCursor.getInt(1));
                messagesList.add(new ChatData(
                        peopleData.getName(),
                        peopleData.getUid(),
                        ChatData.MessageType.ROOM,
                        messagesCursor.getString(2),
                        messagesCursor.getLong(3),
                        roomUID));
            } while (messagesCursor.moveToNext());
        }
        messagesCursor.close();

        databaseLock.unlock();
        return messagesList;
    }
}