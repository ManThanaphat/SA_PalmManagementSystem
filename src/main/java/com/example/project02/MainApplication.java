package com.example.project02;

import com.example.project02.services.PageNavigator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        PageNavigator pageNavigator = new PageNavigator(stage); // Pass the stage reference

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/home.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Palm garden management system");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
