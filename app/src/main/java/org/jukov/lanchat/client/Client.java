package org.jukov.lanchat.client;

import android.content.Context;
import android.os.NetworkOnMainThreadException;
import android.util.Log;

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
                Log.d(getClass().getSimpleName(), "Receive message " + data.getClass().getName());
                if (data.getClass().getName().equals(ChatData.class.getName())) {
                    ChatData chatData = (ChatData) data;
                    ServiceHelper.receiveMessage(context, chatData.getMessageType(), chatData);
                } else if (data.getClass().getName().equals(PeopleData.class.getName())) {
                    PeopleData peopleData = (PeopleData) data;
                    ServiceHelper.receivePeople(context, peopleData);
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
            peopleData.setAction(PeopleData.ACTION_DISCONNECT);
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
