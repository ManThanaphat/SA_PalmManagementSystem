package com.example.project02.controllers;

import com.example.project02.MainApplication;
import com.example.project02.models.Account;
import com.example.project02.services.PageNavigator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

public class HomeController {

    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label checkedLabel;
    private List<Account> accountList = new ArrayList<>();

    public void initialize() {
        // Hide the label when the program starts
        checkedLabel.setVisible(false);

        loadDataFromAccountsCSV();
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

    public void saveDataToLoggingInCSV(Account account) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("csv/logging_in.csv"))) {
            writer.write(account.getUsername() + "," + account.getName() + "," +
                        account.getPassword() + "," + account.isAdmin());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleLoginButtonAction() {
        String inputUsername = usernameTextField.getText();
        String inputPassword = passwordField.getText();
        boolean isCorrect = false;
        Account correctAccount = null;

        for (Account acc : accountList) {
            if (Objects.equals(inputUsername, acc.getUsername()) && Objects.equals(inputPassword, acc.getPassword())){
                isCorrect = !isCorrect;
                correctAccount = acc;
            }
        }

        if (isCorrect){
            checkedLabel.setText("Login Complete.");
            checkedLabel.setTextFill(Color.GREEN);
            saveDataToLoggingInCSV(correctAccount);
            if (correctAccount.isAdmin()) {
                PageNavigator.navigateToPage("/com/example/project02/views/admin.fxml");
            } else {
                PageNavigator.navigateToPage("/com/example/project02/views/worker.fxml");
            }
        } else {
            checkedLabel.setText("ชื่อบัญชีผู้ใช้งาน หรือ รหัสผ่านไม่ถูกต้อง");
            checkedLabel.setTextFill(Color.RED);
            passwordField.clear();
        }
        checkedLabel.setVisible(true);
    }

}


