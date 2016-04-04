package org.jukov.lanchat.server;

import android.content.Context;
import android.util.Log;

import org.jukov.lanchat.R;
import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.DataBundle;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.util.JSONConverter;
import org.jukov.lanchat.service.ServiceHelper;
import org.jukov.lanchat.util.UDP;
import org.jukov.lanchat.util.Utils;

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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by jukov on 05.02.2016.
 */
public class Server extends Thread implements Closeable {

    private Context context;
    private int port;

    private Lock connectionsLock;
    private Lock bundleLock;
    private ExecutorService executorService;
    private TCPListener tcpListener;

    private Set<ClientConnection> clientConnections;
    private DataBundle<ChatData> messages;

    private boolean stopBroadcastFlag;

    public Server(int port, final Context context) {
        connectionsLock = new ReentrantLock();
        bundleLock = new ReentrantLock();
        this.context = context;
        this.port = port;
        stopBroadcastFlag = false;
        clientConnections = Collections.synchronizedSet(new HashSet<ClientConnection>());
        messages = new DataBundle<>(50);
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
            InetAddress broadcastAddress = Utils.getBroadcastAddress(context);
            while (!stopBroadcastFlag) {
                UDP.send(port, broadcastAddress, UDP.SERVER_BROADCAST);
                TimeUnit.MILLISECONDS.sleep(500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        connectionsLock.lock();
        for (ClientConnection clientConnection: clientConnections) {
            clientConnection.close();
        }
        connectionsLock.unlock();
        stopBroadcastFlag = true;
        try {
            tcpListener.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
    }

    public void stopConnection(ClientConnection clientConnection) {
        Log.i(getClass().getSimpleName(), clientConnection.getName() + " disconnected");
        connectionsLock.lock();
        clientConnections.remove(clientConnection);
        connectionsLock.unlock();
        updateStatus();
    }

    public Server getServer() {
        return this;
    }

    public void broadcastMessage(String message) {
        connectionsLock.lock();
        for (ClientConnection clientConnection : clientConnections) {
            clientConnection.sendMessage(message);
        }
        connectionsLock.unlock();
    }

    public void broadcastPeoples(ClientConnection targetClientConnection) {
        for (ClientConnection clientConnection : clientConnections) {
            try {
                PeopleData peopleData = clientConnection.getPeopleData();
                if (peopleData != null) {
                    peopleData.setAction(PeopleData.ACTION_CONNECT);
                    if (!clientConnection.equals(targetClientConnection)) {
                        connectionsLock.lock();
                        targetClientConnection.sendMessage(JSONConverter.toJSON(peopleData));
                        connectionsLock.unlock();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessages(ClientConnection clientConnection) {
        if (messages.size() > 0) {
            try {
                clientConnection.sendMessage(JSONConverter.toJSON(messages));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addMessage(ChatData chatData) {
        bundleLock.lock();
            messages.add(chatData);
            Log.d(getClass().getSimpleName(), messages.toString());
        bundleLock.unlock();
    }

    public void updateStatus() {//TODO: move updating to client
        ServiceHelper.updateStatus(context, context.getString(R.string.nav_header_people_around, clientConnections.size()));
    }
}
