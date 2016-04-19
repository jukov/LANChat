package org.jukov.lanchat.server;

import android.util.Log;

import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.ServiceData;
import org.jukov.lanchat.service.ServiceHelper;
import org.jukov.lanchat.util.JSONConverter;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by jukov on 05.04.2016.
 */
public class ServerConnection extends Connection {

    public ServerConnection(Socket socket, Server server) {
        super(socket, server);
    }

    @Override
    public void run() {
        Log.d(getClass().getSimpleName(), "Connection started");
        try {
            while (!socket.isClosed()) {
                Log.d(getClass().getSimpleName(), "ReceiveMessage");
                String message = dataInputStream.readUTF();
                Object data = JSONConverter.toPOJO(message);
                if (data instanceof ChatData) {
                    if (((ChatData) data).getMessageType() == ServiceHelper.MessageType.GLOBAL)
                        server.addMessage((ChatData) data);
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
