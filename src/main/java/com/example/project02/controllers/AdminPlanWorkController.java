package com.example.project02.controllers;

import com.example.project02.models.Account;
import com.example.project02.models.Plan;
import com.example.project02.models.Work;
import com.example.project02.services.PageNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminPlanWorkController {
    @FXML
    private Label loginNameLabel;
    @FXML
    private Label planLabel;
    @FXML
    private Button assignButton;
    @FXML
    private ChoiceBox<String> statusChoiceBox;
    @FXML
    private TableView<Work> workTable;
    @FXML
    private TableColumn<Work, String> idColumn;
    @FXML
    private TableColumn<Work, String> nameColumn;
    @FXML
    private TableColumn<Work, String> startDateColumn;
    @FXML
    private TableColumn<Work, String> endDateColumn;
    @FXML
    private TableColumn<Work, String> statusColumn;
    private Account loginAccount;
    private Plan plan;
    private List<Work> workList = new ArrayList<>();
    private List<Work> allWorkList = new ArrayList<>();
    String url = "jdbc:postgresql://db.wxxhmqjeruggsslfbkhs.supabase.co/postgres";
    String user = "postgres"; // Replace with your DB username
    String password = "8hlUWjTUakLNou2C"; // Replace with your DB password

    public void initialize() {
        loadDataFromLoggingInCSV();
        loginNameLabel.setText(loginAccount.getName());
        loadDataFromSelectedPlanCSV();
        planLabel.setText("PlanID " + plan.getId() + " : " + plan.getName());
        loadDataFromWorksDatabase();
        loadDataFromWorksDatabaseAll();
        ObservableList<Work> observableWorks = FXCollections.observableArrayList(workList);
        workTable.setItems(observableWorks);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusChoiceBox.getItems().addAll("ยังไม่เริ่มต้น", "กำลังทำ", "เสร็จสิ้น");

        // Get today's date
        LocalDate today = LocalDate.now();
        LocalDate planEndDate = LocalDate.parse(plan.getEndDate()); // Assuming endDate is in YYYY-MM-DD format

        // Compare today's date with the plan's end date
        if (planEndDate.isBefore(today)) {
            // If the plan's end date is before today, hide the assign button
            assignButton.setVisible(false); // Replace 'assignButton' with your actual button name
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

    public void loadDataFromSelectedPlanCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("csv/selected_plan.csv"))) {
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

                    plan = new Plan(id, name, startDate, endDate, details, status);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDataFromWorksDatabase() {
        String sqlSelect = "SELECT * FROM works WHERE plan_id = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = conn.prepareStatement(sqlSelect)) {

            pst.setString(1, plan.getId());

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("work_id");
                    String name = rs.getString("work_name");
                    String startDate = rs.getString("work_start_date");
                    String endDate = rs.getString("work_end_date");
                    String planID = rs.getString("plan_id");
                    String details = rs.getString("work_description");
                    String status = rs.getString("work_status");

                    Work work = new Work(id, name, startDate, endDate, planID, details, status);
                    workList.add(work);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle or log the exception as needed
        }
    }


    public void loadDataFromWorksDatabaseAll() {
        String sqlSelect = "SELECT * FROM works";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = conn.prepareStatement(sqlSelect);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("work_id");
                String name = rs.getString("work_name");
                String startDate = rs.getString("work_start_date");
                String endDate = rs.getString("work_end_date");
                String planID = rs.getString("plan_id");
                String details = rs.getString("work_description");
                String status = rs.getString("work_status");

                Work work = new Work(id, name, startDate, endDate, planID, details, status);
                allWorkList.add(work);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle or log the exception as needed
        }
    }

    public void saveDataToSelectedWorkCSV(Work work) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("csv/selected_work.csv"))) {
            writer.write(work.getId() + "," +
                    work.getName() + "," +
                    work.getStartDate() + "," +
                    work.getEndDate() + "," +
                    work.getPlanID() + "," +
                    work.getDetails() + "," +
                    work.getStatus());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleChangeStatusButtonAction() {
        // Get the selected work and new status from the ChoiceBox
        Work selectedWork = workTable.getSelectionModel().getSelectedItem();
        String newStatus = statusChoiceBox.getValue();

        if (selectedWork != null && newStatus != null) {
            // Change the status of the selected work
            selectedWork.setStatus(newStatus);
            updateWorkStatusInDatabase(selectedWork);

            // Refresh the workTable
            workTable.refresh();
        }
    }

    private void updateWorkStatusInDatabase(Work work) {
        String sqlUpdate = "UPDATE works SET work_status = ? WHERE work_id = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = conn.prepareStatement(sqlUpdate)) {

            pst.setString(1, work.getStatus());
            pst.setString(2, work.getId());

            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle or log the exception as needed
        }
    }

    public void handleWorkDoubleClickAction(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) { // Check for a double click
            Work selectedWork = workTable.getSelectionModel().getSelectedItem();
            if (selectedWork != null) {
                saveDataToSelectedWorkCSV(selectedWork);
                PageNavigator.navigateToPage("/com/example/project02/views/adminWorkDetails.fxml");
            }
        }
    }
    public void handleAssignButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/adminPlanAssign.fxml");
    }

    public void handleLogoutButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/home.fxml");
    }

    public void handleBackButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/adminPlan.fxml");
    }

}
