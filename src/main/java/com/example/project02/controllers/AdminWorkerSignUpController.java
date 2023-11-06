package com.example.project02.controllers;

import com.example.project02.models.Account;
import com.example.project02.models.Worksheet;
import com.example.project02.services.PageNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminWorkerSignUpController {

    @FXML
    public TextField nameField;
    @FXML
    public Label checkedLabel;
    @FXML
    public TextField usernameField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public PasswordField confirmPasswordField;
    @FXML
    private Label loginNameLabel;
    private Account loginAccount;
    private List<Account> accountList = new ArrayList<>();

    public void initialize() {
        loadDataFromLoggingInCSV();
        loginNameLabel.setText(loginAccount.getName());
        loadDataFromAccountsCSV();
        checkedLabel.setText("");
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
                    String username = parts[0];
                    String name = parts[1];
                    String password = parts[2];
                    boolean isAdmin = Boolean.parseBoolean(parts[3]);
                    Account account = new Account(username,name,password,isAdmin);
                    accountList.add(account);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDataToAccountCSV(List<Account> accountList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("csv/accounts.csv"))) {
            for (Account account : accountList) {
                writer.write(account.getUsername() + "," +
                        account.getName() + "," +
                        account.getPassword() + "," +
                        account.isAdmin());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleConfirmButtonAction() {
        String username = usernameField.getText();
        String name = nameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        if (!Objects.equals(username, "") && !Objects.equals(name, "") && !Objects.equals(password, "") && !Objects.equals(confirmPassword, "")) {
            boolean equal = false;
            for (Account account : accountList) {
                if (Objects.equals(username, account.getUsername())) {
                    equal = true;
                    break;
                }
            }
            if (!equal) {
                if (password.length() >= 8) {
                    if (password.equals(confirmPassword)) {
                        Account newAccount = new Account(username, name, password, false);
                        accountList.add(newAccount);
                        saveDataToAccountCSV(accountList);
                        checkedLabel.setText("ลงทะเบียนคนงานสำเร็จ");
                        checkedLabel.setTextFill(Color.GREEN);
                        usernameField.clear();
                        nameField.clear();
                        passwordField.clear();
                        confirmPasswordField.clear();
                    } else {
                        checkedLabel.setText("ยืนยันรหัสผ่านไม่ตรงกับรหัสผ่าน");
                        checkedLabel.setTextFill(Color.RED);
                    }
                } else {
                    checkedLabel.setText("รหัสผ่านต้องมีอย่างน้อย 8 ตัว");
                    checkedLabel.setTextFill(Color.RED);
                }
            } else {
                checkedLabel.setText("ชื่อบัญชีผู้ใช้งานซ้ำกับบัญชีอีน");
                checkedLabel.setTextFill(Color.RED);
            }
        } else {
            checkedLabel.setText("กรุณากรอกข้อมูลให้ครบทุกช่อง");
            checkedLabel.setTextFill(Color.RED);
        }
    }

    public void handleLogoutButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/home.fxml");
    }

    public void handleBackButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/adminWorker.fxml");
    }

}
