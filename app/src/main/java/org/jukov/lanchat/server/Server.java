package org.jukov.lanchat.server;

import android.content.Context;
import android.util.Log;

import org.jukov.lanchat.R;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.json.JSONConverter;
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

    private Lock lock;

    private Context context;
    private int port;
    private Set<ClientConnection> clientConnections;
    private ExecutorService executorService;
    private TCPListener tcpListener;

    private boolean stopBroadcastFlag;

    public Server(int port, final Context context) {
        lock = new ReentrantLock();
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
        lock.lock();
        for (ClientConnection clientConnection: clientConnections) {
            clientConnection.close();
        }
        lock.unlock();
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
        lock.lock();
        clientConnections.remove(clientConnection);
        lock.unlock();
        updateStatus();
    }

    public Server getServer() {
        return this;
    }

    public void broadcastMessage(String message) {
        lock.lock();
        for (ClientConnection clientConnection : clientConnections) {
            clientConnection.sendMessage(message);
        }
        lock.unlock();
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
        ServiceHelper.updateStatus(context, context.getString(R.string.nav_header_people_around, clientConnections.size()));
    }
}
