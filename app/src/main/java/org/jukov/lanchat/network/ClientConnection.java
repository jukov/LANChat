package org.jukov.lanchat.network;

import android.util.Log;

import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.MessagingData;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.dto.RoomData;
import org.jukov.lanchat.util.JSONConverter;
import org.jukov.lanchat.util.Utils;

import java.io.IOException;
import java.net.Socket;
import java.util.AbstractCollection;

import static org.jukov.lanchat.dto.ChatData.MessageType.GLOBAL;

/**
 * Created by jukov on 07.02.2016.
 */
class ClientConnection extends Connection {

    public static final String TAG = ClientConnection.class.getSimpleName();

    private PeopleData peopleData;

    public ClientConnection(Socket socket, Server server) {
        super(socket, server);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        Log.d(getClass().getSimpleName(), "Connection started");
        try {
            while (!socket.isClosed()) {
//                String message = dataInputStream.readUTF();
                int length = dataInputStream.readInt();
                byte[] messageArray = new byte[length];
                dataInputStream.readFully(messageArray);
//                Log.d(getClass().getSimpleName(), message);
                String message = new String(messageArray, "UTF-8");
                Log.d(getClass().getSimpleName(), "Receive message");
                Object data = JSONConverter.toPOJO(message);
                if (data instanceof PeopleData) {
                    peopleData = (PeopleData) data;
                } else
                if (data instanceof ChatData) {
                    if (((ChatData) data).getMessageType() == GLOBAL)
                        server.addMessage((ChatData) data);
                } else if (data instanceof RoomData) {
                    RoomData roomData = (RoomData) data;
                    server.addOrRenameRoom(roomData);
                } else if (data instanceof AbstractCollection) {
                    Log.d(getClass().getSimpleName(), "receive AbstractCollection");
                    AbstractCollection dataBundle = (AbstractCollection) data;
                    MessagingData messagingData = (MessagingData) dataBundle.iterator().next();
                    if (messagingData instanceof RoomData) {
                        server.addOrRenameRoom(dataBundle);
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
        Log.d(TAG, "isLocal() " + socket.getRemoteSocketAddress().toString());
        Log.d(TAG, "isLocal() " + Utils.getIpAddress().getHostAddress());
        return socket.getRemoteSocketAddress().toString().contains(Utils.getIpAddress().getHostAddress());
    }
}
