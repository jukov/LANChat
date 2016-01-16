package org.jukov.lanchat.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jukov.lanchat.activity.MainActivity;
import org.jukov.lanchat.network.UDP;

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
        executorService = Executors.newFixedThreadPool(2);
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
            UDP udp = new UDP(getApplicationContext(), port, new UDP.BroadcastListener() {
                @Override
                public void onReceive(String msg, String ip) {
                    Log.d(TAG, "Receive message");
                    Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
                    intent.putExtra("name", ip);
                    intent.putExtra("message", msg);
                    sendBroadcast(intent);
                }
            });
            udp.start();
            try {
                for (int i = 0; i <= 100; i++) {
                    udp.send("Test");
                    Log.d(TAG, "Send broadcast");
                    TimeUnit.SECONDS.sleep(1);
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
