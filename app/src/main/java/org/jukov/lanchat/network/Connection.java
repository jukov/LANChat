package org.jukov.lanchat.network;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by jukov on 05.04.2016.
 */
abstract class Connection implements Closeable, Runnable {

    protected final Socket socket;
    protected final Server server;

    DataOutputStream dataOutputStream;
    DataInputStream dataInputStream;

    private final String remoteIp;

    Connection(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        remoteIp = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().toString().substring(1);
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.sendDataToNewConnection(this);
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void sendMessage(String message) {
        try {
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            dataOutputStream.close();
            dataInputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
