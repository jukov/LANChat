package org.jukov.lanchat.client;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by jukov on 06.02.2016.
 */
public class Client {

    private int port;
    private String ip;
    private Socket socket;

    public Client(String ip, int port) {
        this.port = port;
        this.ip = ip;
        try {
            socket = new Socket(ip, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
