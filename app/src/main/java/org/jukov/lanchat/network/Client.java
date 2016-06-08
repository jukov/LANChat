package org.jukov.lanchat.network;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import org.jukov.lanchat.PrivateChatActivity;
import org.jukov.lanchat.R;
import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.MessagingData;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.dto.RoomData;
import org.jukov.lanchat.dto.ServiceData;
import org.jukov.lanchat.service.ServiceHelper;
import org.jukov.lanchat.util.DBHelper;
import org.jukov.lanchat.util.JSONConverter;
import org.jukov.lanchat.util.PreferenceConstants;
import org.jukov.lanchat.util.StorageHelper;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.AbstractCollection;
import java.util.List;

import static org.jukov.lanchat.dto.ServiceData.MessageType;

/**
 * Created by jukov on 06.02.2016.
 */
public class Client extends Thread implements Closeable {

    @SuppressWarnings("WeakerAccess")
    public static final String TAG = Client.class.getSimpleName();

    private final Context context;
    private final DBHelper dbHelper;

    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    private PeopleData peopleData;
    private int connections;
    private boolean privateChatState;

    public Client(Context context, String ip, int port) {
        this.context = context;
        connections = 0;
        peopleData = new PeopleData(context);
        dbHelper = DBHelper.getInstance(context);
        dbHelper.insertOrUpdatePeople(peopleData);
        while (socket == null) {
            try {
                socket = new Socket(ip, port);
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());
                peopleData.setAction(PeopleData.ActionType.CONNECT);
                sendMessage(JSONConverter.toJSON(peopleData));
                peopleData.setAction(PeopleData.ActionType.NONE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void changeName(String name) {
        peopleData.setName(name);
        peopleData.setAction(PeopleData.ActionType.CHANGE_PROFILE);
        try {
            sendMessage(JSONConverter.toJSON(peopleData));
        } catch (IOException e) {
            e.printStackTrace();
        }
        peopleData.setAction(PeopleData.ActionType.NONE);
    }

    public void changeProfilePicture(String encodedProfilePicture) {
        peopleData.setEncodedProfilePicture(encodedProfilePicture);
        peopleData.setAction(PeopleData.ActionType.CHANGE_PROFILE);
        try {
            sendMessage(JSONConverter.toJSON(peopleData));
        } catch (IOException e) {
            e.printStackTrace();
        }
        peopleData.setAction(PeopleData.ActionType.NONE);
    }

    public void setPrivateChatState(boolean privateChatState) {
        this.privateChatState = privateChatState;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        Log.d(getClass().getSimpleName(), "Client started");
        try {
            while (!socket.isClosed()) {
//                String message = dataInputStream.readUTF();
                int length = dataInputStream.readInt();
                byte[] messageArray = new byte[length];
                dataInputStream.readFully(messageArray);
                String message = new String(messageArray, "UTF-8");
//                Log.d(getClass().getSimpleName(), message);
                Object data = JSONConverter.toPOJO(message);
                if (data instanceof ChatData) {
                    ChatData chatData = (ChatData) data;

                    switch (chatData.getMessageType()) {
                        case GLOBAL:
                            dbHelper.insertMessage(chatData);
                            ServiceHelper.receiveMessage(context, chatData);
                            break;
                        case PRIVATE:

                            if (!privateChatState) {
                                PeopleData senderData = dbHelper.getPeople(chatData.getUid());

                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

                                if (sharedPreferences.getBoolean(PreferenceConstants.ENABLE_NOTIFICATIONS, false)) {
                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                                            .setSmallIcon(R.drawable.ic_account_multiple)
                                            .setContentTitle(senderData.getName())
                                            .setContentText(chatData.getText());

//                                if (sharedPreferences.getBoolean(PreferenceConstants.ENABLE_SOUND, false))
//                                     builder.setSound();
                                    builder.setSound(Uri.parse(sharedPreferences.getString(PreferenceConstants.NOTIFICATION_RINGTONE, "")));
                                    if (sharedPreferences.getBoolean(PreferenceConstants.ENABLE_VIBRATION, false))
                                        builder.setVibrate(new long[]{500, 500, 500, 500});
                                    if (sharedPreferences.getBoolean(PreferenceConstants.ENABLE_LED, false))
                                        builder.setLights(Color.MAGENTA, 3000, 3000);

                                    Intent intent = new Intent(context, PrivateChatActivity.class);
                                    intent.putExtra(ServiceHelper.IntentConstants.EXTRA_NAME, senderData.getName());
                                    intent.putExtra(ServiceHelper.IntentConstants.EXTRA_UID, senderData.getUid());
                                    intent.putExtra(ServiceHelper.IntentConstants.EXTRA_PEOPLE_AROUND, connections);

                                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                                    builder.setContentIntent(pendingIntent);

                                    Notification notification = new NotificationCompat.BigTextStyle(builder).bigText(chatData.getText()).build();

                                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                    notificationManager.notify(1, notification);
                                }
                            }

                            dbHelper.insertMessage(chatData);
                            ServiceHelper.receiveMessage(context, chatData);
                            break;
                        case ROOM:
                            if (chatData.getMessageType() == ChatData.MessageType.ROOM) {
                                List<RoomData> rooms = dbHelper.getRooms();
                                for (RoomData roomData : rooms) {
                                    if (chatData.getDestinationUID().equals(roomData.getUid())) {
                                        dbHelper.insertMessage(chatData);
                                        ServiceHelper.receiveMessage(context, chatData);
                                        break;
                                    }
                                }
                            } else {
                                dbHelper.insertMessage(chatData);
                                ServiceHelper.receiveMessage(context, chatData);
                            }
                            break;
                    }

                } else if (data instanceof PeopleData) {
                    PeopleData peopleData = (PeopleData) data;
                    dbHelper.insertOrUpdatePeople(peopleData);
                    peopleData.tryCreateBitmap();
                    if (peopleData.getProfilePicture() != null)
                        StorageHelper.storeProfilePicture(context, peopleData.getProfilePicture(), peopleData.getUid() + "_profile_picture.jpg");
                    ServiceHelper.receivePeople(context, peopleData);
                    switch (peopleData.getAction()) {
                        case CONNECT:
                            connections++;
                            break;
                        case DISCONNECT:
                            connections--;
                            break;
                    }
                    updateStatus();

                } else if (data instanceof RoomData) {
                    Log.d(TAG, "data instanceof RoomData");
                    RoomData roomData = (RoomData) data;
                    dbHelper.insertOrUpdateRoom(roomData);
                    ServiceHelper.receiveRoom(context, roomData);

                } else if (data instanceof AbstractCollection) {
                    Log.d(getClass().getSimpleName(), "receive AbstractCollection");
                    AbstractCollection dataBundle = (AbstractCollection) data;
                    MessagingData messagingData = (MessagingData) dataBundle.iterator().next();
                    if (messagingData instanceof ChatData) {
                        dbHelper.insertMessages(dataBundle);
                        ServiceHelper.receivePublicMessages(context, dataBundle);
                    } else if (messagingData instanceof RoomData) {
                        dbHelper.insertRooms(dataBundle);
                        ServiceHelper.receiveRooms(context, dataBundle);
                    }

                } else if (data instanceof ServiceData) {
                    Log.d(getClass().getSimpleName(), "Receive ServiceData");
                    ServiceData serviceData = (ServiceData) data;
                    if (serviceData.getMessageType() == MessageType.DELEGATION_SERVER_STATUS) {
                        close();
                        ServiceHelper.clearPeopleList(context);
                        ServiceHelper.startServer(context);
                        return;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
        * If action = ACTION_DISCONNECT, app closed
        * */
        if (peopleData.getAction() != PeopleData.ActionType.DISCONNECT) {
            close();
            ServiceHelper.clearPeopleList(context);
            ServiceHelper.searchServer(context);
        }
    }

    @Override
    public void close() {
        try {
            dataOutputStream.close();
            dataInputStream.close();
            socket.close();
            Log.d(getClass().getSimpleName(), "close");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendDisconnect() {
        peopleData.setAction(PeopleData.ActionType.DISCONNECT);
        try {
            sendMessage(JSONConverter.toJSON(peopleData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            Log.d(getClass().getSimpleName(), "In sendMessage()");
            byte[] data = message.getBytes("UTF-8");
            dataOutputStream.writeInt(data.length);
            dataOutputStream.write(data);
//            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateStatus() {
        ServiceHelper.updateStatus(context, connections);
    }
}
