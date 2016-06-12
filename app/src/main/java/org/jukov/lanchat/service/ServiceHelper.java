package org.jukov.lanchat.service;

import android.content.Context;
import android.content.Intent;

import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.dto.RoomData;

import java.util.AbstractCollection;
import java.util.Arrays;

/**
 * Created by jukov on 22.02.2016.
 */
public class ServiceHelper {

    private static final String TAG = ServiceHelper.class.getSimpleName();

    public class IntentConstants {

        public static final String INIT_SERVICE_ACTION =        "org.jukov.lanchat.CONNECT_TO_SERVICE";
        public static final String SEARCH_SERVER_ACTION =       "org.jukov.lanchat.SEARCH_SERVER";
        public static final String START_SERVER_ACTION =        "org.jukov.lanchat.START_SERVER";
        public static final String PEOPLE_AROUND_ACTION =       "org.jukov.lanchat.PEOPLE_AROUND";

        public static final String PROFILE_NAME_CHANGE_ACTION =         "org.jukov.lanchat.CHANGE_NAME";
        public static final String PROFILE_PICTURE_CHANGE_ACTION ="org.jukov.lanchat.CHANGE_PROFILE_PICTURE";
        public static final String GLOBAL_MESSAGE_ACTION =      "org.jukov.lanchat.GLOBAL_MESSAGE";

        public static final String PRIVATE_MESSAGE_ACTION =     "org.jukov.lanchat.PRIVATE_MESSAGE";
        public static final String ROOM_MESSAGE_ACTION =        "org.jukov.lanchat.ROOM_MESSAG";
        public static final String MESSAGE_ACTION =             "org.jukov.lanchat.MESSAGE";
        public static final String PEOPLE_ACTION =              "org.jukov.lanchat.PEOPLE";

        public static final String SET_ALL_OFFLINE_ACTION =   "org.jukov.lanchat.CLEAR_PEOPLE_LIST";
        public static final String SEND_ROOM_ACTION =           "org.jukov.lanchat.SEND_ROOM";
        public static final String PRIVATE_CHAT_STATE_ACTION = "org.jukov.lanchat.PRIVATE_CHAT_STATE";

        public static final String EXTRA_NAME =             "name";
        public static final String EXTRA_MESSAGE =          "message";
        public static final String EXTRA_MESSAGE_BUNDLE =   "message_bundle";
        public static final String EXTRA_UID =              "uid";
        public static final String EXTRA_ID =               "id";
        public static final String EXTRA_PEOPLE_AROUND =    "mode";
        public static final String EXTRA_ROOM =             "room";
        public static final String EXTRA_PARTICIPANTS =     "participants";
        public static final String EXTRA_STATE =            "state";
        public static final String EXTRA_PEOPLE =           "people";
        public static final String EXTRA_PROFILE_PICTURE =  "profile_picture";

    }
    /*
    * Methods for messages from activity to service
    */

    public static void startService(Context context) {
        Intent intent = new Intent(context, LANChatService.class);
        intent.setAction(IntentConstants.INIT_SERVICE_ACTION);
        context.startService(intent);
    }

    public static void searchServer(Context context) {
        Intent intent = new Intent(context, LANChatService.class);
        intent.setAction(IntentConstants.SEARCH_SERVER_ACTION);
        context.startService(intent);
    }

    public static void startServer(Context context) {
        Intent intent = new Intent(context, LANChatService.class);
        intent.setAction(IntentConstants.START_SERVER_ACTION);
        context.startService(intent);
    }

    public static void sendMessage(Context context, ChatData chatData) {
        Intent intent = new Intent(context, LANChatService.class);
        intent.setAction(IntentConstants.MESSAGE_ACTION);
        intent.putExtra(IntentConstants.EXTRA_MESSAGE, chatData);
        context.startService(intent);
    }

