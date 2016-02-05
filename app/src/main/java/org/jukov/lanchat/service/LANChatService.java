package org.jukov.lanchat.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jukov.lanchat.network.UDP;
import org.jukov.lanchat.util.IntentStrings;
import org.jukov.lanchat.util.NetworkUtils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by jukov on 16.01.2016.
 */
public class LANChatService extends Service {

    public static final String TAG = "LANChat_Service";

    private ExecutorService executorService;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        executorService = Executors.newFixedThreadPool(3);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        UDPWork udpWork = new UDPWork(1791);
        executorService.execute(udpWork);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class UDPWork extends Thread {

        private int port;

        public UDPWork(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            final Boolean[] receive = {false};
            try {
                UDP udp = new UDP(port, NetworkUtils.getBroadcastAddress(getApplicationContext()), new UDP.BroadcastListener() {
                    @Override
                    public void onReceive(String msg, String ip) {
                        receive[0] = true;
                        Log.d(TAG, "Receive message");
                        Intent intent = new Intent(IntentStrings.BROADCAST_ACTION);
                        intent.putExtra(IntentStrings.EXTRA_NAME, ip);
                        intent.putExtra(IntentStrings.EXTRA_MESSAGE, msg);
                        sendBroadcast(intent);
                    }
                });
                executorService.execute(udp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                TimeUnit.SECONDS.sleep(2);
                Log.d(TAG, receive[0] ? "Broadcast received" : "Broadcast not received");
                if (receive[0]) {
                    startServer();
                }
                startClient();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void startClient() {
    }

    private void startServer() {

    }
}
