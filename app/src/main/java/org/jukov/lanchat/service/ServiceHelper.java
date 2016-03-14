package org.jukov.lanchat.service;

import android.content.Context;
import android.content.Intent;

import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.util.Constants;

/**
 * Created by jukov on 22.02.2016.
 */
public class ServiceHelper {

    public enum MessageType {
        PRIVATE, GLOBAL
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, LANChatService.class);
        intent.setAction(Constants.IntentConstants.START_SERVICE_ACTION);
        context.startService(intent);
    }

    public static void sendMessage(Context context, MessageType messageType, String message) {
        Intent intent = new Intent(context, LANChatService.class);
        switch (messageType) {
            case PRIVATE:
                intent.setAction(Constants.IntentConstants.PRIVATE_CHAT_ACTION);
                break;
            case GLOBAL:
                intent.setAction(Constants.IntentConstants.GLOBAL_CHAT_ACTION);
        }
        intent.putExtra(Constants.IntentConstants.EXTRA_MESSAGE, message);
        context.startService(intent);
    }

    public static void changeName(Context context, String newName) {
        Intent intent = new Intent(context, LANChatService.class);
        intent.setAction(Constants.IntentConstants.NAME_CHANGE_ACTION);
        intent.putExtra(Constants.IntentConstants.EXTRA_NAME, newName);
        context.startService(intent);
    }

    public static void updateStatus(Context context, String status) {
        Intent intent = new Intent(Constants.IntentConstants.ACTIVITY_ACTION);
        intent.putExtra(Constants.IntentConstants.EXTRA_MODE, status);
        context.sendBroadcast(intent);
    }


    public static void receivePeople(Context context, PeopleData peopleData) {
        Intent intent = new Intent(Constants.IntentConstants.PEOPLE_ACTION);
        intent.putExtra(Constants.IntentConstants.EXTRA_NAME, peopleData.getName());
        intent.putExtra(Constants.IntentConstants.EXTRA_UID, peopleData.getUid());
        intent.putExtra(Constants.IntentConstants.EXTRA_MODE, peopleData.getAction());
        context.sendBroadcast(intent);
    }

    public static void receiveMessage(Context context, MessageType messageType, ChatData chatData) {
        Intent intent = new Intent();
        switch (messageType) {
            case PRIVATE:
                intent.setAction(Constants.IntentConstants.PRIVATE_CHAT_ACTION);
                break;
            case GLOBAL:
                intent.setAction(Constants.IntentConstants.GLOBAL_CHAT_ACTION);
        }
        intent.putExtra(Constants.IntentConstants.EXTRA_NAME, chatData.getName());
        intent.putExtra(Constants.IntentConstants.EXTRA_MESSAGE, chatData.getText());
        context.sendBroadcast(intent);
    }
}
