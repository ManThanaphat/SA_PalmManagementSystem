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

    public void initialize() {
        loadDataFromLoggingInCSV();
        loadDataFromSelectedEquipmentCSV();
        loadDataFromBorrowFormCSV();
        loadDataFromAccountsCSV();
        loginNameLabel.setText(loginAccount.getName());
        equipmentLabel.setText(selectedEquipment.getName());
        ObservableList<BorrowForm> observableBorrowForms = FXCollections.observableArrayList(borrowFormList);
        borrowFormTable.setItems(observableBorrowForms);
        borrowTimeColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        // Update the borrowerColumn to display the name of the borrower
        borrowerColumn.setCellValueFactory(data -> {
            String borrowerUsername = data.getValue().getBorrower();
            // Assuming you have a method to find the name associated with the username
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


    public void loadDataFromAccountsCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("csv/accounts.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) { // Assuming there are 4 fields in the CSV
                    String username = parts[0];
                    String name = parts[1];
                    String password = parts[2];
                    boolean isAdmin = Boolean.parseBoolean(parts[3]);
                    Account account = new Account(username, name, password, isAdmin);
                    accountList.add(account);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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

    public void loadDataFromBorrowFormCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("csv/borrow_form.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 7) { // Assuming there are 6 fields in the CSV
                    String id = parts[0];
                    String borrower = parts[1];
                    String equipmentName = parts[2];
                    String borrowDate = parts[3]; // Assuming it's a String, not an int
                    int borrowNumber = Integer.parseInt(parts[4]);
                    int returnedNumber = Integer.parseInt(parts[5]);
                    String status = parts[6];
                    if (Objects.equals(equipmentName, selectedEquipment.getName())) {
                        BorrowForm borrowForm = new BorrowForm(id, borrower, equipmentName, borrowDate, borrowNumber, returnedNumber, status);
                        borrowFormList.add(borrowForm);
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleLogoutButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/home.fxml");
    }

    public void handleBackButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/adminEquip.fxml");
    }

}
