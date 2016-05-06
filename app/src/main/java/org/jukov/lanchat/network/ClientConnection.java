package org.jukov.lanchat.network;

import android.util.Log;

import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.MessagingData;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.dto.RoomData;
import org.jukov.lanchat.service.ServiceHelper;
import org.jukov.lanchat.util.JSONConverter;

import java.io.IOException;
import java.net.Socket;
import java.util.AbstractCollection;

/**
 * Created by jukov on 07.02.2016.
 */
public class ClientConnection extends Connection {

    private PeopleData peopleData;

    public ClientConnection(Socket socket, Server server) {
        super(socket, server);
    }

    @Override
    public void run() {
        Log.d(getClass().getSimpleName(), "Connection started");
        try {
            while (!socket.isClosed()) {
                String message = dataInputStream.readUTF();
                Log.d(getClass().getSimpleName(), "Receive message");
                Object data = JSONConverter.toPOJO(message);
                if (data instanceof PeopleData) {
                    peopleData = (PeopleData) data;
                } else if (data instanceof ChatData) {
                    if (((ChatData) data).getMessageType() == ServiceHelper.MessageType.GLOBAL)
                        server.addMessage((ChatData) data);
                } else if (data instanceof RoomData) {
                    RoomData roomData = (RoomData) data;
                    server.addRoom(roomData);
                } else if (data instanceof AbstractCollection) {
                    Log.d(getClass().getSimpleName(), "receive AbstractCollection");
                    AbstractCollection dataBundle = (AbstractCollection) data;
                    MessagingData messagingData = (MessagingData) dataBundle.iterator().next();
                    if (messagingData instanceof RoomData) {
                        server.addRoom(dataBundle);
                    }
                }
                server.broadcastMessageToClients(message);
                server.broadcastMessageToServers(message);
                peopleData.setAction(PeopleData.ActionType.NONE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
        Log.d(getClass().getSimpleName(), "Connection closed");
        server.stopConnection(this);
    }

    public PeopleData getPeopleData() {
        return peopleData;
    }

    public boolean isLocal() {
        return socket.getRemoteSocketAddress().toString().contains("127.0.0.1");
    }
}
