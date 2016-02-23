package org.jukov.lanchat.service;

import android.content.Context;
import android.content.Intent;

import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.util.IntentStrings;

/**
 * Created by jukov on 22.02.2016.
 */
public class ServiceHelper {

    public static void startService(Context context) {
        Intent intent = new Intent(context, LANChatService.class);
        intent.setAction(IntentStrings.START_SERVICE_ACTION);
        context.startService(intent);
    }

    public static void sendMessage(Context context, String message) {
        Intent intent = new Intent(context, LANChatService.class);
        intent.setAction(IntentStrings.CHAT_ACTION);
        intent.putExtra(IntentStrings.EXTRA_MESSAGE, message);
        context.startService(intent);
    }

    public static void updateStatus(Context context, String status) {
        Intent intent = new Intent(IntentStrings.ACTIVITY_ACTION);
        intent.putExtra(IntentStrings.EXTRA_MODE, status);
        context.sendBroadcast(intent);
    }

    public static void receivePeople(Context context, PeopleData peopleData) {
        Intent intent = new Intent(IntentStrings.PEOPLES_ACTION);
        intent.putExtra(IntentStrings.EXTRA_NAME, peopleData.getName());
        intent.putExtra(IntentStrings.EXTRA_UID, peopleData.getUid());
        context.sendBroadcast(intent);
    }

    public static void receiveMessage(Context context, ChatData chatData) {
        Intent intent = new Intent(IntentStrings.CHAT_ACTION);
        intent.putExtra(IntentStrings.EXTRA_NAME, chatData.getName());
        intent.putExtra(IntentStrings.EXTRA_MESSAGE, chatData.getText());
        context.sendBroadcast(intent);
    }
}
