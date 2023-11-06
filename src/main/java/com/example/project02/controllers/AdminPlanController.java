package com.example.project02.controllers;

import com.example.project02.models.Account;
import com.example.project02.models.Plan;
import com.example.project02.services.PageNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AdminPlanController {
    @FXML
    private Label loginNameLabel;

    @FXML
    private TableView<Plan> planTable;
    @FXML
    private TableColumn<Plan, String> idColumn;
    @FXML
    private TableColumn<Plan, String> nameColumn;
    @FXML
    private TableColumn<Plan, String> startDateColumn;
    @FXML
    private TableColumn<Plan, String> endDateColumn;
    @FXML
    private TableColumn<Plan, String> statusColumn;
    @FXML
    private Label detailsLabel;
    @FXML
    private ChoiceBox<String> statusChoiceBox;
    private Account loginAccount;
    private List<Plan> planList = new ArrayList<>();

    public void initialize() {
        loadDataFromLoggingInCSV();
        loginNameLabel.setText(loginAccount.getName());

        detailsLabel.setText("-");

        loadDataFromPlansCSV();
        ObservableList<Plan> observablePlans = FXCollections.observableArrayList(planList);
        planTable.setItems(observablePlans);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        // Add a listener to the TableView to respond to row selection
        planTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Update the detailsLabel with the details of the selected plan
                detailsLabel.setText(newSelection.getDetails());
            } else {
                detailsLabel.setText("-"); // Clear the label if no plan is selected
            }
        });

        statusChoiceBox.getItems().addAll("ยังไม่เริ่มต้น", "กำลังทำ", "เสร็จสิ้น");
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

    public void saveDataToSelectedPlanCSV(Plan plan) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("csv/selected_plan.csv"))) {
            writer.write(plan.getId() + "," + plan.getName() + "," +
                    plan.getStartDate() + "," + plan.getEndDate() + "," +
                    plan.getDetails() + "," + plan.getStatus());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleChangeStatusButtonAction() {
        // Get the selected plan and new status from the ChoiceBox
        Plan selectedPlan = planTable.getSelectionModel().getSelectedItem(); // Assuming planTable is your TableView
        String newStatus = statusChoiceBox.getValue();

        if (selectedPlan != null && newStatus != null) {
            // Change the status of the selected plan
            selectedPlan.setStatus(newStatus);
            planTable.refresh();
            saveDataToPlansCSV(planList);
        }
    }

    public void handleLogoutButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/home.fxml");
    }

    public void handleBackButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/admin.fxml");
    }

    public void handleAddPlanButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/adminPlanAdd.fxml");
    }

    public void handleWorkDoubleClickAction(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) { // Check for a double click
            Plan selectedPlan = planTable.getSelectionModel().getSelectedItem();
            if (selectedPlan != null) {
                saveDataToSelectedPlanCSV(selectedPlan);
                PageNavigator.navigateToPage("/com/example/project02/views/adminPlanWork.fxml");
            }
        }
    }

}
