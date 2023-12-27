package com.example.project02.controllers;

import com.example.project02.models.*;
import com.example.project02.services.PageNavigator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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

public class WorkerEquipController {
    @FXML
    private Label loginNameLabel;
    @FXML
    private Label checkedLabel;
    @FXML
    private TextField numberField;
    @FXML
    private TableView<BorrowForm> equipmentTable;
    @FXML
    private TableColumn<BorrowForm, String> nameColumn;
    @FXML
    private TableColumn<BorrowForm, Integer> numberColumn;
    @FXML
    private ChoiceBox<String> equipmentChoiceBox;
    private Account loginAccount;
    public Equipment selectedEquipment = null;
    private List<Equipment> equipmentList = new ArrayList<>();
    private List<BorrowForm> borrowFormList = new ArrayList<>();
    private List<BorrowForm> allBorrowFormList = new ArrayList<>();
    String url = "jdbc:postgresql://db.wxxhmqjeruggsslfbkhs.supabase.co/postgres";
    String user = "postgres"; // Replace with your DB username
    String password = "8hlUWjTUakLNou2C"; // Replace with your DB password

    public void initialize() {
        loadDataFromLoggingInCSV();
        loadDataFromEquipmentDatabase();
        loadDataFromBorrowFormDatabase();
        loadDataFromBorrowFormDatabaseBorrowedOnly();
        checkedLabel.setText("");
        loginNameLabel.setText(loginAccount.getName());
        ObservableList<BorrowForm> observableEquipments = FXCollections.observableArrayList(borrowFormList);
        equipmentTable.setItems(observableEquipments);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("equipmentName"));
        numberColumn.setCellValueFactory(data -> {
            int number = data.getValue().getBorrowNumber() - data.getValue().getReturnedNumber();
            ObservableValue<Integer> observableValue = Bindings.createObjectBinding(() -> number);
            return observableValue;
        });
        for (BorrowForm borrowForm : borrowFormList) {
            equipmentChoiceBox.getItems().add(borrowForm.getEquipmentName());
        }

    }

    public void loadDataFromEquipmentDatabase() {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String query = "SELECT equipment_name, equipment_number, equipment_borrowing FROM public.equipments";
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    String name = resultSet.getString("equipment_name");
                    int number = resultSet.getInt("equipment_number");
                    int borrowing = resultSet.getInt("equipment_borrowing");

                    Equipment equipment = new Equipment(name, number, borrowing);
                    equipmentList.add(equipment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateBorrowFormData(BorrowForm borrowForm) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String query = "UPDATE borrow_forms SET borrower = ?, borrow_equipment = ?, borrow_date = ?, borrow_number = ?, returned_number = ?, bf_status = ? WHERE bf_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, borrowForm.getBorrower());
                statement.setString(2, borrowForm.getEquipmentName());

                // Assuming borrowDate is a LocalDateTime, convert it to Timestamp
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime borrowDate = LocalDateTime.parse(borrowForm.getBorrowDate(), formatter);

                // Convert LocalDateTime to Timestamp
                Timestamp timestamp = Timestamp.valueOf(borrowDate);

                statement.setTimestamp(3, timestamp);
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

    public void updateEquipmentData(Equipment equipment) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String query = "UPDATE equipments SET equipment_number = ?, equipment_borrowing = ? WHERE equipment_name = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, equipment.getNumber());
                statement.setInt(2, equipment.getBorrowing());
                statement.setString(3, equipment.getName());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public void loadDataFromBorrowFormDatabaseBorrowedOnly() {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String query = "SELECT bf_id, borrower, borrow_equipment, borrow_date, borrow_number, returned_number, bf_status " +
                    "FROM public.borrow_forms " +
                    "WHERE borrower = ? AND (borrow_number - returned_number) > 0";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, loginAccount.getUsername());
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    String id = resultSet.getString("bf_id");
                    String borrower = resultSet.getString("borrower");
                    String equipmentName = resultSet.getString("borrow_equipment");
                    String borrowDate = resultSet.getString("borrow_date"); // Change type if different in the database
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


    public void loadDataFromBorrowFormDatabase() {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String query = "SELECT bf_id, borrower, borrow_equipment, borrow_date, borrow_number, returned_number, bf_status " +
                    "FROM public.borrow_forms";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    String id = resultSet.getString("bf_id");
                    String borrower = resultSet.getString("borrower");
                    String equipmentName = resultSet.getString("borrow_equipment");
                    String borrowDate = resultSet.getString("borrow_date"); // Change type if different in the database
                    int borrowNumber = resultSet.getInt("borrow_number");
                    int returnedNumber = resultSet.getInt("returned_number");
                    String status = resultSet.getString("bf_status");

                    BorrowForm borrowForm = new BorrowForm(id, borrower, equipmentName, borrowDate, borrowNumber, returnedNumber, status);
                    allBorrowFormList.add(borrowForm);
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
        PageNavigator.navigateToPage("/com/example/project02/views/worker.fxml");
    }

    public void handleLogoutButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/home.fxml");
    }

    public void handleReturnButtonAction() {
        String selectedEquipmentStr = equipmentChoiceBox.getValue();
        BorrowForm selectedBorrowForm = null;
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
            numberField.clear();
            checkedLabel.setText("จำนวนต้องอยู่ระหว่าง 1 ถึงจำนวนที่ยืมทั้งหมดของอุปกรณ์นี้");
            checkedLabel.setTextFill(Color.RED);
            return;
        }
        for (Equipment equipment : equipmentList) {
            if (Objects.equals(selectedEquipmentStr, equipment.getName())) {
                selectedEquipment = equipment;
            }
        }
        for (BorrowForm borrowForm : borrowFormList) {
            if (Objects.equals(selectedEquipmentStr, borrowForm.getEquipmentName())) {
                selectedBorrowForm = borrowForm;
            }
        }
        if (selectedBorrowForm != null && selectedEquipment != null) {
            if (number > 0 && number <= selectedBorrowForm.getBorrowNumber() - selectedBorrowForm.getReturnedNumber()) {
                selectedBorrowForm.setReturnedNumber(selectedBorrowForm.getReturnedNumber() + number);
                selectedEquipment.setNumber(selectedEquipment.getNumber() + number);
                selectedEquipment.setBorrowing(selectedEquipment.getBorrowing() - number);
                for (BorrowForm borrowForm : allBorrowFormList) {
                    if (Objects.equals(borrowForm.getId(), selectedBorrowForm.getId())) {
                        borrowForm.setReturnedNumber(selectedBorrowForm.getReturnedNumber());
                        if (borrowForm.getReturnedNumber() == borrowForm.getBorrowNumber()) {
                            borrowForm.setStatus("คืนครบแล้ว");
                        } else {
                            borrowForm.setStatus("คืนไม่ครบ");
                        }
                    }
                }
                // Check if all items have been returned
                if (selectedBorrowForm.getBorrowNumber() - selectedBorrowForm.getReturnedNumber() == 0) {
                    borrowFormList.remove(selectedBorrowForm);
                    equipmentChoiceBox.getItems().remove(selectedEquipmentStr);
                }
                updateBorrowFormData(selectedBorrowForm);
                updateEquipmentData(selectedEquipment);
                borrowFormList.clear();
                equipmentTable.refresh();
                loadDataFromBorrowFormDatabaseBorrowedOnly();
                ObservableList<BorrowForm> observableEquipments = FXCollections.observableArrayList(borrowFormList);
                equipmentTable.setItems(observableEquipments);
                numberField.clear();
                checkedLabel.setText("คืนอุปกรณ์สำเร็จ");
                checkedLabel.setTextFill(Color.GREEN);
            } else {
                checkedLabel.setText("จำนวนต้องอยู่ระหว่าง 1 ถึงจำนวนที่ยืมทั้งหมดของอุปกรณ์นี้");
                checkedLabel.setTextFill(Color.RED);
            }

        }
        numberField.clear();
    }

    public void handleBorrowButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/workerBorrow.fxml");
    }
}
