package org.jukov.lanchat.client;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.Data;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.json.JSONConverter;
import org.jukov.lanchat.util.IntentStrings;
import org.jukov.lanchat.util.NetworkUtils;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by jukov on 06.02.2016.
 */
public class Client extends Thread implements Closeable {

    public static final String TAG = "LC_Client";

    private Context context;
    private int port;
    private String ip;
    private PeopleData peopleData;

    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    public Client(Context context, String ip, int port) {
        this.context = context;
        this.port = port;
        this.ip = ip;
        peopleData = new PeopleData(context, NetworkUtils.getMACAddress(context));
        try {
            socket = new Socket(ip, port);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
            sendMessage(JSONConverter.toJSON(peopleData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeName(String name) {
        peopleData.setName(name);
        try {
            sendMessage(JSONConverter.toJSON(peopleData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Log.d(TAG, "Client started");
        try {
            while (!socket.isClosed()) {
                String message = dataInputStream.readUTF();
                Data data = JSONConverter.toJavaObject(message);
                Log.d(TAG, "Receive message " + data.getClass().getName());
                if (data.getClass().getName().equals(ChatData.class.getName())) {
                    ChatData chatData = (ChatData) data;
                    Intent intent = new Intent(IntentStrings.CHAT_ACTION);
                    intent.putExtra(IntentStrings.EXTRA_NAME, chatData.getName());
                    intent.putExtra(IntentStrings.EXTRA_MESSAGE, chatData.getText());
                    context.sendBroadcast(intent);
                } else if (data.getClass().getName().equals(PeopleData.class.getName())) {
                    PeopleData peopleData = (PeopleData) data;
                    Intent intent = new Intent(IntentStrings.PEOPLES_ACTION);
                    intent.putExtra(IntentStrings.EXTRA_NAME, peopleData.getName());
                    intent.putExtra(IntentStrings.EXTRA_UID, peopleData.getUid());
                    context.sendBroadcast(intent);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            sendMessage(JSONConverter.toJSON(peopleData));
            dataOutputStream.close();
            dataInputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            Log.d(TAG, "In sendMessage()");
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLocalIP() {
        return socket.getLocalAddress().toString();
    }

    public void updateStatus() {
        Intent intent = new Intent(IntentStrings.ACTIVITY_ACTION);
        intent.putExtra(IntentStrings.EXTRA_MODE, "Mode: client");
        context.sendBroadcast(intent);
    }


}
