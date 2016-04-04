package org.jukov.lanchat.server;

import android.util.Log;

import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.util.JSONConverter;
import org.jukov.lanchat.service.ServiceHelper;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by jukov on 07.02.2016.
 */
public class ClientConnection extends Thread implements Closeable {

    private Server server;
    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    private PeopleData peopleData;

    public ClientConnection(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
            server.broadcastPeoples(this);
            server.sendMessages(this);
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
                Object data = JSONConverter.toPOJO(message);
                if (data instanceof PeopleData) {
                    peopleData = (PeopleData) data;
                } else if (data instanceof ChatData) {
                    if (((ChatData) data).getMessageType() == ServiceHelper.MessageType.GLOBAL)
                        server.addMessage((ChatData) data);
                }
                server.broadcastMessage(message);
                peopleData.setAction(PeopleData.ACTION_NONE);
            } catch (IOException e) {
                e.printStackTrace();
                close();
            }
        }
        Log.d(getClass().getSimpleName(), "Connection closed");
        server.stopConnection(this);
    }

    @Override
    public void close() {
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
