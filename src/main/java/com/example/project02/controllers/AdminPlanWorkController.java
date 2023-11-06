package com.example.project02.controllers;

import com.example.project02.models.Account;
import com.example.project02.models.Plan;
import com.example.project02.models.Work;
import com.example.project02.services.PageNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminPlanWorkController {
    @FXML
    private Label loginNameLabel;
    @FXML
    private Label planLabel;
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

    public void initialize() {
        loadDataFromLoggingInCSV();
        loginNameLabel.setText(loginAccount.getName());
        loadDataFromSelectedPlanCSV();
        planLabel.setText("PlanID " + plan.getId() + " : " + plan.getName());
        loadDataFromWorksCSV();
        loadDataFromWorksCSVAll();
        ObservableList<Work> observableWorks = FXCollections.observableArrayList(workList);
        workTable.setItems(observableWorks);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
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

    public void loadDataFromWorksCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("csv/works.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 7) { // Assuming there are 6 fields in the CSV
                    String id = parts[0];
                    String name = parts[1];
                    String startDate = parts[2];
                    String endDate = parts[3];
                    String planID = parts[4];
                    String details = parts[5];
                    String status = parts[6];


                    if (Objects.equals(planID, plan.getId())) {
                        Work work = new Work(id,name,startDate,endDate,planID,details,status);
                        workList.add(work);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDataFromWorksCSVAll() {
        try (BufferedReader reader = new BufferedReader(new FileReader("csv/works.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 7) { // Assuming there are 6 fields in the CSV
                    String id = parts[0];
                    String name = parts[1];
                    String startDate = parts[2];
                    String endDate = parts[3];
                    String planID = parts[4];
                    String details = parts[5];
                    String status = parts[6];

                    Work work = new Work(id,name,startDate,endDate,planID,details,status);
                    allWorkList.add(work);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDataToWorksCSV(List<Work> workList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("csv/works.csv"))) {
            for (Work work : workList) {
                writer.write(work.getId() + "," +
                        work.getName() + "," +
                        work.getStartDate() + "," +
                        work.getEndDate() + "," +
                        work.getPlanID() + "," +
                        work.getDetails() + "," +
                        work.getStatus());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        // Get the selected plan and new status from the ChoiceBox
        Work selectedWork = workTable.getSelectionModel().getSelectedItem(); // Assuming planTable is your TableView
        String newStatus = statusChoiceBox.getValue();

        if (selectedWork != null && newStatus != null) {
            // Change the status of the selected plan
            selectedWork.setStatus(newStatus);
            workTable.refresh();
            saveDataToWorksCSV(allWorkList);
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
