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

    public void initialize() {
        loadDataFromLoggingInCSV();
        loadDataFromEquipmentCSV();
        loadDataFromBorrowFormCSV();
        checkedLabel.setText("");
        loginNameLabel.setText(loginAccount.getName());
        ObservableList<Equipment> observableEquipments = FXCollections.observableArrayList(equipmentList);
        equipmentTable.setItems(observableEquipments);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));

    }

    public void loadDataFromEquipmentCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("csv/equipment.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) { // Assuming there are 6 fields in the CSV
                    String name = parts[0];
                    int number = Integer.parseInt(parts[1]);
                    int borrowing = Integer.parseInt(parts[2]);
                    Equipment equipment = new Equipment(name,number,borrowing);
                    equipmentList.add(equipment);
                    equipmentChoiceBox.getItems().add(equipment.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDataToBorrowFormCSV(List<BorrowForm> borrowFormList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("csv/borrow_form.csv"))) {
            for (BorrowForm borrowForm : borrowFormList) {
                writer.write(borrowForm.getId() + "," +
                        borrowForm.getBorrower() + "," +
                        borrowForm.getEquipmentName() + "," +
                        borrowForm.getBorrowDate() + "," +
                        borrowForm.getBorrowNumber() + "," +
                        borrowForm.getReturnedNumber() + "," +
                        borrowForm.getStatus());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDataToEquipmentsCSV(List<Equipment> equipmentList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("csv/equipment.csv"))) {
            for (Equipment equipment : equipmentList) {
                writer.write(equipment.getName() + "," +
                        equipment.getNumber() + "," +
                        equipment.getBorrowing());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDataFromBorrowFormCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("csv/borrow_form.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 7) { // Assuming there are 6 fields in the CSV
                    String id = parts[0];
                    String borrower = parts[1];
                    String equipmentName = parts[2];
                    String borrowDate = parts[3]; // Assuming it's a String, not an int
                    int borrowNumber = Integer.parseInt(parts[4]);
                    int returnedNumber = Integer.parseInt(parts[5]);
                    String status = parts[6];
                    BorrowForm borrowForm = new BorrowForm(id, borrower, equipmentName, borrowDate, borrowNumber, returnedNumber, status);
                    borrowFormList.add(borrowForm);
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
                saveDataToBorrowFormCSV(borrowFormList);
                saveDataToEquipmentsCSV(equipmentList);
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
