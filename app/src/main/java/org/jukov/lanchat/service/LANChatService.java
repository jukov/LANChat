package org.jukov.lanchat.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jukov.lanchat.client.Client;
import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.RoomData;
import org.jukov.lanchat.server.Server;
import org.jukov.lanchat.util.JSONConverter;
import org.jukov.lanchat.util.UDP;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_MESSAGE;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_NAME;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_RECEIVER_UID;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_UID;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.INIT_SERVICE_ACTION;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.NAME_CHANGE_ACTION;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.NEW_ROOM_ACTION;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.SEARCH_SERVER_ACTION;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.SEND_GLOBAL_MESSAGE_ACTION;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.SEND_PRIVATE_MESSAGE_ACTION;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.START_SERVER_ACTION;

/**
 * Created by jukov on 16.01.2016.
 */
public class LANChatService extends Service {

    public static final int TCP_PORT = 1791;
    public static final int UDP_PORT = 1791;

    private int mode;

    public static final int MODE_NONE = 0;
    public static final int MODE_CLIENT = 1;
    public static final int MODE_SERVER = 2;
    private ExecutorService executorService;

    private Server server;

    private Client client;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(getClass().getSimpleName(), "onCreate");
        executorService = Executors.newFixedThreadPool(3);
        mode = MODE_NONE;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(getClass().getSimpleName(), "onStartCommand");
        if (intent != null) {
            Log.d(getClass().getSimpleName(), intent.getAction());
            switch (intent.getAction()) {
                case INIT_SERVICE_ACTION:
                    initService();
                    break;
                case SEARCH_SERVER_ACTION:
                    searchServer();
                    break;
                case START_SERVER_ACTION:
                    startServer(TCP_PORT);
                    break;
                case SEND_GLOBAL_MESSAGE_ACTION:
                    sendGlobalMessage(intent);
                    break;
                case SEND_PRIVATE_MESSAGE_ACTION:
                    sendPrivateMessage(intent);
                    break;
                case NAME_CHANGE_ACTION:
                    changeName(intent);
                    break;
                case NEW_ROOM_ACTION:
                    newRoom(intent);
                    break;
                default:
                    Log.w(getClass().getSimpleName(), "Unexpected intent action type");
            }
        } else {
            Log.i(getClass().getSimpleName(), "Service stopped");
            stopSelf();
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(getClass().getSimpleName(), "onDestroy()");
        super.onDestroy();
        if (client != null) {
            client.close();
            client = null;
        }
        if (server != null) {
            server.close();
            server = null;
        }
        executorService.shutdown();
//        System.exit(0);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void initService() {
        switch (mode) {
            case MODE_NONE:
                ServerSearch serverSearch = new ServerSearch(UDP_PORT);
                executorService.execute(serverSearch);
                break;
            case MODE_CLIENT:
                client.updateStatus();
                break;
            case MODE_SERVER:
                server.updateStatus();
                break;
        }
    }

    public void searchServer() {
        ServerSearch serverSearch = new ServerSearch(UDP_PORT);
        executorService.execute(serverSearch);
    }

    public void sendGlobalMessage(Intent intent) {
        if (client != null) {
            try {
                String message = JSONConverter.toJSON(new ChatData(
                        getApplicationContext(),
                        ServiceHelper.MessageType.GLOBAL, intent.getStringExtra(EXTRA_MESSAGE)
                ));
                Log.d(getClass().getSimpleName(), message);
                client.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendPrivateMessage(Intent intent) {
        if (client != null) {
            try {
                String message = JSONConverter.toJSON(new ChatData(
                        getApplicationContext(),
                        ServiceHelper.MessageType.PRIVATE,
                        intent.getStringExtra(EXTRA_MESSAGE),
                        intent.getStringExtra(EXTRA_RECEIVER_UID)
                ));
                Log.d(getClass().getSimpleName(), message);
                client.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void changeName(Intent intent) {
        if (client != null) {
            client.changeName(intent.getStringExtra(EXTRA_NAME));
        }
    }

    private void newRoom(Intent intent) {
        try {
            String message = JSONConverter.toJSON(new RoomData(
                    intent.getStringExtra(EXTRA_NAME),
                    intent.getStringExtra(EXTRA_UID)
            ));
            client.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    class ServerSearch extends Thread {

        private int port;
        final Semaphore semaphore;

        public ServerSearch(int port) {
            this.port = port;
            semaphore = new Semaphore(1);
        }

        @Override
        public void run() {
            final Boolean[] receive = {false};
            final StringBuffer broadcastIP = new StringBuffer();

            try {
                UDP udp = new UDP(port, new UDP.BroadcastListener() {
                    @Override
                    public void onReceive(String message, String ip) {
                        if (message.equals(UDP.CLIENT_BROADCAST)) {
                            receive[0] = true;
                            broadcastIP.delete(0, broadcastIP.length());
                            broadcastIP.append(ip);
                            semaphore.release();
                        }
                        Log.d(getClass().getSimpleName(), "Receive message");
                    }
                });
                executorService.execute(udp);
                semaphore.acquire();
                Random random = new Random();
                int r = random.nextInt(4000 - 1000) + 1000;
                semaphore.tryAcquire(r, TimeUnit.MILLISECONDS);
                Log.d(getClass().getSimpleName(), Integer.toString(r));

                udp.close();
                Log.d(getClass().getSimpleName(), receive[0] ? "Broadcast received" : "Broadcast not received");

                if (receive[0]) {
                    startClient(broadcastIP.toString(), TCP_PORT);
                } else {
                    startServer(TCP_PORT);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startServer(int port) {
        mode = MODE_SERVER;
        server = new Server(getApplicationContext(), port);
        executorService.execute(server);
        client = new Client(getApplicationContext(), "127.0.0.1", port);
        executorService.execute(client);
    }

    private void startClient(String ip, int port) {
        mode = MODE_CLIENT;
        client = new Client(getApplicationContext(), ip, port);
        executorService.execute(client);
        client.updateStatus();
    }
}
