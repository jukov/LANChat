package org.jukov.lanchat.network;

import android.util.Log;

import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.MessagingData;
import org.jukov.lanchat.dto.RoomData;
import org.jukov.lanchat.dto.ServiceData;
import org.jukov.lanchat.util.JSONConverter;

import java.io.IOException;
import java.net.Socket;
import java.util.AbstractCollection;

/**
 * Created by jukov on 05.04.2016.
 */
class ServerConnection extends Connection {

    public ServerConnection(Socket socket, Server server) {
        super(socket, server);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        Log.d(getClass().getSimpleName(), "Connection started");
        try {
            while (!socket.isClosed()) {
                Log.d(getClass().getSimpleName(), "ReceiveMessage");
                String message = dataInputStream.readUTF();
                Object data = JSONConverter.toPOJO(message);
                if (data instanceof ChatData) {
                    if (((ChatData) data).getMessageType() == ChatData.MessageType.GLOBAL)
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
                } else if (data instanceof ServiceData) {
                    ServiceData serviceData = (ServiceData) data;
                    Log.d(getClass().getSimpleName(), "Receive ServiceData " + serviceData.getData());
                    if (serviceData.getMessageType() == ServiceData.MessageType.NEW_NODE_ADDRESS) {
                        server.connectToServer(serviceData.getData(), this);
                    }
                    break;
                }
                server.broadcastMessageToClients(message);
                server.broadcastMessageToServers(message, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
        Log.d(getClass().getSimpleName(), "Connection closed");
        server.stopConnection(this);
    }
}
