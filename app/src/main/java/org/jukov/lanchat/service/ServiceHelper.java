package org.jukov.lanchat.service;

import android.content.Context;
import android.content.Intent;

import org.jukov.lanchat.util.IntentStrings;

/**
 * Created by jukov on 22.02.2016.
 */
public class ServiceHelper {//TODO full-integration in app

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
}
