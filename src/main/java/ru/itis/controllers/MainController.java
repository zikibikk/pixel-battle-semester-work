package ru.itis.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import ru.itis.protocols.Message;
import ru.itis.protocols.Type;
import ru.itis.sockets.SocketClient;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainController implements Initializable {

    private final Integer ROUND_TIME = 65;
    private SocketClient socketClient;
    private ScheduledExecutorService service;

    @FXML
    private AnchorPane pane;

    @FXML
    private Label timerLabel;

    @FXML
    private ColorPicker colorPicker;

    private GridPane grid;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //создаем табличную панель
        grid = new GridPane();
        grid.setLayoutX(50);
        grid.setLayoutY(65);
        grid.setPrefSize(500, 500);
        grid.setGridLinesVisible(true);
        grid.setDisable(true); //блокируем
        //заполняем таблицу клетками
        for (int i = 0; i < 50; i++) {
            //столбцы
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPrefWidth(10);

            //строки
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPrefHeight(10);

            grid.getColumnConstraints().add(colConst);
            grid.getRowConstraints().add(rowConst);
        }
        //заполняем клетки прямоугольниками
        for (int i = 0; i < 50; i++) {
            for (int y = 1; y <= 50; y++) {
                Rectangle cell = new Rectangle(9, 9, Paint.valueOf("green"));
                cell.setId(String.valueOf(i * 50 + y)); //id клетки по позиции в GridPane
                //при нажатии:
                cell.setOnMouseClicked(event -> {
                    cell.setFill(colorPicker.getValue()); //меняет цвет
                    Message message = new Message();
                    message.setType(Type.SELECT_PIXEL);
                    message.setBody(cell.getId() + "," + colorPicker.getValue());
                    socketClient.sendMessage(message);
                });
                grid.add(cell, i, y - 1, 1, 1);
            }
        }
        //добавляем панель на сцену
        pane.getChildren().add(grid);

        //создаем клиента
        socketClient = new SocketClient("localhost", 7777);
        socketClient.setController(this);
        socketClient.start();
        service = Executors.newScheduledThreadPool(2);
        // запускаем слушателя сообщений
        //отправляем серверу сообщение о том, что клиент подключился и готов к запуску игры
        Message connectMessage = new Message();
        connectMessage.setType(Type.START);
        socketClient.sendMessage(connectMessage);
    }

    public void startGame() { //начало игры
        grid.setDisable(false); //разблокируем панель
        service.schedule(() -> Platform.runLater(() -> stopGame()), ROUND_TIME + 1, TimeUnit.SECONDS); //ставим таймер окончания

        //отрисовка таймера
        timerLabel.setText(formatTime(ROUND_TIME));
        Timeline timer = new Timeline(new KeyFrame(Duration.seconds(1), animation ->
                timerLabel.setText(formatTime(unFormatTime(timerLabel.getText()) - 1))));
        timer.setCycleCount(ROUND_TIME);
        timer.play();
    }

    private String formatTime(Integer seconds) { //время из секунд в формат MM:SS
        Integer minutes = seconds / 60;
        String minutesAsString = String.valueOf(minutes);
        if (minutes < 10) {
            minutesAsString = "0" + minutesAsString;
        }

        seconds = seconds % 60;
        String secondsAsString = String.valueOf(seconds);
        if (seconds < 10) {
            secondsAsString = "0" + secondsAsString;
        }

        return minutesAsString + ":" + secondsAsString;
    }

    private Integer unFormatTime(String time) { //время из формата MM:SS в секунды
        String[] timeArray = time.split(":");
        return Integer.parseInt(timeArray[0]) * 60 + Integer.parseInt(timeArray[1]);
    }

    public void fillCell(String id, String color) { //покрасить клетку по id и цвету
        Rectangle cell = (Rectangle) grid.getChildren().get(Integer.parseInt(id));
        cell.setFill(Paint.valueOf(color));
    }

    public void stopGame() { //завершение игры
        timerLabel.setText("Time's up!");
        grid.setDisable(true); //блокируем панель
        Message stopMessage = new Message();
        stopMessage.setType(Type.STOP);
        socketClient.sendMessage(stopMessage); //отправляем серверу сообщение об остановке игры
        socketClient.stop(); //отключаем клиента
    }
}
