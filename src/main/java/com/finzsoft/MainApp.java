package com.finzsoft;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class MainApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) {
        Application.launch(MainApp.class, (java.lang.String[])null);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            AnchorPane page = (AnchorPane) FXMLLoader.load(MainApp.class.getResource("/fxml/bossDeployer.fxml"));
            Scene scene = new Scene(page);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Issue Tracking Sample");
            primaryStage.show();
        } catch (Exception ex) {
            log.error("Error starting up:",ex);
        }
    }


}
