package org.jukov.lanchat.client;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import static org.jukov.lanchat.util.Constants.DatabaseConstants;

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

    private SQLiteDatabase sqLiteDatabase;

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
        DBHelper dbHelper = new DBHelper(context, DBHelper.DATABASE_NAME, null, DBHelper.DATABASE_VERSION);
        sqLiteDatabase = dbHelper.getReadableDatabase();
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
                ContentValues contentValues = new ContentValues(1);
                if (data.getClass().getName().equals(ChatData.class.getName())) {
                    ChatData chatData = (ChatData) data;
                    Cursor cursor = sqLiteDatabase.query(
                            DatabaseConstants.TABLE_PEOPLE,
                            new String[]{"_id"},
                            DatabaseConstants.KEY_MAC + " = ?",
                            new String[]{chatData.getUid()},
                            null, null, null);
                    cursor.moveToFirst();
//                    int idIndex = cursor.getColumnIndex("_id");
                    contentValues.put(DatabaseConstants.ID_PEOPLE, cursor.getInt(0));
                    contentValues.put(DatabaseConstants.KEY_DATE, chatData.getSendDate());
                    contentValues.put(DatabaseConstants.KEY_MESSAGE, chatData.getText());
                    switch (chatData.getMessageType()) {
                        case GLOBAL:
                            sqLiteDatabase.insert(DatabaseConstants.TABLE_PUBLIC_MESSAGES, null, contentValues);
                            break;
                        case PRIVATE:
                            sqLiteDatabase.insert(DatabaseConstants.TABLE_PRIVATE_MESSAGES, null, contentValues);
                    }
                    ServiceHelper.receiveMessage(context, chatData.getMessageType(), chatData);
                } else if (data.getClass().getName().equals(PeopleData.class.getName())) {
                    PeopleData peopleData = (PeopleData) data;
                    contentValues.put(DatabaseConstants.KEY_NAME, peopleData.getName());
                    contentValues.put(DatabaseConstants.KEY_MAC, peopleData.getUid());
                    if (sqLiteDatabase.update(
                            DatabaseConstants.TABLE_PEOPLE,
                            contentValues,
                            DatabaseConstants.KEY_MAC + " = ?",
                            new String[] {peopleData.getUid()}) == 0) {
                        sqLiteDatabase.insert(DatabaseConstants.TABLE_PEOPLE, null, contentValues);
                    }
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
