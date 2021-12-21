package ru.itis.sockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import ru.itis.controllers.MainController;
import ru.itis.protocols.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient extends Thread {
    private Socket client;

    private MainController controller;

    private PrintWriter printWriter; // на SocketServer fromClient
    private BufferedReader bufferedReader; // на SocketServer toClient

    public SocketClient(String host, int port) {
        try {
            client = new Socket(host, port);
            printWriter = new PrintWriter(client.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void sendMessage(Message message) {
        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(message);
            printWriter.println(jsonMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public BufferedReader getFromServer() {
        return bufferedReader;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String futureMessage = bufferedReader.readLine();
                ObjectMapper objectMapper = new ObjectMapper();
                Message message = objectMapper.readValue(futureMessage, Message.class);

                switch (message.getType()) {
                    case START: {
                        System.out.println(message);
                        Platform.runLater(() -> controller.startGame());
                        break;
                    }
                    case SELECT_PIXEL: {
                        System.out.println(message);
                        String[] coordinates = message.getBody().split(",");
                        Platform.runLater(() -> controller.fillCell(coordinates[0], coordinates[1]));
                        break;
                    }
                    case STOP: {
                        System.out.println(message);
                        Platform.runLater(() -> controller.stopGame());
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public MainController getController() {
        return controller;
    }

    public void setController(MainController controller) {
        this.controller = controller;
    }
}