    public static void changeName(Context context, String newName) {
        Intent intent = new Intent(context, LANChatService.class);
        intent.setAction(IntentConstants.PROFILE_NAME_CHANGE_ACTION);
        intent.putExtra(IntentConstants.EXTRA_NAME, newName);
        context.startService(intent);
    }

    public static void changeProfilePicture(Context context, String encodedNewProfilePicture) {
        Intent intent = new Intent(context, LANChatService.class);
        intent.setAction(IntentConstants.PROFILE_PICTURE_CHANGE_ACTION);
        intent.putExtra(IntentConstants.EXTRA_PROFILE_PICTURE, encodedNewProfilePicture);
        context.startService(intent);
    }

    public static void sendRoom(Context context, RoomData roomData) {
        Intent intent = new Intent(context, LANChatService.class);
        intent.setAction(IntentConstants.SEND_ROOM_ACTION);
        intent.putExtra(IntentConstants.EXTRA_ROOM, roomData);
//        String className = context.startService(intent).getClassName();
        context.startService(intent);
//        Log.d(TAG, className);
    }

    public static void sendPrivateChatState(Context context, boolean state) {
        Intent intent = new Intent(context, LANChatService.class);
        intent.setAction(IntentConstants.PRIVATE_CHAT_STATE_ACTION);
        intent.putExtra(IntentConstants.EXTRA_STATE, state);
        context.startService(intent);
    }

    /*
    * Methods for messages from service to activity
    */

    public static void updateStatus(Context context, int connections) {
        Intent intent = new Intent(IntentConstants.PEOPLE_AROUND_ACTION);
        intent.putExtra(IntentConstants.EXTRA_PEOPLE_AROUND, connections);
        context.sendBroadcast(intent);
    }

    public static void receiveRoom(Context context, RoomData roomData) {
        Intent intent = new Intent(IntentConstants.SEND_ROOM_ACTION);
        intent.putExtra(IntentConstants.EXTRA_ROOM, roomData);
        context.sendBroadcast(intent);
    }

    public static void receivePeople(Context context, PeopleData peopleData) {
        Intent intent = new Intent(IntentConstants.PEOPLE_ACTION);
        intent.putExtra(IntentConstants.EXTRA_PEOPLE, peopleData);
        context.sendBroadcast(intent);
    }

    public static void receiveMessage(Context context, ChatData chatData) {
        Intent intent = new Intent();
        switch (chatData.getMessageType()) {
            case PRIVATE:
                intent.setAction(IntentConstants.PRIVATE_MESSAGE_ACTION);
                break;
            case GLOBAL:
                intent.setAction(IntentConstants.GLOBAL_MESSAGE_ACTION);
                break;
            case ROOM:
                intent.setAction(IntentConstants.ROOM_MESSAGE_ACTION);
        }
        intent.putExtra(IntentConstants.EXTRA_MESSAGE, chatData);
        context.sendBroadcast(intent);
    }

    public static void receiveRooms(Context context, AbstractCollection roomsBundle) {
        Object[] objectArray = roomsBundle.toArray();
        RoomData[] messagesArray = Arrays.copyOf(objectArray, objectArray.length, RoomData[].class);
        Intent intent = new Intent(IntentConstants.SEND_ROOM_ACTION);
        intent.putExtra(IntentConstants.EXTRA_MESSAGE_BUNDLE, messagesArray);
        context.sendBroadcast(intent);
    }

    public static void receivePublicMessages(Context context, AbstractCollection messagesBundle) {
        Object[] objectArray = messagesBundle.toArray();
        ChatData[] messagesArray = Arrays.copyOf(objectArray, objectArray.length, ChatData[].class);
        Intent intent = new Intent(IntentConstants.GLOBAL_MESSAGE_ACTION);
        intent.putExtra(IntentConstants.EXTRA_MESSAGE_BUNDLE, messagesArray);
        context.sendBroadcast(intent);
    }

    public static void clearPeopleList(Context context) {
        Intent intent = new Intent(IntentConstants.SET_ALL_OFFLINE_ACTION);
        context.sendBroadcast(intent);
    }
}
