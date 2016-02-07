package org.jukov.lanchat.client;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.jukov.lanchat.util.IntentStrings;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by jukov on 06.02.2016.
 */
public class Client extends Thread implements Closeable {

    public static final String TAG = "LC_Client";

    private Context context;
    private int port;
    private String ip;

    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    public Client(Context context, String ip, int port) {
        this.context = context;
        this.port = port;
        this.ip = ip;
        try {
            socket = new Socket(ip, port);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @Override
    public void run() {
        Log.d(TAG, "Client started");
        try {
            while (!socket.isClosed()) {
                String message = dataInputStream.readUTF();
                Log.d(TAG, "Receive message");
                Intent intent = new Intent(IntentStrings.BROADCAST_ACTION);
                intent.putExtra(IntentStrings.EXTRA_TYPE, IntentStrings.TYPE_MESSAGE);
                intent.putExtra(IntentStrings.EXTRA_NAME, socket.getInetAddress().toString());
                intent.putExtra(IntentStrings.EXTRA_MESSAGE, message);
                context.sendBroadcast(intent);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        dataOutputStream.close();
        dataInputStream.close();
        socket.close();
    }
}
