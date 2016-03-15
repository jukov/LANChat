package org.jukov.lanchat.client;

import android.content.Context;
import android.util.Log;

import org.jukov.lanchat.db.DBHelper;
import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.Data;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.json.JSONConverter;
import org.jukov.lanchat.service.ServiceHelper;
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

    private Context context;
    private int port;
    private String ip;
    private PeopleData peopleData;

    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    private DBHelper dbHelper;

    public Client(Context context, String ip, int port) {
        this.context = context;
        this.port = port;
        this.ip = ip;
        peopleData = new PeopleData(context, NetworkUtils.getMACAddress(context), PeopleData.ACTION_NONE);
        while (socket == null) {
            try {
                socket = new Socket(ip, port);
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());
                peopleData.setAction(PeopleData.ACTION_CONNECT);
                sendMessage(JSONConverter.toJSON(peopleData));
                peopleData.setAction(PeopleData.ACTION_NONE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dbHelper = DBHelper.getInstance(context);
    }

    public void changeName(String name) {
        peopleData.setName(name);
        peopleData.setAction(PeopleData.ACTION_CHANGE_NAME);
        try {
            sendMessage(JSONConverter.toJSON(peopleData));
        } catch (IOException e) {
            e.printStackTrace();
        }
        peopleData.setAction(PeopleData.ACTION_NONE);
    }

    @Override
    public void run() {
        Log.d(getClass().getSimpleName(), "Client started");
        try {
            while (!socket.isClosed()) {
                String message = dataInputStream.readUTF();
                Data data = JSONConverter.toJavaObject(message);
                if (data instanceof ChatData) {
                    ChatData chatData = (ChatData) data;
                    dbHelper.insertMessage(chatData);
                    ServiceHelper.receiveMessage(context, chatData.getMessageType(), chatData);
                } else if (data instanceof PeopleData) {
                    PeopleData peopleData = (PeopleData) data;
                    dbHelper.insertOrRenamePeople(peopleData);
                    ServiceHelper.receivePeople(context, peopleData);
                }
            }
        }
        catch (IOException e) {
            if (!e.getMessage().equals("Socket closed"))
                e.printStackTrace();
        }
        if (peopleData.getAction() != PeopleData.ACTION_DISCONNECT) {
            ServiceHelper.searchServer(context);
        }
    }

    @Override
    public void close() {
        try {
            peopleData.setAction(PeopleData.ACTION_DISCONNECT);
            sendMessage(JSONConverter.toJSON(peopleData));
            dataOutputStream.close();
            dataInputStream.close();
            socket.close();
            dbHelper.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            Log.d(getClass().getSimpleName(), "In sendMessage()");
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
        ServiceHelper.updateStatus(context, "Mode: client");
    }


}
