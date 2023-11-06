package com.example.project02.controllers;

import com.example.project02.models.Account;
import com.example.project02.models.Equipment;
import com.example.project02.models.Plan;
import com.example.project02.services.PageNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminEquipController {
    @FXML
    private Label loginNameLabel;
    @FXML
    private TextField nameField;
    @FXML
    private TextField numberField;
    @FXML
    private Label errorLabel;
    @FXML
    private TableView<Equipment> equipmentTable;
    @FXML
    private TableColumn<Equipment, String> nameColumn;
    @FXML
    private TableColumn<Equipment, Integer> numberColumn;
    @FXML
    private TableColumn<Equipment, Integer> borrowingColumn;
    private Account loginAccount;
    private List<Equipment> equipmentList = new ArrayList<>();

    public void initialize() {
        errorLabel.setText("");
        loadDataFromLoggingInCSV();
        loadDataFromEquipmentCSV();
        loginNameLabel.setText(loginAccount.getName());
        ObservableList<Equipment> observableEquipments = FXCollections.observableArrayList(equipmentList);
        equipmentTable.setItems(observableEquipments);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        borrowingColumn.setCellValueFactory(new PropertyValueFactory<>("borrowing"));
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
                }
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

    public void saveDataToSelectedEquipment(Equipment equipment) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("csv/selected_equipment.csv"))) {
            writer.write(equipment.getName() + "," + equipment.getNumber() + "," + equipment.getBorrowing());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleEquipmentDoubleClickAction(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) { // Check for a double click
            Equipment selectedEquipment = equipmentTable.getSelectionModel().getSelectedItem();
            if (selectedEquipment != null) {
                saveDataToSelectedEquipment(selectedEquipment);
                PageNavigator.navigateToPage("/com/example/project02/views/adminEquipDetails.fxml");
            }
        }
    }

    public void handleSetButtonAction() {
        String name = nameField.getText();
        try {
            int number = Integer.parseInt(numberField.getText());

            // Define the maximum allowed value (e.g., 99999)
            int maxAllowedValue = 99999;

            if (number > maxAllowedValue) {
                // Prevent further actions
                numberField.clear();
                return; // Exit the method early
            }

            if (number < 0) {
                errorLabel.setText("กรุณากรอกข้อมูลเป็นจำนวนเต็มบวก");
                errorLabel.setTextFill(Color.YELLOW);
                return;
            }

            boolean same = false;
            for (Equipment equipment : equipmentList) {
                if (Objects.equals(equipment.getName(), name)) {
                    equipment.setNumber(number);
                    same = true;
                }
            }
            if (!same) {
                equipmentList.add(new Equipment(name, number, 0));
            }
            saveDataToEquipmentsCSV(equipmentList);
            equipmentTable.refresh();
            ObservableList<Equipment> observableEquipments = FXCollections.observableArrayList(equipmentList);
            equipmentTable.setItems(observableEquipments);
        } catch (NumberFormatException e) {
            // Handle invalid number input here (e.g., clear the field)
            numberField.clear();
        }
        numberField.clear();
        nameField.clear();
    }



    public void handleLogoutButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/home.fxml");
    }

    public void handleBackButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/admin.fxml");
    }

}
