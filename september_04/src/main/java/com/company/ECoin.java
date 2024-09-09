package com.company;

import com.company.Threads.MiningThread;
import com.company.Threads.PeerClient;
import com.company.Threads.PeerServer;
import com.company.Threads.UI;
import javafx.application.Application;
import javafx.stage.Stage;

public class ECoin extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        new UI().start(primaryStage);
        new PeerClient().start();
        new PeerServer(6000).start();
        new MiningThread().start();
    }

    @Override
    public void init() {

    }
}
