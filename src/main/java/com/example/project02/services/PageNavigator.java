package com.example.project02.services;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class PageNavigator {
    private static Stage stage;
    private static Object controller;
    private static Object passedData; // Add a field to store the data to be passed

    public PageNavigator(Stage stage) {
        PageNavigator.stage = stage;
    }

    public static void navigateToPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(PageNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();
            controller = loader.getController();
            // You may want to keep or clear passed data here based on your application logic

            Scene newScene = new Scene(root);
            stage.setScene(newScene);
        } catch (IOException e) {
            // Handle the error gracefully, such as showing an error message to the user
            e.printStackTrace(); // For debugging purposes
        }
    }

}
