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
import java.sql.*;
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
    String url = "jdbc:postgresql://db.wxxhmqjeruggsslfbkhs.supabase.co/postgres";
    String user = "postgres"; // Replace with your DB username
    String password = "8hlUWjTUakLNou2C"; // Replace with your DB password

    public void initialize() {
        loadDataFromLoggingInCSV();
        loginNameLabel.setText(loginAccount.getName());
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

    public void handleConfirmButtonAction() {
        String inputUsername = usernameField.getText();
        String inputName = nameField.getText();
        String inputPassword = passwordField.getText();
        String inputConfirmPassword = confirmPasswordField.getText();

        if (!Objects.equals(inputUsername, "") && !Objects.equals(inputName, "") && !Objects.equals(inputPassword, "") && !Objects.equals(inputConfirmPassword, "")) {

            boolean isEqual = false;

                    String sqlCheckEqual = "SELECT * FROM accounts WHERE username = ?";

                    try (Connection conn = DriverManager.getConnection(url, user, password);
                         PreparedStatement pst = conn.prepareStatement(sqlCheckEqual)) {

                        pst.setString(1, inputUsername);

                        ResultSet rs = pst.executeQuery();

                        if (rs.next()) {
                            isEqual = true;
                        }

                    } catch (SQLException e) {
                        e.printStackTrace(); // In production, consider a more robust error handling mechanism
                    }

                    if (!isEqual) {
                        if (inputPassword.length() >= 8) {
                            if (inputPassword.equals(inputConfirmPassword)) {
                        Account newAccount = new Account(inputUsername, inputName, inputPassword, false);
                        // accountList.add(newAccount);
                        // saveDataToAccountCSV(accountList);

                        // insert account into database
                        String sqlInsert = "INSERT INTO accounts (username, name, password, is_admin) VALUES (?, ?, ?, ?)";

                        try (Connection conn = DriverManager.getConnection(url, user, password);
                             PreparedStatement pst = conn.prepareStatement(sqlInsert)) {

                            pst.setString(1, newAccount.getUsername());  // Set username
                            pst.setString(2, newAccount.getName());      // Set name
                            pst.setString(3, newAccount.getPassword());  // Set password
                            pst.setBoolean(4, newAccount.isAdmin());

                            int rowsAffected = pst.executeUpdate(); // Execute the INSERT statement

                            if (rowsAffected > 0) {
                                // Insertion successful
                                System.out.println("New account inserted successfully.");
                            } else {
                                // Insertion failed
                                System.out.println("Failed to insert the new account.");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace(); // Handle any potential database errors here
                        }

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
