package org.jukov.lanchat.network;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jukov on 05.02.2016.
 */
public class TCP extends Thread implements Closeable {

    private ClientListener clientListener;
    private ServerSocket serverSocket;
    private int port;

    public TCP(int port, ClientListener clientListener) {
        this.clientListener = clientListener;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                clientListener.onReceive(socket);
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        serverSocket.close();
    }

    public interface ClientListener {
        void onReceive(Socket socket);
    }

}
