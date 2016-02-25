package org.jukov.lanchat.server;

import android.content.Context;
import android.util.Log;

import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.json.JSONConverter;
import org.jukov.lanchat.service.ServiceHelper;
import org.jukov.lanchat.util.NetworkUtils;
import org.jukov.lanchat.util.Strings;
import org.jukov.lanchat.util.UDP;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by jukov on 05.02.2016.
 */
public class Server extends Thread implements Closeable {

    private Context context;
    private int port;
    private Set<ClientConnection> clientConnections;
    private ExecutorService executorService;
    private TCPListener tcpListener;

    private boolean stopBroadcastFlag;

    public Server(int port, final Context context) {
        this.context = context;
        this.port = port;
        stopBroadcastFlag = false;
        clientConnections = Collections.synchronizedSet(new HashSet<ClientConnection>());
        executorService = Executors.newFixedThreadPool(10);

        tcpListener = new TCPListener(port, new TCPListener.ClientListener() {
            @Override
            public void onReceive(Socket socket) {
                ClientConnection clientConnection = new ClientConnection(socket, getServer());
                executorService.execute(clientConnection);
                clientConnections.add(clientConnection);
                Log.d(getClass().getSimpleName(), "Connections - " + clientConnections.size());
                updateStatus();
            }
        });
        tcpListener.start();
    }

    @Override
    public void run() {
        try {
            InetAddress broadcastAddress = NetworkUtils.getBroadcastAddress(context);
            while (!stopBroadcastFlag) {
                UDP.send(port, broadcastAddress, Strings.SERVER_BROADCAST);
                TimeUnit.MILLISECONDS.sleep(500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
//        for (ClientConnection clientConnection: clientConnections) {
//            clientConnection.close();
//        }
        stopBroadcastFlag = true;
        try {
            tcpListener.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopConnection(ClientConnection clientConnection) {
        clientConnections.remove(clientConnection);
    }

    public Server getServer() {
        return this;
    }

    public void broadcastMessage(String message) {
        for (ClientConnection clientConnection : clientConnections) {
            clientConnection.sendMessage(message);
        }
    }

    public void broadcastPeoples(ClientConnection targetClientConnection) {
        for (ClientConnection clientConnection : clientConnections) {
            try {
                PeopleData peopleData = clientConnection.getPeopleData();
                if (peopleData != null) {
                    peopleData.setAction(PeopleData.ACTION_CONNECT);
                    if (!clientConnection.equals(targetClientConnection))
                        targetClientConnection.sendMessage(JSONConverter.toJSON(peopleData));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateStatus() {
        ServiceHelper.updateStatus(context, "Mode: server; clients - " + clientConnections.size());
    }
}
