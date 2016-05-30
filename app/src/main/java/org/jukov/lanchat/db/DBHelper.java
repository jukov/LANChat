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

    private static volatile DBHelper instance;

    @SuppressWarnings("WeakerAccess")
    public static final String TAG = DBHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 13;
    private static final String DATABASE_NAME = "LANChatDatabase";

    private final Context context;
    private final SQLiteDatabase sqLiteDatabase;
    private final ContentValues contentValues;
    private final Lock databaseLock;

    private static final String KEY_ID = "_id";
    private static final String KEY_NAME = "key_name";
    private static final String KEY_UID = "key_uid";
    private static final String KEY_MESSAGE = "key_message";
    private static final String KEY_DATE = "key_date";
    private static final String KEY_ID_PEOPLE = "id_people";
    private static final String KEY_ID_RECEIVER = "id_receiver";
    private static final String KEY_ID_ROOM = "id_room";
    private static final String TABLE_PEOPLE = "people";
    private static final String TABLE_PRIVATE_MESSAGES = "private_messages";
    private static final String TABLE_GLOBAL_MESSAGES = "public_messages";
    private static final String TABLE_ROOMS = "rooms";
    private static final String TABLE_ROOMS_MESSAGES = "rooms_messages";
    private static final String TABLE_PRIVATE_ROOM_PARTICIPANTS = "private_room_participants";

    private static final String QUERY_CREATE_PEOPLE = "CREATE TABLE " + TABLE_PEOPLE +
            "(" + KEY_ID +" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            KEY_NAME + " TEXT NOT NULL," +
            KEY_UID + " TEXT NOT NULL UNIQUE" +
            ");";
    private static final String QUERY_CREATE_PRIVATE_MESSAGES = "CREATE TABLE " + TABLE_PRIVATE_MESSAGES +
            "(" + KEY_ID +" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            KEY_ID_PEOPLE + " INTEGER NOT NULL," +
            KEY_ID_RECEIVER + " INTEGER NOT NULL," +
            KEY_MESSAGE + " TEXT NOT NULL," +
            KEY_DATE + " INTEGER NOT NULL," +
            "FOREIGN KEY(" + KEY_ID_PEOPLE + ") REFERENCES " + TABLE_PEOPLE + "(" + KEY_ID + ")," +
            "FOREIGN KEY(" + KEY_ID_RECEIVER + ") REFERENCES " + TABLE_PEOPLE + "(" + KEY_ID + ")" +
            ");";
    private static final String QUERY_CREATE_GLOBAL_MESSAGES = "CREATE TABLE " + TABLE_GLOBAL_MESSAGES +
            "(" + KEY_ID +" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            KEY_ID_PEOPLE + " INTEGER NOT NULL," +
            KEY_MESSAGE + " TEXT NOT NULL," +
            KEY_DATE + " INTEGER NOT NULL," +
            "FOREIGN KEY(" + KEY_ID_PEOPLE + ") REFERENCES " + TABLE_PEOPLE + "(" + KEY_ID + ")" +
            ");";
    private static final String QUERY_CREATE_ROOMS = "CREATE TABLE " + TABLE_ROOMS +
            "(" + KEY_ID +" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            KEY_NAME + " TEXT NOT NULL," +
            KEY_UID + " TEXT NOT NULL UNIQUE" +
            ");";
    private static final String QUERY_CREATE_PRIVATE_ROOM_PARTICIPANTS = "CREATE TABLE " + TABLE_PRIVATE_ROOM_PARTICIPANTS +
            "(" + KEY_ID +" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            KEY_ID_ROOM + " INTEGER NOT NULL," +
            KEY_ID_PEOPLE + " INTEGER NOT NULL," +
            "FOREIGN KEY(" + KEY_ID_ROOM + ") REFERENCES " + TABLE_ROOMS + "(" + KEY_ID + ")," +
            "FOREIGN KEY(" + KEY_ID_PEOPLE + ") REFERENCES " + TABLE_PEOPLE + "(" + KEY_ID + ")" +
            ");";
    private static final String QUERY_CREATE_ROOM_MESSAGES = "CREATE TABLE " + TABLE_ROOMS_MESSAGES +
            "(" + KEY_ID +" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            KEY_ID_ROOM + " INTEGER NOT NULL," +
            KEY_ID_PEOPLE + " INTEGER NOT NULL," +
            KEY_MESSAGE + " TEXT NOT NULL," +
            KEY_DATE + " INTEGER NOT NULL," +
            "FOREIGN KEY(" + KEY_ID_PEOPLE + ") REFERENCES " + TABLE_PEOPLE + "(" + KEY_ID + ")," +
            "FOREIGN KEY(" + KEY_ID_ROOM + ") REFERENCES " + TABLE_ROOMS + "(" + KEY_ID + ")" +
            ");";

    /*
    * Init methods
    */

    public static DBHelper getInstance(Context context) {
        DBHelper localInstance = instance;
        if (localInstance == null) {
            synchronized (DBHelper.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new DBHelper(context);
                }
            }
        }
        return localInstance;
    }

    private DBHelper(Context context) {
        super(context, DBHelper.DATABASE_NAME, null, DBHelper.DATABASE_VERSION);
        this.context = context;
        sqLiteDatabase = this.getReadableDatabase();
        contentValues = new ContentValues(1);
        databaseLock = new ReentrantLock();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(QUERY_CREATE_PEOPLE);
        db.execSQL(QUERY_CREATE_GLOBAL_MESSAGES);
        db.execSQL(QUERY_CREATE_PRIVATE_MESSAGES);
        db.execSQL(QUERY_CREATE_ROOMS);
        db.execSQL(QUERY_CREATE_ROOM_MESSAGES);
        db.execSQL(QUERY_CREATE_PRIVATE_ROOM_PARTICIPANTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PEOPLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRIVATE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GLOBAL_MESSAGES);
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

        int peopleId = getPeopleID(new PeopleData(chatData.getName(), chatData.getUid()));

        contentValues.put(KEY_ID_PEOPLE, peopleId);
        contentValues.put(KEY_MESSAGE, chatData.getText());
        contentValues.put(KEY_DATE, chatData.getSendDate());

        Cursor cursorReceiver;
        switch (chatData.getMessageType()) {
            case GLOBAL:
                if (sqLiteDatabase.update(
                        TABLE_GLOBAL_MESSAGES,
                        contentValues,
                        KEY_ID_PEOPLE + " = ? AND " + KEY_MESSAGE + " = ? AND " + KEY_DATE + " = ?",
                        new String[] {Integer.toString(peopleId), chatData.getText(), Long.toString(chatData.getSendDate())}
                ) == 0) {
                    sqLiteDatabase.insert(TABLE_GLOBAL_MESSAGES, null, contentValues);
                }
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
        databaseLock.unlock();
    }

    public void insertMessages(AbstractCollection<ChatData> messages) {
        for (ChatData chatData : messages) {
            insertMessage(chatData);
        }
    }

    public void insertRooms(AbstractCollection<RoomData> rooms) {
        for (RoomData roomData : rooms) {
            insertOrUpdateRoom(roomData);
        }
    }

    public void insertOrUpdateRoom(RoomData roomData) {
        boolean isParticipant = false;
        if (roomData.getParticipants() != null && roomData.getParticipants().size() > 0) {
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
                TABLE_GLOBAL_MESSAGES,
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

    /*
    * Delete methods
    */

    public void deleteMessage(ChatData chatData) {
        switch (chatData.getMessageType()) {
            case GLOBAL:
                sqLiteDatabase.delete(TABLE_GLOBAL_MESSAGES, KEY_ID_PEOPLE + " = ? AND " +
                                      KEY_MESSAGE + " = ? AND " +
                                      KEY_DATE + " = ?",
                        new String[] {Integer.toString(getPeopleID(chatData.getUid())),
                                      chatData.getText(),
                                      Long.toString(chatData.getSendDate())});
                break;
            case PRIVATE:
                sqLiteDatabase.delete(TABLE_PRIVATE_MESSAGES, KEY_ID_PEOPLE + " = ? AND " +
                                KEY_MESSAGE + " = ? AND " +
                                KEY_DATE + " = ?",
                        new String[] {Integer.toString(getPeopleID(chatData.getUid())),
                                chatData.getText(),
                                Long.toString(chatData.getSendDate())});
                break;
            case ROOM:
                sqLiteDatabase.delete(TABLE_ROOMS_MESSAGES, KEY_ID_PEOPLE + " = ? AND " +
                                KEY_MESSAGE + " = ? AND " +
                                KEY_DATE + " = ?",
                        new String[] {Integer.toString(getPeopleID(chatData.getUid())),
                                chatData.getText(),
                                Long.toString(chatData.getSendDate())});
                break;
        }
    }
}