package com.company.Threads;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class UI extends Application {
    public void start(Stage stage) {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("..//View/MainWindow.fxml"));
        } catch (IOException e) {
            
            e.printStackTrace();
        }

        stage.setTitle("E-coins");
        stage.setScene(new Scene(root, 900, 700));
    }
}
