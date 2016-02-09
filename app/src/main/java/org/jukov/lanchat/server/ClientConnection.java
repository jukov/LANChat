package org.jukov.lanchat.server;

import android.util.Log;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by jukov on 07.02.2016.
 */
public class ClientConnection extends Thread implements Closeable {

    public static final String TAG = "LC_ClientConnection";

    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    private Server server;

    public ClientConnection(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Connection created");
    }

    @Override
    public void run() {
        Log.d(TAG, "Connection started");
        while (!socket.isClosed()) {
            try {
                String message = dataInputStream.readUTF();
                Log.d(TAG, "Receive message");
                server.broadcastMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "Connection closed");
    }

    @Override
    public void close() throws IOException {
        server.stopConnection(this);
        dataOutputStream.close();
        dataInputStream.close();
        socket.close();
    }

    public void sendMessage(String message) {
        try {
            Log.d(TAG, "In sendMessage()");
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
