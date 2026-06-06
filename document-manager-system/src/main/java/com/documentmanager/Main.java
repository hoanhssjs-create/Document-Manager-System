package com.documentmanager;

import com.documentmanager.controller.AppController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        AppController controller = new AppController(stage);
        Scene scene = new Scene(controller.createLoginView(), 1180, 760);
        scene.getStylesheets().add(Main.class.getResource("/css/app.css").toExternalForm());
        stage.setTitle("Document Manager System");
        stage.setMinWidth(980);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
