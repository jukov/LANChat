package org.jukov.lanchat.server;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by jukov on 05.04.2016.
 */
public abstract class Connection implements Closeable, Runnable {

    protected Socket socket;
    protected Server server;

    protected DataOutputStream dataOutputStream;
    protected DataInputStream dataInputStream;

    private String remoteIp;

    public Connection(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        remoteIp = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().toString().substring(1);
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.broadcastPeoples(this);
        server.sendMessageHistory(this);
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
