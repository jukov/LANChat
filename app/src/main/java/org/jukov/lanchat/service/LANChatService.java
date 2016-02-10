package org.jukov.lanchat.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jukov.lanchat.client.Client;
import org.jukov.lanchat.dto.MessageDTO;
import org.jukov.lanchat.network.UDP;
import org.jukov.lanchat.server.Server;
import org.jukov.lanchat.util.BroadcastStrings;
import org.jukov.lanchat.util.IntentStrings;
import org.jukov.lanchat.util.JSONConverter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by jukov on 16.01.2016.
 */
public class LANChatService extends Service {

    public static final String TAG = "LC_Service";
    public static final int TCP_PORT = 1791;
    public static final int UDP_PORT = 1791;

    private ExecutorService executorService;

    private Server server;
    private Client client;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        executorService = Executors.newFixedThreadPool(3);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (intent.hasExtra(IntentStrings.EXTRA_TYPE)) {
            switch (intent.getStringExtra(IntentStrings.EXTRA_TYPE)) {
                case IntentStrings.TYPE_START_SERVICE:
                    ServerSearch serverSearch = new ServerSearch(UDP_PORT);
                    executorService.execute(serverSearch);
                    break;
                case IntentStrings.TYPE_MESSAGE:
                    if (client != null) {
                        Log.d(TAG, "In Type_Message, client != null");
                        try {
                            String message = JSONConverter.toJSON(new MessageDTO(client.getLocalIP(), intent.getStringExtra(IntentStrings.EXTRA_MESSAGE)));
                            client.sendMessage(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    Log.d(TAG, "Unexpected intent type");
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (server != null) {
            server.close();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class ServerSearch extends Thread {

        private int port;

        public ServerSearch(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            final Boolean[] receive = {false};
            final StringBuffer broadcastIP = new StringBuffer();

            try {
                UDP udp = new UDP(port, new UDP.BroadcastListener() {
                    @Override
                    public void onReceive(String message, String ip) {
                        if (message.equals(BroadcastStrings.SERVER_BROADCAST)) {
                            receive[0] = true;
                            broadcastIP.delete(0, broadcastIP.length());
                            broadcastIP.append(ip);
                        }
                        Log.d(TAG, "Receive message");
                        Intent intent = new Intent(IntentStrings.BROADCAST_ACTION);
                        intent.putExtra(IntentStrings.EXTRA_TYPE, IntentStrings.TYPE_MESSAGE);
                        intent.putExtra(IntentStrings.EXTRA_NAME, ip);
                        intent.putExtra(IntentStrings.EXTRA_MESSAGE, message);
                        sendBroadcast(intent);
                    }
                });
                executorService.execute(udp);

                TimeUnit.SECONDS.sleep(2);
                udp.close();
                Log.d(TAG, receive[0] ? "Broadcast received" : "Broadcast not received");

                if (receive[0]) {
                    startClient(broadcastIP.toString(), TCP_PORT);
                } else {
                    startServer(TCP_PORT);
                    startClient("127.0.0.1", TCP_PORT);
                }
                Intent intent = new Intent(IntentStrings.BROADCAST_ACTION);
                intent.putExtra(IntentStrings.EXTRA_TYPE, IntentStrings.TYPE_UNLOCK_VIEWS);
                sendBroadcast(intent);
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
