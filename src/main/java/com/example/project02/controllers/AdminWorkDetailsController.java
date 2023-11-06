package com.example.project02.controllers;

import com.example.project02.models.Account;
import com.example.project02.models.Work;
import com.example.project02.models.Worksheet;
import com.example.project02.services.PageNavigator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AdminWorkDetailsController {
    @FXML
    private Label loginNameLabel;
    @FXML
    private Label workLabel;
    @FXML
    private Label detailsLabel;
    @FXML
    private TableView<Worksheet> workersTable;
    @FXML
    private TableColumn<Worksheet, String> nameColumn;
    @FXML
    private TableColumn<Worksheet, String> statusColumn;
    @FXML
    private ChoiceBox<String> workersChoiceBox;

    private Account loginAccount;
    private Work work;
    private List<Account> allWorkerList = new ArrayList<>();
    private List<String> workerList = new ArrayList<>();
    private List<Worksheet> worksheetList = new ArrayList<>();
    private List<Worksheet> allWorksheetList = new ArrayList<>();

    public void initialize() {
        loadDataFromLoggingInCSV();
        loadDataFromSelectedWorkCSV();
        loadDataFromWorksheetsCSV();
        loadDataFromAccountsCSV();
        loadDataFromWorksheetsCSVAll();
        loginNameLabel.setText(loginAccount.getName());
        workLabel.setText("work ID " + work.getId() + " : " + work.getName());
        detailsLabel.setText(work.getDetails());

        // Set up the cell value factories for the columns
        nameColumn.setCellValueFactory(cellData -> {
            String workerUsername = cellData.getValue().getUsername();
            Account worker = getWorkerByUsername(workerUsername);
            if (worker != null) {
                return new SimpleStringProperty(worker.getName());
            }
            return new SimpleStringProperty("");
        });

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Create a list of Worksheet instances for populating the table
        ObservableList<Worksheet> workerWorksheetList = worksheetList.stream()
                .filter(worksheet -> Objects.equals(worksheet.getWorkID(), work.getId()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        // Add the workerWorksheetList to the table
        workersTable.setItems(workerWorksheetList);


        // Populate the workersChoiceBox with workers not in the workerList
        ObservableList<String> workersNotInList = FXCollections.observableArrayList();

        for (Account worker : allWorkerList) {
            String workerName = worker.getName();
            if (!workerList.contains(workerName) && !isWorkerAssignedByName(workerName)) {
                workersNotInList.add(workerName);
            }
        }

        workersChoiceBox.setItems(workersNotInList);
    }

    private Account getWorkerByUsername(String username) {
        for (Account worker : allWorkerList) {
            if (Objects.equals(username, worker.getUsername())) {
                return worker;
            }
        }
        return null;
    }

    private Account getSelectedWorker() {
        // Get the selected item from the TableView
        Worksheet selectedWorksheet = workersTable.getSelectionModel().getSelectedItem();

        if (selectedWorksheet != null) {
            String username = selectedWorksheet.getUsername();
            // Find the worker with the corresponding username
            for (Account worker : allWorkerList) {
                if (worker.getUsername().equals(username)) {
                    return worker;
                }
            }
        }

        return null; // Return null if no worker is selected or found.
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
                    if (!isAdmin) {
                        Account worker = new Account(username,name,password,isAdmin);
                        allWorkerList.add(worker);
                        workersChoiceBox.getItems().add(worker.getName());
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
                    Worksheet worksheet = new Worksheet(worksheetID,workID,username,details,photo,status);
                    if (Objects.equals(workID, work.getId())) {
                        worksheetList.add(worksheet);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDataFromWorksheetsCSVAll() {
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
                    Worksheet worksheet = new Worksheet(worksheetID,workID,username,details,photo,status);
                    allWorksheetList.add(worksheet);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDataFromSelectedWorkCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("csv/selected_work.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 7) { // Assuming there are 7 fields in the CSV
                    String id = parts[0];
                    String name = parts[1];
                    String startDate = parts[2];
                    String endDate = parts[3];
                    String planID = parts[4];
                    String details = parts[5];
                    String status = parts[6];
                    work = new Work(id,name,startDate,endDate,planID,details,status);
                    }
            }
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

    public void saveDataToSelectedWorkerCSV(Account account) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("csv/selected_worker.csv"))) {
            writer.write(account.getUsername() + "," + account.getName() + "," +
                    account.getPassword() + "," + account.isAdmin());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDataToWorksheetsCSV(List<Worksheet> worksheetList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("csv/worksheets.csv"))) {
            for (Worksheet worksheet : worksheetList) {
                writer.write(worksheet.getWorksheetID() + "," +
                        worksheet.getWorkID() + "," +
                        worksheet.getUsername() + "," +
                        worksheet.getDetails() + "," +
                        worksheet.getPhoto() + "," +
                        worksheet.getStatus());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleLogoutButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/home.fxml");
    }

    public void handleBackButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/adminPlanWork.fxml");
    }

    public void handleAddButtonAction() {
        String workerName = workersChoiceBox.getValue();
        if (workerName != null && !isWorkerAssignedByName(workerName)) {
            // Generate a unique worksheet ID
            String worksheetID = generateUniqueWorksheetID();

            // Create a new worksheet
            Worksheet newWorksheet = new Worksheet(worksheetID, work.getId(), getWorkerUsernameByName(workerName), "-", "", "ยังไม่เสร็จ");

            // Add the newWorksheet to the TableView
            workersTable.getItems().add(newWorksheet);

            // Add the newWorksheet to the worksheetList
            worksheetList.add(newWorksheet);

            // Save the updated worksheetList to the worksheets.csv file
            saveDataToWorksheetsCSV(worksheetList);

            // Remove the selected worker from the workersChoiceBox
            workersChoiceBox.getItems().remove(workerName);
        }
    }

    // Helper method to check if a worker with the given name is already assigned to the current work
    private boolean isWorkerAssignedByName(String workerName) {
        String workerUsername = getWorkerUsernameByName(workerName);
        if (workerUsername != null) {
            for (Worksheet worksheet : worksheetList) {
                if (Objects.equals(worksheet.getWorkID(), work.getId()) && Objects.equals(worksheet.getUsername(), workerUsername)) {
                    return true;
                }
            }
        }
        return false;
    }



    // Helper method to get the worker's username by name
    private String getWorkerUsernameByName(String workerName) {
        for (Account worker : allWorkerList) {
            if (workerName.equals(worker.getName())) {
                return worker.getUsername();
            }
        }
        return ""; // Return an empty string if the worker's username is not found.
    }


    // Helper method to generate a unique worksheet ID
    private String generateUniqueWorksheetID() {
        int maxID = 0;

        // Find the maximum worksheet ID in the existing worksheets
        for (Worksheet worksheet : allWorksheetList) {
            String worksheetID = worksheet.getWorksheetID();
            int id = extractIDFromWorksheetID(worksheetID);
            if (id > maxID) {
                maxID = id;
            }
        }

        // Generate a new unique ID
        int newID = maxID + 1;

        // Construct the worksheet ID
        return work.getId() + "WS" + newID;
    }

    // Helper method to extract the numeric part of a worksheet ID
    private int extractIDFromWorksheetID(String worksheetID) {
        String[] parts = worksheetID.split("WS");
        if (parts.length == 2) {
            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                // Handle the exception appropriately, e.g., log an error.
            }
        }
        return 0; // Default to 0 if unable to extract a valid ID.
    }


    public void handleEraseButtonAction() {
        Worksheet selectedWorksheet = workersTable.getSelectionModel().getSelectedItem();
        if (selectedWorksheet != null) {
            // Remove the selected worksheet from the TableView
            workersTable.getItems().remove(selectedWorksheet);

            // Remove the selected worksheet from the worksheetList
            worksheetList.remove(selectedWorksheet);

            // Save the updated worksheetList to the worksheets.csv file
            saveDataToWorksheetsCSV(worksheetList);

            // Add the worker's name back to the workersChoiceBox
            String workerName = getWorkerNameByUsername(selectedWorksheet.getUsername());
            if (workerName != null) {
                workersChoiceBox.getItems().add(workerName);
            }
        }
    }



    // Helper method to get the worker's name by username
    private String getWorkerNameByUsername(String username) {
        for (Account worker : allWorkerList) {
            if (username.equals(worker.getUsername())) {
                return worker.getName();
            }
        }
        return null; // Return null if the worker's name is not found.
    }

    public void handleWorkerDoubleClickAction(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) { // Check for a double click
            Account selectedWorker = getSelectedWorker();
            if (selectedWorker != null) {
                saveDataToSelectedWorkerCSV(selectedWorker);
                // Navigate to the appropriate page based on your application's logic
                PageNavigator.navigateToPage("/com/example/project02/views/adminWorksheet.fxml");
            }
        }
    }




}
