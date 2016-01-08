package org.jukov.lanchat.network;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

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

    private BroadcastListener broadcastListener;
    private Context context;
    private DatagramSocket datagramSocket;
    private int port;

    public UDP(Context context, int port, BroadcastListener broadcastListener) {
        this.broadcastListener = broadcastListener;
        this.context = context;
        this.port = port;
    }

    public void send(String msg) {
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            clientSocket.setBroadcast(true);
            byte[] sendData = msg.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length, getBroadcastAddress(context), port);
            clientSocket.send(sendPacket);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            datagramSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (!datagramSocket.isClosed()) {
            try {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                datagramSocket.receive(packet);
                broadcastListener.onReceive(
                        new String(packet.getData(), 0, packet.getLength()),
                        packet.getAddress().getHostAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        datagramSocket.close();
    }

    public void close() {
        datagramSocket.close();
    }

    public interface BroadcastListener {
        void onReceive(String msg, String ip);
    }

    public static InetAddress getBroadcastAddress(Context context) throws IOException {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if(dhcp == null)
            return InetAddress.getByName("255.255.255.255");
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

}
