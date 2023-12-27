package com.example.project02.controllers;

import com.example.project02.models.Account;
import com.example.project02.models.BorrowForm;
import com.example.project02.models.Equipment;
import com.example.project02.services.PageNavigator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminEquipDetailsController {
    @FXML
    private Label loginNameLabel;
    @FXML
    private Label equipmentLabel;
    @FXML
    private TableView<BorrowForm> borrowFormTable;
    @FXML
    private TableColumn<BorrowForm, String> borrowTimeColumn;
    @FXML
    private TableColumn<BorrowForm, String> borrowerColumn;
    @FXML
    private TableColumn<BorrowForm, Integer> borrowNumberColumn;
    @FXML
    private TableColumn<BorrowForm, Integer> returnedNumberColumn;
    @FXML
    private TableColumn<BorrowForm, String> statusColumn;

    private Account loginAccount;
    private Equipment selectedEquipment;
    private List<BorrowForm> borrowFormList = new ArrayList<>();
    private List<Account> accountList = new ArrayList<>();
    String url = "jdbc:postgresql://db.wxxhmqjeruggsslfbkhs.supabase.co/postgres";
    String user = "postgres"; // Replace with your DB username
    String password = "8hlUWjTUakLNou2C"; // Replace with your DB password

    public void initialize() {
        loadDataFromLoggingInCSV();
        loadDataFromSelectedEquipmentCSV();

        loadDataFromBorrowFormDatabase();
        loadDataFromAccountsDatabase();
        loginNameLabel.setText(loginAccount.getName());
        equipmentLabel.setText(selectedEquipment.getName());
        ObservableList<BorrowForm> observableBorrowForms = FXCollections.observableArrayList(borrowFormList);
        borrowFormTable.setItems(observableBorrowForms);
        borrowTimeColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        // Update the borrowerColumn to display the name of the borrower
        borrowerColumn.setCellValueFactory(data -> {
            String borrowerUsername = data.getValue().getBorrower();
            String borrowerName = findNameForUsername(borrowerUsername);
            return new SimpleStringProperty(borrowerName);
        });

        borrowNumberColumn.setCellValueFactory(new PropertyValueFactory<>("borrowNumber"));
        returnedNumberColumn.setCellValueFactory(new PropertyValueFactory<>("returnedNumber"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }


    public String findNameForUsername(String username) {
        for (Account account : accountList) {
            if (account.getUsername().equals(username)) {
                return account.getName();
            }
        }
        return "Name Not Found"; // Handle the case where the username is not found
    }


    public void loadDataFromAccountsDatabase() {
        String sqlQuery = "SELECT * FROM accounts";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = conn.prepareStatement(sqlQuery);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                String name = rs.getString("name");
                String password = rs.getString("password");
                boolean isAdmin = rs.getBoolean("is_admin");
                Account account = new Account(username, name, password, isAdmin);
                accountList.add(account);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle or log the exception as needed
        }
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

    public void loadDataFromSelectedEquipmentCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("csv/selected_equipment.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) { // Assuming there are 6 fields in the CSV
                    String name = parts[0];
                    int number = Integer.parseInt(parts[1]);
                    int borrowing = Integer.parseInt(parts[2]);
                    selectedEquipment = new Equipment(name,number,borrowing);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDataFromBorrowFormDatabase() {
        String sqlQuery = "SELECT * FROM borrow_forms WHERE borrow_equipment = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = conn.prepareStatement(sqlQuery)) {

            pst.setString(1, selectedEquipment.getName()); // Set the equipment name parameter

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("bf_id");
                    String borrower = rs.getString("borrower");
                    String equipmentName = rs.getString("borrow_equipment");
                    String borrowDate = rs.getString("borrow_date"); // Assuming it's a String, not an int
                    int borrowNumber = rs.getInt("borrow_number");
                    int returnedNumber = rs.getInt("returned_number");
                    String status = rs.getString("bf_status");

                    BorrowForm borrowForm = new BorrowForm(id, borrower, equipmentName, borrowDate, borrowNumber, returnedNumber, status);
                    borrowFormList.add(borrowForm);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle or log the exception as needed
        }
    }

    public void handleLogoutButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/home.fxml");
    }

    public void handleBackButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/adminEquip.fxml");
    }

}
