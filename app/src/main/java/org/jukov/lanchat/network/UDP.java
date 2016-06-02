package org.jukov.lanchat.network;

import android.util.Log;

import org.jukov.lanchat.util.Utils;

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

    public static final String CLIENT_BROADCAST = "org.jukov.lanchat.BROADCAST_TO_CLIENTS";
    public static final String SERVER_BROADCAST = "org.jukov.lanchat.BROADCAST_TO_SERVERS";

    private final BroadcastListener broadcastListener;
    private final int port;
    private DatagramSocket receiveSocket;

    private static DatagramSocket sendSocket;

    public UDP(int port, BroadcastListener broadcastListener) {
        this.port = port;
        this.broadcastListener = broadcastListener;
    }

    public static void send(int port, InetAddress broadcastAddress, String message) {
        try {
            if (sendSocket == null) {
                sendSocket = new DatagramSocket(port+1, Utils.getIpAddress());
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
        if (receiveSocket != null) {
            while (!receiveSocket.isClosed()) {
                try {
                    byte[] buf = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    receiveSocket.receive(packet);
                    broadcastListener.onReceive(
                            new String(packet.getData(), 0, packet.getLength()),
                            packet.getAddress().getHostAddress());
                } catch (IOException e) {
                    Log.d(getClass().getSimpleName(), "Stop broadcast catching");
                }
            }
            receiveSocket.close();
        }
    }

    public void close() {
        receiveSocket.close();
    }

    public interface BroadcastListener {
        void onReceive(String message, String ip);
    }

}
