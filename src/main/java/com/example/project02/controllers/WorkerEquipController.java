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

    public void initialize() {
        loadDataFromLoggingInCSV();
        loadDataFromEquipmentCSV();
        loadDataFromBorrowFormCSV();
        loadDataFromBorrowFormCSVBorrowedOnly();
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

    public void loadDataFromEquipmentCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("csv/equipment.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) { // Assuming there are 6 fields in the CSV
                    String name = parts[0];
                    int number = Integer.parseInt(parts[1]);
                    int borrowing = Integer.parseInt(parts[2]);
                    Equipment equipment = new Equipment(name, number, borrowing);
                    equipmentList.add(equipment);
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

    public void loadDataFromBorrowFormCSVBorrowedOnly() {
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
                    if (Objects.equals(borrower, loginAccount.getUsername()) && borrowNumber - returnedNumber > 0) {
                        BorrowForm borrowForm = new BorrowForm(id, borrower, equipmentName, borrowDate, borrowNumber, returnedNumber, status);
                        borrowFormList.add(borrowForm);
                    }
                }
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
                    allBorrowFormList.add(borrowForm);
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
                saveDataToBorrowFormCSV(allBorrowFormList);
                saveDataToEquipmentsCSV(equipmentList);
                borrowFormList.clear();
                equipmentTable.refresh();
                loadDataFromBorrowFormCSVBorrowedOnly();
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
