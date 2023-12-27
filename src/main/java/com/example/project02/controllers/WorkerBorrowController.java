package com.example.project02.controllers;

import com.example.project02.models.*;
import com.example.project02.services.PageNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorkerBorrowController {
    @FXML
    private Label loginNameLabel;
    @FXML
    private Label checkedLabel;
    @FXML
    private TextField numberField;
    @FXML
    private TableView<Equipment> equipmentTable;
    @FXML
    private TableColumn<Equipment, String> nameColumn;
    @FXML
    private TableColumn<Equipment, Integer> numberColumn;
    @FXML
    private ChoiceBox<String> equipmentChoiceBox;
    private Account loginAccount;
    public Equipment selectedEquipment = null;
    private List<Equipment> equipmentList = new ArrayList<>();
    private List<BorrowForm> borrowFormList = new ArrayList<>();
    String url = "jdbc:postgresql://db.wxxhmqjeruggsslfbkhs.supabase.co/postgres";
    String user = "postgres"; // Replace with your DB username
    String password = "8hlUWjTUakLNou2C"; // Replace with your DB password

    public void initialize() {
        loadDataFromLoggingInCSV();
        loadDataFromEquipmentDatabase();
        loadDataFromBorrowFormDatabase();
        checkedLabel.setText("");
        loginNameLabel.setText(loginAccount.getName());
        ObservableList<Equipment> observableEquipments = FXCollections.observableArrayList(equipmentList);
        equipmentTable.setItems(observableEquipments);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));

    }

    public void loadDataFromEquipmentDatabase() {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String query = "SELECT equipment_name, equipment_number, equipment_borrowing FROM equipments"; // Replace 'equipments_table' with your actual table name

            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    String name = resultSet.getString("equipment_name");
                    int number = resultSet.getInt("equipment_number");
                    int borrowing = resultSet.getInt("equipment_borrowing");

                    Equipment equipment = new Equipment(name, number, borrowing);
                    equipmentList.add(equipment);
                    equipmentChoiceBox.getItems().add(equipment.getName());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void updateBorrowFormInDatabase(BorrowForm borrowForm) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String query = "UPDATE borrow_forms SET borrower = ?, borrow_equipment = ?, borrow_date = ?, borrow_number = ?, returned_number = ?, bf_status = ? WHERE bf_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, borrowForm.getBorrower());
                statement.setString(2, borrowForm.getEquipmentName());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime borrowDate = LocalDateTime.parse(borrowForm.getBorrowDate(), formatter);

                // Convert LocalDateTime to Timestamp
                Timestamp timestamp = Timestamp.valueOf(borrowDate);
                statement.setTimestamp(3, timestamp); // Set the timestamp instead of the string

                statement.setInt(4, borrowForm.getBorrowNumber());
                statement.setInt(5, borrowForm.getReturnedNumber());
                statement.setString(6, borrowForm.getStatus());
                statement.setString(7, borrowForm.getId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public void updateEquipmentInDatabase(List<Equipment> equipmentList) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            for (Equipment equipment : equipmentList) {
                String query = "UPDATE public.equipments SET equipment_number = ?, equipment_borrowing = ? WHERE equipment_name = ?";

                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, equipment.getNumber());
                    statement.setInt(2, equipment.getBorrowing());
                    statement.setString(3, equipment.getName());

                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public void loadDataFromBorrowFormDatabase() {
        borrowFormList.clear(); // Clear existing data before loading from the database
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String query = "SELECT * FROM borrow_forms"; // Modify the query based on your table structure

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    String id = resultSet.getString("bf_id");
                    String borrower = resultSet.getString("borrower");
                    String equipmentName = resultSet.getString("borrow_equipment");
                    String borrowDate = resultSet.getString("borrow_date"); // Adjust as per your database type
                    int borrowNumber = resultSet.getInt("borrow_number");
                    int returnedNumber = resultSet.getInt("returned_number");
                    String status = resultSet.getString("bf_status");

                    BorrowForm borrowForm = new BorrowForm(id, borrower, equipmentName, borrowDate, borrowNumber, returnedNumber, status);
                    borrowFormList.add(borrowForm);
                }
            }
        } catch (SQLException e) {
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

    public void handleBackButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/workerEquip.fxml");
    }

    public void handleLogoutButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/home.fxml");
    }

    public void handleBorrowButtonAction() {
        String selectedEquipmentStr = equipmentChoiceBox.getValue();
        String numberInput = numberField.getText();
        if (!numberInput.matches("\\d+")) {
            checkedLabel.setText("กรุณาป้อนจำนวนเป็นตัวเลขเท่านั้น");
            checkedLabel.setTextFill(Color.RED);
            return; // Exit the method
        }
        int number;

        try {
            number = Integer.parseInt(numberInput);
        } catch (NumberFormatException e) {
            checkedLabel.setText("จำนวนต้องอยู่ระหว่าง 1 ถึงจำนวนที่มีทั้งหมดในคลังของอุปกรณ์นี้");
            checkedLabel.setTextFill(Color.RED);
            numberField.clear();
            return;
        }
        for (Equipment equipment : equipmentList) {
            if (Objects.equals(selectedEquipmentStr, equipment.getName())) {
                selectedEquipment = equipment;
            }
        }
        if (number > 0 && number <= selectedEquipment.getNumber()) {
            if (selectedEquipment != null) {
                BorrowForm selectedBorrowForm = null;
                for (BorrowForm borrowForm : borrowFormList) {
                    if (Objects.equals(selectedEquipment.getName(), borrowForm.getEquipmentName()) && Objects.equals(loginAccount.getUsername(), borrowForm.getBorrower())) {
                        selectedBorrowForm = borrowForm;
                        break;
                    }
                }
                if (selectedBorrowForm != null) {
                    selectedBorrowForm.setBorrowDate();
                    selectedBorrowForm.setBorrowNumber(selectedBorrowForm.getBorrowNumber() + number - selectedBorrowForm.getReturnedNumber());
                    selectedBorrowForm.setReturnedNumber(0);
                    selectedBorrowForm.setStatus("ยังไม่คืน");
                } else {
                    selectedBorrowForm = new BorrowForm(loginAccount.getUsername(), selectedEquipmentStr, number, 0, "ยังไม่คืน");
                    borrowFormList.add(selectedBorrowForm);
                }
                selectedEquipment.setNumber(selectedEquipment.getNumber() - number);
                selectedEquipment.setBorrowing(selectedEquipment.getBorrowing() + number);
                updateBorrowFormInDatabase(selectedBorrowForm);
                updateEquipmentInDatabase(equipmentList);
                equipmentTable.refresh();
                numberField.clear();
                checkedLabel.setText("ยืมอุปกรณ์สำเร็จ");
                checkedLabel.setTextFill(Color.GREEN);
            }
        } else {
            checkedLabel.setText("จำนวนต้องอยู่ระหว่าง 1 ถึงจำนวนที่มีทั้งหมดในคลังของอุปกรณ์นี้");
            checkedLabel.setTextFill(Color.RED);
        }
        numberField.clear();
    }
}
