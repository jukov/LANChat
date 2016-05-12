package org.jukov.lanchat.network;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jukov on 05.02.2016.
 */
class TCPListener extends Thread implements Closeable {

    private final ClientListener clientListener;
    private ServerSocket serverSocket;

    public TCPListener(int port, ClientListener clientListener) {
        this.clientListener = clientListener;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            if (serverSocket != null) {
                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    clientListener.onReceive(socket);
                }
                serverSocket.close();
            }
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        if (serverSocket != null)
            serverSocket.close();
    }

    public interface ClientListener {
        void onReceive(Socket socket);
    }

}
