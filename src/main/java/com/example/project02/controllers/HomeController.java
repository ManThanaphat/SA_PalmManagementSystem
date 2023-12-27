package com.example.project02.controllers;

import com.example.project02.models.Account;
import com.example.project02.services.PageNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.io.*;
import java.sql.*;

public class HomeController {
    

    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label checkedLabel;

    public void initialize() throws SQLException {
        // Hide the label when the program starts
        checkedLabel.setVisible(false);
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

        String url = "jdbc:postgresql://db.wxxhmqjeruggsslfbkhs.supabase.co/postgres";
        String user = "postgres"; // Replace with your DB username
        String password = "8hlUWjTUakLNou2C"; // Replace with your DB password
        String sql = "SELECT * FROM accounts WHERE username = ? AND password = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, inputUsername);
            pst.setString(2, inputPassword);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                isCorrect = true;

                correctAccount = new Account(
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getBoolean("is_admin")
                );
                System.out.println(correctAccount.toString());
            } else {
                // Handle the case where no matching account was found
                System.out.println("No matching account found.");
            }

        } catch (SQLException e) {
            e.printStackTrace(); // In production, consider a more robust error handling mechanism
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


