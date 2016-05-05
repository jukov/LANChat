package org.jukov.lanchat.network;

import android.content.Context;
import android.util.Log;

import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.DataBundle;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.dto.RoomData;
import org.jukov.lanchat.dto.ServiceData;
import org.jukov.lanchat.util.JSONConverter;
import org.jukov.lanchat.util.UDP;
import org.jukov.lanchat.util.Utils;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by jukov on 05.02.2016.
 */
public class Server extends Thread implements Closeable {

    public static final String TAG = Server.class.getSimpleName();
    public static final int CLIENT_THREADS_COUNT = 10;
    public static final int SERVER_THREADS_COUNT = 3;
    public static final int GLOBAL_CHAT_MESSAGES_MAX_CAPACITY = 50;
    public static final int ROOMS_MAX_CAPACITY = 50;

    private Context context;
    private int port;

    private Lock messagesBundleLock;
    private Lock roomsBundleLock;
    private ThreadPoolExecutor clientExecutor;
    private ThreadPoolExecutor serverExecutor;
    private TCPListener clientListener;
    private TCPListener serverListener;
    private UDP udpServerListener;

    private Set<ClientConnection> clientConnections;
    private Set<ServerConnection> serverConnections;
    private Set<String> serverIps;

    private DataBundle<ChatData> messages;
    private DataBundle<RoomData> rooms;

    private boolean stopBroadcastFlag;
    private boolean sendServerBroadcastFlag;

