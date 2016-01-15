package org.jukov.lanchat.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jukov.lanchat.activity.MainActivity;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
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
        Spam spam = new Spam();
        executorService.execute(spam);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class Spam extends Thread {

        public Spam() {
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < 100; i++) {
                    Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
                    intent.putExtra("name", "vasya");
                    intent.putExtra("message", "meow");
                    sendBroadcast(intent);
                    TimeUnit.SECONDS.sleep(1);
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
