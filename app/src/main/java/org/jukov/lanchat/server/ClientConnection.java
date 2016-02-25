package org.jukov.lanchat.server;

import android.util.Log;

import org.jukov.lanchat.dto.Data;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.json.JSONConverter;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by jukov on 07.02.2016.
 */
public class ClientConnection extends Thread implements Closeable {

    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    private Server server;

    private PeopleData peopleData;

    public ClientConnection(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
            server.broadcastPeoples(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Log.d(getClass().getSimpleName(), "Connection started");
        while (!socket.isClosed()) {
            try {
                String message = dataInputStream.readUTF();
                Data data = JSONConverter.toJavaObject(message);
                if (data.getClass().getName().equals(PeopleData.class.getName())) {
                    peopleData = (PeopleData) data;
                }
                server.broadcastMessage(message);
                peopleData.setAction(PeopleData.ACTION_NONE);
            } catch (IOException e) {
                close();
                e.printStackTrace();
            }
        }
        Log.d(getClass().getSimpleName(), "Connection closed");
    }

    @Override
    public void close() {
        server.stopConnection(this);
        try {
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

    public PeopleData getPeopleData() {
        return peopleData;
    }
}
