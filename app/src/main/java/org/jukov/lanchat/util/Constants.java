package org.jukov.lanchat.util;

/**
 * Created by jukov on 17.01.2016.
 */
public interface Constants {

    class IntentConstants {
        public static final String ACTIVITY_ACTION = "org.jukov.lanchat.ACTIVITY";
        public static final String GLOBAL_CHAT_ACTION = "org.jukov.lanchat.GLOBAL_CHAT";
        public static final String PRIVATE_CHAT_ACTION = "org.jukov.lanchat.PRIVATE_CHAT";
        public static final String PEOPLE_ACTION = "org.jukov.lanchat.PEOPLE";
        public static final String START_SERVICE_ACTION = "org.jukov.lanchat.CONNECT_TO_SERVICE";
        public static final String NAME_CHANGE_ACTION = "org.jukov.lanchat.CHANGE_NAME";
        public static final String SEARCH_SERVER_ACTION = "org.jukov.lanchat.SEARCH_SERVER";

        public static final String EXTRA_NAME = "name";
        public static final String EXTRA_MESSAGE = "message";
        public static final String EXTRA_UID = "uid";
        public static final String EXTRA_MODE = "mode";
        public static final String EXTRA_ACTION = "mode";
    }

    class DatabaseConstants {
        public static final String KEY_NAME = "key_name";
        public static final String KEY_MAC = "key_mac";
        public static final String KEY_MESSAGE = "key_message";
        public static final String KEY_DATE = "key_date";
        public static final String ID_PEOPLE = "id_people";
        public static final String TABLE_PEOPLE = "people";
        public static final String TABLE_PRIVATE_MESSAGES = "private_messages";
        public static final String TABLE_PUBLIC_MESSAGES = "public_messages";

        public static final String QUERY_CREATE_PEOPLE = "CREATE TABLE " + TABLE_PEOPLE +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_NAME + " TEXT, " +
                KEY_MAC + " TEXT);";

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
    }
}
