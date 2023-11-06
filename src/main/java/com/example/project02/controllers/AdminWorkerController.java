package com.example.project02.controllers;

import com.example.project02.models.Account;
import com.example.project02.services.PageNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AdminWorkerController {
    @FXML
    private Label loginNameLabel;
    @FXML
    private ListView<String> workersListView;
    @FXML
    private Label workerCountLabel;
    private Account loginAccount;
    private List<String> workerList = new ArrayList<>();

    public void initialize() {
        loadDataFromLoggingInCSV();
        loginNameLabel.setText(loginAccount.getName());
        loadDataFromAccountsCSV();
        int workerCount = workerList.size();
        workerCountLabel.setText(workerCount + " คน");
        ObservableList<String> observableNames = FXCollections.observableArrayList(workerList);
        workersListView.setItems(observableNames);
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

    public void loadDataFromAccountsCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("csv/accounts.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) { // Assuming there are 4 fields in the CSV
                    String name = parts[1];
                    boolean isAdmin = Boolean.parseBoolean(parts[3]);
                    if (!isAdmin) workerList.add(name);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleSignUpButtonAction() { PageNavigator.navigateToPage("/com/example/project02/views/adminWorkerSignUp.fxml"); }

    public void handleLogoutButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/home.fxml");
    }

    public void handleBackButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/admin.fxml");
    }

}
