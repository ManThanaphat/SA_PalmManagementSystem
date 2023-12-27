package com.example.project02.controllers;

import com.example.project02.models.Account;
import com.example.project02.models.Plan;
import com.example.project02.services.PageNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminPlanAddController {
    @FXML
    private Label loginNameLabel;
    @FXML
    private TextField nameField;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TextArea detailsArea;
    @FXML
    private Label addedLabel;
    private Account loginAccount;
    private List<Plan> planList = new ArrayList<>();
    String url = "jdbc:postgresql://db.wxxhmqjeruggsslfbkhs.supabase.co/postgres";
    String user = "postgres"; // Replace with your DB username
    String password = "8hlUWjTUakLNou2C"; // Replace with your DB password

    public void initialize() {
        loadDataFromLoggingInCSV();
        loginNameLabel.setText(loginAccount.getName());
        addedLabel.setText("");
        loadDataFromPlansDatabase();
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

    public void loadDataFromPlansDatabase() {
        String sqlSelect = "SELECT * FROM plans";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = conn.prepareStatement(sqlSelect);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("plan_id");
                String name = rs.getString("plan_name");
                String startDate = rs.getString("plan_start_date");
                String endDate = rs.getString("plan_end_date");
                String details = rs.getString("plan_description");
                String status = rs.getString("plan_status");

                Plan plan = new Plan(id, name, startDate, endDate, details, status);
                planList.add(plan);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle or log the exception as needed
        }
    }

    public void saveDataToPlansDatabase(Plan newPlan) {
        String sqlInsert = "INSERT INTO plans (plan_id, plan_name, plan_start_date, plan_end_date, plan_description, plan_status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = conn.prepareStatement(sqlInsert)) {

            pst.setString(1, newPlan.getId());
            pst.setString(2, newPlan.getName());

            // Use setDate for the date columns
            pst.setDate(3, java.sql.Date.valueOf(newPlan.getStartDate()));
            pst.setDate(4, java.sql.Date.valueOf(newPlan.getEndDate()));

            pst.setString(5, newPlan.getDetails());
            pst.setString(6, newPlan.getStatus());

            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle or log the exception as needed
        }
    }

    public void handleAddButtonAction() {
        String name = nameField.getText();
        String startDateText = String.valueOf(startDatePicker.getValue());
        String endDateText = String.valueOf(endDatePicker.getValue());
        String details = detailsArea.getText();

        String errorMessage = "";

        if (Objects.equals(name, "") || Objects.equals(details, "")) {
            errorMessage = "กรอกข้อมูลให้ครบถ้วน";
        } else if (Objects.equals(startDateText, "") || Objects.equals(endDateText, "")) {
            errorMessage = "กรุณาเลือกวันที่เริ่มและสิ้นสุด";
        } else {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            LocalDate today = LocalDate.now(); // Get today's date

            if (startDate.isBefore(today)) {
                errorMessage = "วันที่เริ่มต้องไม่น้อยกว่าวันที่ปัจจุบัน";
            } else if (startDate.isAfter(endDate)) {
                errorMessage = "วันที่เริ่มต้นต้องอยู่ก่อนวันที่สิ้นสุด";
            }
        }

        if (errorMessage.isEmpty()) {
            int nextId = planList.size() + 1; // Generate ID based on the size of the planList
            Plan newPlan = new Plan(String.valueOf(nextId), name, startDateText, endDateText, details, "ยังไม่เริ่มต้น");
            planList.add(newPlan);
            saveDataToPlansDatabase(newPlan);
            addedLabel.setText("เพิ่มแผนงานสำเร็จ");
            addedLabel.setTextFill(Color.GREEN);
            nameField.clear();
            detailsArea.clear();
        } else {
            addedLabel.setText(errorMessage);
            addedLabel.setTextFill(Color.RED);
        }
    }



    public void handleLogoutButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/home.fxml");
    }

    public void handleBackButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/adminPlan.fxml");
    }
}
