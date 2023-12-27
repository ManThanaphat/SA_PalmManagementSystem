package com.example.project02.controllers;

import com.example.project02.models.Account;
import com.example.project02.models.Equipment;
import com.example.project02.models.Plan;
import com.example.project02.services.PageNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.io.*;
import java.sql.*;
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
    @FXML
    private ChoiceBox<String> doChoiceBox;
    private Account loginAccount;
    private List<Equipment> equipmentList = new ArrayList<>();

    String url = "jdbc:postgresql://db.wxxhmqjeruggsslfbkhs.supabase.co/postgres";
    String user = "postgres"; // Replace with your DB username
    String password = "8hlUWjTUakLNou2C"; // Replace with your DB password

    public void initialize() {
        errorLabel.setText("");
        loadDataFromLoggingInCSV();

        String sqlCheckEqual = "SELECT * FROM equipments";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = conn.prepareStatement(sqlCheckEqual)) {

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {

                    Equipment equipment = new Equipment(
                            rs.getString("equipment_name"),
                            rs.getInt("equipment_number"), // Use getInt for an integer column
                            rs.getInt("equipment_borrowing")
                    );

                    equipmentList.add(equipment);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace(); // In production, consider a more robust error handling mechanism
        }

        loginNameLabel.setText(loginAccount.getName());
        ObservableList<Equipment> observableEquipments = FXCollections.observableArrayList(equipmentList);
        equipmentTable.setItems(observableEquipments);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        borrowingColumn.setCellValueFactory(new PropertyValueFactory<>("borrowing"));

        ObservableList<String> choices = FXCollections.observableArrayList(
                "เพิ่มรายชื่ออุปกรณ์",
                "ลบรายชื่ออุปกรณ์",
                "เพิ่มจำนวนอุปกรณ์",
                "ลดจำนวนอุปกรณ์"
        );
        doChoiceBox.setItems(choices);
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

    public void addDataToEquipmentsDatabase(Equipment equipment) {
        String sqlInsert = "INSERT INTO equipments (equipment_name, equipment_number, equipment_borrowing) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = conn.prepareStatement(sqlInsert)) {

            // Set values for the single equipment
            pst.setString(1, equipment.getName());
            pst.setInt(2, equipment.getNumber());
            pst.setInt(3, equipment.getBorrowing());

            // Execute the single insert
            pst.executeUpdate();

        } catch (SQLException e) {
            // Log the error or display a user-friendly message
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
        String selectedAction = doChoiceBox.getValue(); // Get the selected action from the choice box

        if (selectedAction == null) {
            errorLabel.setText("กรุณาเลือกคำสั่ง");
            errorLabel.setTextFill(Color.RED);
            return;
        }

        switch (selectedAction) {
            case "เพิ่มรายชื่ออุปกรณ์":
                add();
                break;
            case "ลบรายชื่ออุปกรณ์":
                delete();
                break;
            case "เพิ่มจำนวนอุปกรณ์":
                increase();
                break;
            case "ลดจำนวนอุปกรณ์":
                decrease();
                break;
            default:
                errorLabel.setText("กรุณาเลือกคำสั่ง");
                errorLabel.setTextFill(Color.RED);
                break;
        }
    }
    public void add() {
        String name = nameField.getText();
        try {
            int number = Integer.parseInt(numberField.getText());

            // Define the maximum allowed value (e.g., 99999)
            int maxAllowedValue = 99;

            if (number > maxAllowedValue) {
                // Prevent further actions
                numberField.clear();
                errorLabel.setText("จำนวนต้องอยู่ระหว่าง 1 ถึง 99");
                errorLabel.setTextFill(Color.RED);
                return; // Exit the method early
            }

            if (number <= 0) {
                errorLabel.setText("กรุณากรอกข้อมูลเป็นจำนวนเต็มบวก");
                errorLabel.setTextFill(Color.RED);
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
                Equipment newEquipment = new Equipment(name, number, 0);
                equipmentList.add(newEquipment);
                addDataToEquipmentsDatabase(newEquipment);
                errorLabel.setText("");
            } else {
                errorLabel.setText("ชื่ออุปกรณ์ต้องไม่ซ้ำกัน");
                errorLabel.setTextFill(Color.RED);
                return;
            }
            equipmentTable.refresh();
            ObservableList<Equipment> observableEquipments = FXCollections.observableArrayList(equipmentList);
            equipmentTable.setItems(observableEquipments);
        } catch (NumberFormatException e) {
            // Handle invalid number input here (e.g., clear the field)
            numberField.clear();
            errorLabel.setText("จำนวนต้องอยู่ระหว่าง 1 ถึง 99");
            errorLabel.setTextFill(Color.RED);
            return;
        }
        numberField.clear();
        nameField.clear();
    }

    private void delete() {
        String equipmentNameToDelete = nameField.getText();

        boolean found = false;
        for (Equipment equipment : equipmentList) {
            if (equipment.getName().equals(equipmentNameToDelete)) {
                found = true;
                if (equipment.getBorrowing() > 0) {
                    // If the selected equipment has borrowings, show an error message
                    errorLabel.setText("ไม่สามารถลบอุปกรณ์ที่มีการยืม");
                    errorLabel.setTextFill(Color.RED);
                    return;
                } else {
                    // Delete the equipment from the list and database
                    equipmentList.remove(equipment);
                    deleteEquipmentFromDatabase(equipment);
                    errorLabel.setText(""); // Clear any previous error messages
                    ObservableList<Equipment> observableEquipments = FXCollections.observableArrayList(equipmentList);
                    equipmentTable.setItems(observableEquipments);
                    equipmentTable.refresh();
                    return;
                }
            }
        }

        // If the equipment name is not found in the table
        if (!found) {
            errorLabel.setText("ไม่พบอุปกรณ์ที่ต้องการลบ");
            errorLabel.setTextFill(Color.RED);
        }
    }

    public void deleteEquipmentFromDatabase(Equipment equipment) {
        String sqlDelete = "DELETE FROM equipments WHERE equipment_name = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = conn.prepareStatement(sqlDelete)) {

            // Set the parameter for the delete query
            pst.setString(1, equipment.getName());

            // Execute the delete query
            pst.executeUpdate();

        } catch (SQLException e) {
            // Handle or log the SQL exception
            e.printStackTrace();
        }
    }

    private Equipment findEquipmentByName(String equipmentName) {
        for (Equipment equipment : equipmentList) {
            if (equipment.getName().equals(equipmentName)) {
                return equipment;
            }
        }
        return null;
    }

    private void increase() {
        String name = nameField.getText();
        try {
            int increaseBy = Integer.parseInt(numberField.getText());

            // Define the maximum allowed value
            int maxAllowedValue = 99;

            if (increaseBy > maxAllowedValue) {
                // Prevent further actions
                numberField.clear();
                errorLabel.setText("จำนวนต้องอยู่ระหว่าง 1 ถึง 99");
                errorLabel.setTextFill(Color.RED);
                return; // Exit the method early
            }

            if (increaseBy <= 0) {
                errorLabel.setText("กรุณากรอกข้อมูลเป็นจำนวนเต็มบวก");
                errorLabel.setTextFill(Color.RED);
                return;
            }

            Equipment equipmentToUpdate = findEquipmentByName(name);

            if (equipmentToUpdate != null) {
                int currentCount = equipmentToUpdate.getNumber();
                int updatedCount = currentCount + increaseBy;

                // Check if the updated count exceeds the maximum allowed value
                if (updatedCount <= maxAllowedValue) {
                    equipmentToUpdate.setNumber(updatedCount);
                    updateEquipmentInDatabase(equipmentToUpdate);

                    // Refresh the table
                    equipmentTable.refresh();
                    ObservableList<Equipment> observableEquipments = FXCollections.observableArrayList(equipmentList);
                    equipmentTable.setItems(observableEquipments);
                    errorLabel.setText("");
                    numberField.clear();
                    nameField.clear();
                } else {
                    // Display an error if the updated count exceeds the maximum allowed value
                    errorLabel.setText("ไม่สามารถเพิ่มจำนวนเกิน " + (maxAllowedValue - currentCount));
                    errorLabel.setTextFill(Color.RED);
                }
            } else {
                // Equipment not found
                errorLabel.setText("ไม่พบอุปกรณ์ที่ต้องการเพิ่มจำนวน");
                errorLabel.setTextFill(Color.RED);
            }
        } catch (NumberFormatException e) {
            // Handle invalid number input here (e.g., clear the field)
            numberField.clear();
            errorLabel.setText("จำนวนต้องอยู่ระหว่าง 1 ถึง 99");
            errorLabel.setTextFill(Color.RED);
        }
    }

    private void decrease() {
        String name = nameField.getText();
        try {
            int decreaseBy = Integer.parseInt(numberField.getText());

            // Find the equipment to decrease from the list
            for (Equipment equipment : equipmentList) {
                if (Objects.equals(equipment.getName(), name)) {
                    int currentNumber = equipment.getNumber();

                    // Check if the decrease operation is valid
                    if (currentNumber - decreaseBy >= 0) {
                        equipment.setNumber(currentNumber - decreaseBy);

                        // Update the equipment in the database
                        updateEquipmentInDatabase(equipment);

                        // Refresh the table view
                        equipmentTable.refresh();
                        ObservableList<Equipment> observableEquipments = FXCollections.observableArrayList(equipmentList);
                        equipmentTable.setItems(observableEquipments);

                        // Clear any previous error messages and fields
                        errorLabel.setText("");
                        numberField.clear();
                        nameField.clear();
                    } else {
                        // Show an error if the decrease operation exceeds the current number
                        errorLabel.setText("จำนวนไม่เพียงพอสำหรับการลด");
                        errorLabel.setTextFill(Color.RED);
                    }
                    return;
                }
            }

            // If the equipment name is not found in the table
            errorLabel.setText("ไม่พบอุปกรณ์ที่ต้องการลดจำนวน");
            errorLabel.setTextFill(Color.RED);
        } catch (NumberFormatException e) {
            // Handle invalid number input here (e.g., clear the field)
            numberField.clear();
            errorLabel.setText("กรุณากรอกข้อมูลเป็นจำนวนเต็มบวก");
            errorLabel.setTextFill(Color.RED);
        }
    }


    // Method to update equipment in the database
    private void updateEquipmentInDatabase(Equipment equipment) {
        String sqlUpdate = "UPDATE equipments SET equipment_number = ?, equipment_borrowing = ? WHERE equipment_name = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = conn.prepareStatement(sqlUpdate)) {

            // Set values for the update
            pst.setInt(1, equipment.getNumber());
            pst.setInt(2, equipment.getBorrowing());
            pst.setString(3, equipment.getName());

            // Execute the update
            pst.executeUpdate();

        } catch (SQLException e) {
            // Handle or log the SQL exception
            e.printStackTrace();
        }
    }



    public void handleLogoutButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/home.fxml");
    }

    public void handleBackButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/admin.fxml");
    }

}
