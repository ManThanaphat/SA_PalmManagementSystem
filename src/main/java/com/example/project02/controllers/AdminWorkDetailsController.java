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
import java.sql.*;
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
    String url = "jdbc:postgresql://db.wxxhmqjeruggsslfbkhs.supabase.co/postgres";
    String user = "postgres"; // Replace with your DB username
    String password = "8hlUWjTUakLNou2C"; // Replace with your DB password

    public void initialize() {
        loadDataFromLoggingInCSV();
        loadDataFromSelectedWorkCSV();
        loadDataFromWorksheetsDatabase();
        loadDataFromAccountsDatabase();
        loadDataFromWorksheetsDatabaseAll();
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


    private void loadDataFromAccountsDatabase() {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM accounts WHERE is_admin = false")) {

            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String name = resultSet.getString("name");
                String password = resultSet.getString("password");
                boolean isAdmin = resultSet.getBoolean("is_admin");

                if (!isAdmin) {
                    Account worker = new Account(username, name, password, isAdmin);
                    allWorkerList.add(worker);
                    workersChoiceBox.getItems().add(worker.getName());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void loadDataFromWorksheetsDatabase() {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM worksheets WHERE work_id = '" + work.getId() + "'")) {

            while (resultSet.next()) {
                String worksheetID = resultSet.getString("worksheet_id");
                String workID = resultSet.getString("work_id");
                String username = resultSet.getString("worker_username"); // Corrected column name
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



    public void loadDataFromWorksheetsDatabaseAll() {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM worksheets")) {

            while (resultSet.next()) {
                String worksheetID = resultSet.getString("worksheet_id");
                String workID = resultSet.getString("work_id");
                String username = resultSet.getString("worker_username"); // Corrected column name
                String details = resultSet.getString("worksheet_description");
                String photo = resultSet.getString("worksheet_photo");
                String status = resultSet.getString("worksheet_status");

                Worksheet worksheet = new Worksheet(worksheetID, workID, username, details, photo, status);
                allWorksheetList.add(worksheet);
            }
        } catch (SQLException e) {
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

    public void handleLogoutButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/home.fxml");
    }

    public void handleBackButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/adminPlanWork.fxml");
    }

    public void addDataToWorksheetsDatabase(Worksheet newWorksheet) {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO worksheets (worksheet_id, work_id, worker_username, worksheet_description, worksheet_photo, worksheet_status) VALUES (?, ?, ?, ?, ?, ?)")) {

            preparedStatement.setString(1, newWorksheet.getWorksheetID());
            preparedStatement.setString(2, newWorksheet.getWorkID());
            preparedStatement.setString(3, newWorksheet.getUsername());
            preparedStatement.setString(4, newWorksheet.getDetails());
            preparedStatement.setString(5, newWorksheet.getPhoto());
            preparedStatement.setString(6, newWorksheet.getStatus());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

            // Save the updated worksheetList to the database
            addDataToWorksheetsDatabase(newWorksheet);

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

    public void removeDataToWorksheetsDatabase(Worksheet worksheet) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String sql = "DELETE FROM worksheets WHERE worksheet_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, worksheet.getWorksheetID());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleEraseButtonAction() {
        Worksheet selectedWorksheet = workersTable.getSelectionModel().getSelectedItem();
        if (selectedWorksheet != null) {
            // Remove the selected worksheet from the TableView
            workersTable.getItems().remove(selectedWorksheet);

            // Remove the selected worksheet from the worksheetList
            worksheetList.remove(selectedWorksheet);

            // Save the updated worksheetList to the database
            removeDataToWorksheetsDatabase(selectedWorksheet);

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