    public Server(final Context context, int port) {
        Log.d(TAG, "Server constructor");
        this.context = context;
        this.port = port;

        stopBroadcastFlag = false;
        sendServerBroadcastFlag = false;

        clientConnections = Collections.synchronizedSet(new HashSet<ClientConnection>(CLIENT_THREADS_COUNT));
        serverConnections = Collections.synchronizedSet(new HashSet<ServerConnection>(SERVER_THREADS_COUNT));
        serverIps = Collections.synchronizedSet(new HashSet<String>());
        serverIps.add(Utils.getWifiAddress(context));

        messages = new DataBundle<>(GLOBAL_CHAT_MESSAGES_MAX_CAPACITY);
        rooms = new DataBundle<>(ROOMS_MAX_CAPACITY);

        messagesBundleLock = new ReentrantLock();
        roomsBundleLock = new ReentrantLock();

        clientExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        serverExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        clientListener = new TCPListener(port, new TCPListener.ClientListener() {
            @Override
            public void onReceive(Socket socket) { //listener for client connections
                ClientConnection clientConnection = new ClientConnection(socket, getServer());
                clientExecutor.execute(clientConnection);
                clientConnections.add(clientConnection);
                Log.d(TAG, Integer.toString(clientExecutor.getActiveCount()));
            }
        });
        clientListener.start();

        serverListener = new TCPListener(port+1, new TCPListener.ClientListener() { //listener for server connections
            @Override
            public void onReceive(Socket socket) {
                String ip = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().toString().substring(1);
                Log.d(TAG, "Trying to accept " + ip);
                Log.d(TAG, Utils.getWifiAddress(context));
                Log.d(TAG, Arrays.toString(serverIps.toArray()));
                if (!serverIps.contains(ip)) {
                    serverIps.add(ip);
                    if (udpServerListener != null)
                        udpServerListener.close();
                    Log.d(TAG, "Server accepted " + ip);
                    ServerConnection serverConnection = new ServerConnection(socket, getServer());
                    serverExecutor.execute(serverConnection);
                    serverConnections.add(serverConnection);
                } else {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        serverListener.start();

        udpServerListener = new UDP(port, new UDP.BroadcastListener() {
            @Override
            public void onReceive(String message, String ip) {
                if (message.equals(UDP.SERVER_BROADCAST)) {
                    if (!serverIps.contains(ip)) {
                        try {
                            serverIps.add(ip);
                            Log.d(TAG, "Catched broadcast from server " + ip);
                            Socket socket = new Socket(ip, getServer().port + 1);
                            ServerConnection serverConnection = new ServerConnection(socket, getServer());
                            serverExecutor.execute(serverConnection);
                            serverConnections.add(serverConnection);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            closeUDP();
                        }
                    }
                }
            }
        });
        udpServerListener.start();
    }

    @Override
    public void run() {
        try {
            short count = 0;
            InetAddress broadcastAddress = Utils.getBroadcastAddress(context);
            while (!stopBroadcastFlag) {
                if (clientExecutor.getActiveCount() < CLIENT_THREADS_COUNT)
                    UDP.send(port, broadcastAddress, UDP.CLIENT_BROADCAST);
                TimeUnit.MILLISECONDS.sleep(500);
                if (sendServerBroadcastFlag) { //server must start sending broadcast to servers after 5 seconds after start
                    if (serverExecutor.getActiveCount() < SERVER_THREADS_COUNT)
                        UDP.send(port, broadcastAddress, UDP.SERVER_BROADCAST);
                } else {
                    count++;
                    if (count > 10) {
                        sendServerBroadcastFlag = true;
                        Log.d(TAG, "Starting broadcast to servers");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeUDP() {
        udpServerListener.close();
        Log.d(TAG, "Close UDP");
    }

    public void close() {
        Log.d(TAG, "Close server");
        stopBroadcastFlag = true;

        for (ClientConnection clientConnection: clientConnections) {
            if (!clientConnection.isLocal()) {
                try {
                    clientConnection.sendMessage(
                            JSONConverter.toJSON(new ServiceData(
                                    ServiceData.MessageType.DELEGATION_SERVER_STATUS)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        for (ClientConnection clientConnection : clientConnections) {
            clientConnection.close();
        }

        /*
        * It needed for send to servers new node address.
        * New node is first server in Set.
        */
        //TODO check this
        Iterator<ServerConnection> iterator = serverConnections.iterator();
        ServerConnection serverConnection1;
        String newNodeIp;
        if (iterator.hasNext()) {
            serverConnection1 = iterator.next();
            newNodeIp = serverConnection1.getRemoteIp();
            Log.d(TAG, "Server has one connection " + newNodeIp);
            while (iterator.hasNext()) {
                serverConnection1 = iterator.next();
                Log.d(TAG, "Server has more than one connection " + serverConnection1.getRemoteIp());
                try {
                    serverConnection1.sendMessage(
                            JSONConverter.toJSON(new ServiceData(
                                    newNodeIp, ServiceData.MessageType.NEW_NODE_ADDRESS)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        for (ServerConnection serverConnection : serverConnections) {
            serverConnection.close();
        }

        try {
            clientListener.close();
            serverListener.close();
            udpServerListener.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientExecutor.shutdown();
        serverExecutor.shutdown();
    }

    public void stopConnection(Connection connection) {
        if (connection instanceof ServerConnection) {
            serverIps.remove(connection.getRemoteIp());
            serverConnections.remove(connection);
        } else {
            clientConnections.remove(connection);
        }
    }

    public void connectToServer(String data, ServerConnection closingConnection) {
        Log.d(TAG, "Connect to server " + data);
        Log.d(TAG, "My ip: " + Utils.getWifiAddress(context));
        closingConnection.close();
        try {
            Socket socket = new Socket(data, getServer().port + 1);
            ServerConnection serverConnection = new ServerConnection(socket, getServer());
            serverExecutor.execute(serverConnection);
            serverConnections.add(serverConnection);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Server getServer() {
        return this;
    }

    public void broadcastMessageToClients(String message) {
        for (ClientConnection clientConnection : clientConnections) {
            clientConnection.sendMessage(message);
        }
    }

    public void broadcastMessageToServers(String message) {
        broadcastMessageToServers(message, null);
    }

    public void broadcastMessageToServers(String message, ServerConnection excludeConnection) {
        for (ServerConnection serverConnection : serverConnections) {
            if (!serverConnection.equals(excludeConnection)) {
                serverConnection.sendMessage(message);
                Log.d(TAG, "sendMessageToServer");
                Log.d(TAG, Integer.toString(serverConnections.size()));
            }
        }
    }

    public void sendDataToNewConnection(Connection connection) {
        try {
            //Send connected people
            for (ClientConnection clientConnection : clientConnections) {
                PeopleData peopleData = clientConnection.getPeopleData();
                if (peopleData != null) {
                    peopleData.setAction(PeopleData.ActionType.CONNECT);
                    if (!clientConnection.equals(connection)) {
                        Log.d(TAG, peopleData.toString());
                        connection.sendMessage(JSONConverter.toJSON(peopleData));
                    }
                }
            }

            //Send message history
            if (messages.size() > 0)
                connection.sendMessage(JSONConverter.toJSON(messages));

            //Send existing rooms
            if (rooms.size() > 0)
                connection.sendMessage(JSONConverter.toJSON(rooms));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addMessage(ChatData chatData) {
        messagesBundleLock.lock();
        messages.add(chatData);
        messagesBundleLock.unlock();
    }


    public void addRoom(RoomData roomData) {
        roomsBundleLock.lock();
        rooms.add(roomData);
        roomsBundleLock.unlock();
    }
}
