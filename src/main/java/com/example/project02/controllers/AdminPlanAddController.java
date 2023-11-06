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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminPlanAddController {
    @FXML
    private Label loginNameLabel;
    @FXML
    private TextField idField;
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

    public void initialize() {
        loadDataFromLoggingInCSV();
        loginNameLabel.setText(loginAccount.getName());
        addedLabel.setText("");
        loadDataFromPlansCSV();
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

    public void loadDataFromPlansCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("csv/plans.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) { // Assuming there are 6 fields in the CSV
                    String id = parts[0];
                    String name = parts[1];
                    String startDate = parts[2];
                    String endDate = parts[3];
                    String details = parts[4];
                    String status = parts[5];

                    Plan plan = new Plan(id, name, startDate, endDate, details, status);
                    planList.add(plan);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDataToPlansCSV(List<Plan> planList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("csv/plans.csv"))) {
            for (Plan plan : planList) {
                writer.write(plan.getId() + "," +
                        plan.getName() + "," +
                        plan.getStartDate() + "," +
                        plan.getEndDate() + "," +
                        plan.getDetails() + "," +
                        plan.getStatus());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void handleAddButtonAction() {
        String id = idField.getText();
        String name = nameField.getText();
        String startDateText = startDatePicker.getEditor().getText();
        String endDateText = endDatePicker.getEditor().getText();
        String details = detailsArea.getText();

        String errorMessage = "";

        if (Objects.equals(id, "") || Objects.equals(name, "") || Objects.equals(details, "")) {
            errorMessage = "กรอกข้อมูลให้ครบถ้วน";
        } else if (Objects.equals(startDateText, "") || Objects.equals(endDateText, "")) {
            errorMessage = "กรุณาเลือกวันที่เริ่มและสิ้นสุด";
        } else {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (startDate.isAfter(endDate)) {
                errorMessage = "วันที่เริ่มต้นต้องอยู่ก่อนวันที่สิ้นสุด";
            } else {
                for (Plan existingPlan : planList) {
                    if (existingPlan.getId().equals(id)) {
                        errorMessage = "กรุณากรอก ID ใหม่ เนื่องจากซ้ำกับแผนงานอื่น";
                        break;
                    }
                }
            }
        }

        if (errorMessage.isEmpty()) {
            Plan newPlan = new Plan(id, name, startDateText, endDateText, details, "ยังไม่เริ่มต้น");
            planList.add(newPlan);
            saveDataToPlansCSV(planList);
            addedLabel.setText("เพิ่มแผนงานสำเร็จ");
            addedLabel.setTextFill(Color.GREEN);
            idField.clear();
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
