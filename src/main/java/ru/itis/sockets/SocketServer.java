package ru.itis.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {

    private List<Client> clients = new CopyOnWriteArrayList<>();

    private final int PLAYERS_NUMBER = 2;

    public boolean isEnoughPlayers() {
        return clients.size() == PLAYERS_NUMBER;
    }

    public void start(int port) {
        new Thread() {

            ServerSocket serverSocket;
            {
                try {
                    serverSocket = new ServerSocket(port);

                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        Client client = new Client(clientSocket);
                        clients.add(client);

                        client.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private BufferedReader readerFromClient(Socket client) {
        try {
            return new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private PrintWriter writerToClient(Socket client) {
        try {
            return new PrintWriter(client.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static SocketServer socketServer;

    public static SocketServer getInstance() {
        if (socketServer == null) {
            socketServer = new SocketServer();
        }
        return socketServer;
    }

    public List<Client> getClients() {
        return clients;
    }
}
