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
import java.sql.*;
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
    String url = "jdbc:postgresql://db.wxxhmqjeruggsslfbkhs.supabase.co/postgres";
    String user = "postgres"; // Replace with your DB username
    String password = "8hlUWjTUakLNou2C"; // Replace with your DB password

    public void initialize() {
        loadDataFromLoggingInCSV();
        loginNameLabel.setText(loginAccount.getName());

        String sqlCheckEqual = "SELECT * FROM accounts WHERE is_admin = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = conn.prepareStatement(sqlCheckEqual)) {

            pst.setBoolean(1, false);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    // Assuming your Account class has a constructor that takes these parameters
                    Account account = new Account(
                            rs.getString("username"),
                            rs.getString("name"),
                            rs.getString("password"),
                            rs.getBoolean("is_admin")
                    );
                    workerList.add(account.getName());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace(); // In production, consider a more robust error handling mechanism
        }

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

    public void handleSignUpButtonAction() { PageNavigator.navigateToPage("/com/example/project02/views/adminWorkerSignUp.fxml"); }

    public void handleLogoutButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/home.fxml");
    }

    public void handleBackButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/admin.fxml");
    }

}
