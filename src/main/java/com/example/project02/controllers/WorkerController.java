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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public void initialize() {
        detailsLabel.setText("-");
        loadDataFromLoggingInCSV();
        loginNameLabel.setText(loginAccount.getName());
        loadDataFromWorksheetsCSV();
        loadDataFromWorksCSV();
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
        workTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Update the detailsLabel with the details of the selected plan
                detailsLabel.setText(newSelection.getDetails());
            } else {
                detailsLabel.setText("-"); // Clear the label if no plan is selected
            }
        });
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

                    for (Worksheet worksheet : worksheetList) {
                        if (Objects.equals(worksheet.getWorkID(), id) && !Objects.equals(worksheet.getStatus(), "เสร็จสิ้น")) {
                            Work work = new Work(id, name, startDate, endDate, planID, details, status);
                            workList.add(work);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDataFromWorksheetsCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("csv/worksheets.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) { // Assuming there are 4 fields in the CSV
                    String worksheetID = parts[0];
                    String workID = parts[1];
                    String username = parts[2];
                    String details = parts[3];
                    String photo = parts[4];
                    String status = parts[5];
                    if (Objects.equals(username, loginAccount.getUsername())) {
                        Worksheet worksheet = new Worksheet(worksheetID, workID, username, details, photo, status);
                        worksheetList.add(worksheet);
                    }
                }
            }
        } catch (IOException e) {
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
