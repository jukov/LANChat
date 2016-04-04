package org.jukov.lanchat.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.PeopleData;

import java.util.AbstractCollection;
import java.util.Arrays;

/**
 * Created by jukov on 22.02.2016.
 */
public class ServiceHelper {

    public class IntentConstants {

        public static final String ACTIVITY_ACTION = "org.jukov.lanchat.ACTIVITY";
        public static final String GLOBAL_CHAT_ACTION = "org.jukov.lanchat.GLOBAL_CHAT";
        public static final String PRIVATE_CHAT_ACTION = "org.jukov.lanchat.PRIVATE_CHAT";
        public static final String PEOPLE_ACTION = "org.jukov.lanchat.PEOPLE";
        public static final String START_SERVICE_ACTION = "org.jukov.lanchat.CONNECT_TO_SERVICE";
        public static final String NAME_CHANGE_ACTION = "org.jukov.lanchat.CHANGE_NAME";
        public static final String SEARCH_SERVER_ACTION = "org.jukov.lanchat.SEARCH_SERVER";
        public static final String CLEAR_PEOPLE_LIST_ACTION = "org.jukov.lanchat.CLEAR_PEOPLE_LIST";

        public static final String EXTRA_NAME = "name";
        public static final String EXTRA_MESSAGE = "message";
        public static final String EXTRA_MESSAGE_BUNDLE = "message_bundle";
        public static final String EXTRA_UID = "uid";
        public static final String EXTRA_MODE = "mode";
        public static final String EXTRA_ACTION = "action";
        public static final String EXTRA_RECEIVER_UID = "receiver_uid";
    }

    public enum MessageType {
        PRIVATE(0), GLOBAL(1);

        private int value;
        MessageType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, LANChatService.class);
        intent.setAction(IntentConstants.START_SERVICE_ACTION);
        context.startService(intent);
    }

    public static void searchServer(Context context) {
        Intent intent = new Intent(context, LANChatService.class);
        intent.setAction(IntentConstants.SEARCH_SERVER_ACTION);
        context.startService(intent);
    }

    public static void sendMessage(Context context, MessageType messageType, String message) {
        sendMessage(context, messageType, message, null);
    }

    public static void sendMessage(Context context, MessageType messageType, String message, String receiverUID) {
        Intent intent = new Intent(context, LANChatService.class);
        switch (messageType) {
            case PRIVATE:
                intent.setAction(IntentConstants.PRIVATE_CHAT_ACTION);
                intent.putExtra(IntentConstants.EXTRA_RECEIVER_UID, receiverUID);
                break;
            case GLOBAL:
                intent.setAction(IntentConstants.GLOBAL_CHAT_ACTION);
        }
        intent.putExtra(IntentConstants.EXTRA_MESSAGE, message);
        context.startService(intent);
    }

    public static void changeName(Context context, String newName) {
        Intent intent = new Intent(context, LANChatService.class);
        intent.setAction(IntentConstants.NAME_CHANGE_ACTION);
        intent.putExtra(IntentConstants.EXTRA_NAME, newName);
        context.startService(intent);
    }

    public static void updateStatus(Context context, String status) {
        Intent intent = new Intent(IntentConstants.ACTIVITY_ACTION);
        intent.putExtra(IntentConstants.EXTRA_MODE, status);
        context.sendBroadcast(intent);
    }

    public static void receivePeople(Context context, PeopleData peopleData) {
        Intent intent = new Intent(IntentConstants.PEOPLE_ACTION);
        intent.putExtra(IntentConstants.EXTRA_NAME, peopleData.getName());
        intent.putExtra(IntentConstants.EXTRA_UID, peopleData.getUid());
        intent.putExtra(IntentConstants.EXTRA_ACTION, peopleData.getAction());
        context.sendBroadcast(intent);
    }

    public static void receiveMessage(Context context, MessageType messageType, ChatData chatData) {
        Intent intent = new Intent();
        switch (messageType) {
            case PRIVATE:
                intent.setAction(IntentConstants.PRIVATE_CHAT_ACTION);
                intent.putExtra(IntentConstants.EXTRA_RECEIVER_UID, chatData.getReceiverUID());
                break;
            case GLOBAL:
                intent.setAction(IntentConstants.GLOBAL_CHAT_ACTION);
        }
        intent.putExtra(IntentConstants.EXTRA_NAME, chatData.getName());
        intent.putExtra(IntentConstants.EXTRA_MESSAGE, chatData.getText());
        context.sendBroadcast(intent);
    }

    public static void receivePublicMessages(Context context, AbstractCollection messagesBundle) {
        Object[] objectArray = messagesBundle.toArray();
        ChatData[] messagesArray = Arrays.copyOf(objectArray, objectArray.length, ChatData[].class);
        Intent intent = new Intent(IntentConstants.GLOBAL_CHAT_ACTION);
        intent.putExtra(IntentConstants.EXTRA_MESSAGE_BUNDLE, messagesArray);
        context.sendBroadcast(intent);
    }

    public static void clearPeopleList(Context context) {
        Intent intent = new Intent(IntentConstants.CLEAR_PEOPLE_LIST_ACTION);
        context.sendBroadcast(intent);
    }
}
