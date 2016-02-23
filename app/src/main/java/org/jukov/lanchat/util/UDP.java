package org.jukov.lanchat.util;

import android.content.Context;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by jukov on 08.01.2016.
 */
public class UDP extends Thread implements Closeable {

    public static final String TAG = "LC_UDP";

    private BroadcastListener broadcastListener;
    private Context context;
    private DatagramSocket receiveSocket;
    private int port;

    public static DatagramSocket sendSocket;

    public UDP(int port, BroadcastListener broadcastListener) {
        this.port = port;
        this.broadcastListener = broadcastListener;
    }

    public static void send(int port, InetAddress broadcastAddress, String message) {
        try {
            if (sendSocket == null) {
                sendSocket = new DatagramSocket();
            }
            sendSocket.setBroadcast(true);
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length, broadcastAddress, port);
            sendSocket.send(sendPacket);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            receiveSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (!receiveSocket.isClosed()) {
            try {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                receiveSocket.receive(packet);
                broadcastListener.onReceive(
                        new String(packet.getData(), 0, packet.getLength()),
                        packet.getAddress().getHostAddress());
            } catch (IOException e) {
                Log.d(TAG, "Stop broadcast catching");
            }
        }
        receiveSocket.close();
    }

    public void close() {
        receiveSocket.close();
    }

    public interface BroadcastListener {
        void onReceive(String message, String ip);
    }

}
