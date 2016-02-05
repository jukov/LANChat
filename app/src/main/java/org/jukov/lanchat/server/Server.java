package org.jukov.lanchat.server;

import android.content.Context;
import android.content.Intent;

import org.jukov.lanchat.network.TCP;
import org.jukov.lanchat.network.UDP;
import org.jukov.lanchat.util.BroadcastStrings;
import org.jukov.lanchat.util.IntentStrings;
import org.jukov.lanchat.util.NetworkUtils;

import java.io.Closeable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by jukov on 05.02.2016.
 */
public class Server extends Thread implements Closeable {

    private Context context;
    private int port;
    private List<Socket> clientSockets;
    private TCP tcp;

    private boolean stopBroadcastFlag;

    public Server(int port, final Context context) {
        this.context = context;
        this.port = port;
        stopBroadcastFlag = false;
        clientSockets = Collections.synchronizedList(new ArrayList<Socket>());

        tcp = new TCP(port, new TCP.ClientListener() {
            @Override
            public void onReceive(Socket socket) {
                if (!clientSockets.contains(socket)) {
                    clientSockets.add(socket);
                    Intent intent = new Intent(IntentStrings.BROADCAST_ACTION);
                    intent.putExtra(IntentStrings.EXTRA_DEBUG, "Mode: server; clients - " + clientSockets.size());
                    context.sendBroadcast(intent);
                }
            }
        });
    }

    @Override
    public void run() {
        try {
            InetAddress broadcastAddress = NetworkUtils.getBroadcastAddress(context);
            while (!stopBroadcastFlag) {
                UDP.send(port, broadcastAddress, BroadcastStrings.SERVER_BROADCAST);
                TimeUnit.MILLISECONDS.sleep(500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        stopBroadcastFlag = true;
    }
}
