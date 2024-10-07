package com.company.Threads;

import java.io.IOException;
import java.net.ServerSocket;

public class PeerServer extends Thread {

    private ServerSocket serverSocket;

    public PeerServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        while (true) {
            try {

                new PeerRequestThread(serverSocket.accept()).start();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }
}
