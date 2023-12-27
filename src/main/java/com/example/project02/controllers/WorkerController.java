package com.example.project02.controllers;

import com.example.project02.models.Account;
import com.example.project02.models.Work;
import com.example.project02.models.Worksheet;
import com.example.project02.services.PageNavigator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class WorkerController {
    @FXML
    private Label loginNameLabel;
    @FXML
    private Label detailsLabel;
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
    private List<Worksheet> worksheetList = new ArrayList<>();
    private List<Work> workList = new ArrayList<>();
    String url = "jdbc:postgresql://db.wxxhmqjeruggsslfbkhs.supabase.co/postgres";
    String user = "postgres"; // Replace with your DB username
    String password = "8hlUWjTUakLNou2C"; // Replace with your DB password

    public void initialize() {
        detailsLabel.setText("-");
        loadDataFromLoggingInCSV();
        loginNameLabel.setText(loginAccount.getName());
        loadDataFromWorksheetsDatabase();
        loadDataFromWorksDatabase();
        ObservableList<Work> observableWorks = FXCollections.observableArrayList(workList);
        workTable.setItems(observableWorks);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        statusColumn.setCellValueFactory(data -> {
            String workID = data.getValue().getId();
            // Assuming you have a method to find the name associated with the username
            String worksheetStatus = findStatusOfWorksheet(workID);
            return new SimpleStringProperty(worksheetStatus);
        });
        ObservableList<Work> filteredWorks = getFilteredWorks();
        workTable.setItems(filteredWorks);
        workTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Update the detailsLabel with the details of the selected plan
                detailsLabel.setText(newSelection.getDetails());
            } else {
                detailsLabel.setText("-"); // Clear the label if no plan is selected
            }
        });
    }

    private ObservableList<Work> getFilteredWorks() {
        List<Work> filteredList = workList.stream()
                .filter(work -> !findStatusOfWorksheet(work.getId()).equals("Not Found"))
                .collect(Collectors.toList());
        return FXCollections.observableArrayList(filteredList);
    }

    public String findStatusOfWorksheet(String workID) {
        for (Worksheet worksheet : worksheetList) {
            if (Objects.equals(worksheet.getWorkID(), workID) && Objects.equals(worksheet.getUsername(), loginAccount.getUsername())) {
                return worksheet.getStatus();
            }
        }
        return "Not Found"; // Handle the case where the username is not found
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

    public void loadDataFromWorksDatabase() {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM works")) {

            while (resultSet.next()) {
                String id = resultSet.getString("work_id");
                String name = resultSet.getString("work_name");
                String startDate = resultSet.getString("work_start_date");
                String endDate = resultSet.getString("work_end_date");
                String planID = resultSet.getString("plan_id");
                String details = resultSet.getString("work_description");
                String status = resultSet.getString("work_status");

                Work work = new Work(id, name, startDate, endDate, planID, details, status);
                workList.add(work);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadDataFromWorksheetsDatabase() {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM worksheets " +
                     "WHERE worker_username = '" + loginAccount.getUsername() + "' " +
                     "AND worksheet_status != 'เสร็จสิ้น'")) {

            while (resultSet.next()) {
                String worksheetID = resultSet.getString("worksheet_id");
                String workID = resultSet.getString("work_id");
                String username = resultSet.getString("worker_username");
                String details = resultSet.getString("worksheet_description");
                String photo = resultSet.getString("worksheet_photo");
                String status = resultSet.getString("worksheet_status");

                Worksheet worksheet = new Worksheet(worksheetID, workID, username, details, photo, status);
                worksheetList.add(worksheet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public void handleWorkDoubleClickAction(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) { // Check for a double click
            Work selectedWork = workTable.getSelectionModel().getSelectedItem();
            if (selectedWork != null) {
                saveDataToSelectedWorkCSV(selectedWork);
                PageNavigator.navigateToPage("/com/example/project02/views/workerWorksheet.fxml");
            }
        }
    }


    public void handleLogoutButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/home.fxml");
    }

    public void handleEquipButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/workerEquip.fxml");
    }
}
