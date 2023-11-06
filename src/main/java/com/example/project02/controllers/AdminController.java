package com.example.project02.controllers;

import com.example.project02.models.Account;
import com.example.project02.services.PageNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class AdminController {
    @FXML
    private Label loginNameLabel;
    private Account loginAccount;

    public void initialize() {
        loadDataFromLoggingInCSV();
        loginNameLabel.setText(loginAccount.getName());
    }

    public void loadDataFromLoggingInCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("csv/logging_in.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) { // Assuming there are 4 fields in the CSV
                    String username = parts[0];
                    String name = parts[1];
                    String password = parts[2];
                    boolean isAdmin = Boolean.parseBoolean(parts[3]);
                    loginAccount = new Account(username, name, password, isAdmin);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleLogoutButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/home.fxml");
    }

    public void handleEquipButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/adminEquip.fxml");
    }

    public void handlePlanButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/adminPlan.fxml");
    }

    public void handleWorkerButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/adminWorker.fxml");
    }

}
