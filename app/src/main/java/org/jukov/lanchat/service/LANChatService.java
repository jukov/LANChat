package org.jukov.lanchat.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jukov.lanchat.client.Client;
import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.util.JSONConverter;
import org.jukov.lanchat.server.Server;
import org.jukov.lanchat.util.UDP;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_MESSAGE;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_NAME;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_RECEIVER_UID;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.GLOBAL_CHAT_ACTION;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.NAME_CHANGE_ACTION;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.PRIVATE_CHAT_ACTION;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.SEARCH_SERVER_ACTION;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.START_SERVICE_ACTION;

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
                case START_SERVICE_ACTION:
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
                    break;
                case SEARCH_SERVER_ACTION:
                    ServerSearch serverSearch = new ServerSearch(UDP_PORT);
                    executorService.execute(serverSearch);
                    break;
                case GLOBAL_CHAT_ACTION:
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
                    break;
                case PRIVATE_CHAT_ACTION:
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
                    break;
                case NAME_CHANGE_ACTION:
                    if (client != null) {
                        client.changeName(intent.getStringExtra(EXTRA_NAME));
                    }
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
        }
        if (server != null) {
            server.close();
        }
        executorService.shutdown();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
                        if (message.equals(UDP.SERVER_BROADCAST)) {
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
                semaphore.tryAcquire(2, TimeUnit.SECONDS);

                udp.close();
                Log.d(getClass().getSimpleName(), receive[0] ? "Broadcast received" : "Broadcast not received");

                if (receive[0]) {
                    mode = MODE_CLIENT;
                    startClient(broadcastIP.toString(), TCP_PORT);
                    client.updateStatus();
                } else {
                    mode = MODE_SERVER;
                    startServer(TCP_PORT);
                    startClient("127.0.0.1", TCP_PORT);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startServer(int port) {
        server = new Server(port, getApplicationContext());
        executorService.execute(server);
    }

    private void startClient(String ip, int port) {
        client = new Client(getApplicationContext(), ip, port);
        executorService.execute(client);
    }
}
