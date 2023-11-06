package com.example.project02.controllers;

import com.example.project02.models.Account;
import com.example.project02.models.Work;
import com.example.project02.models.Worksheet;
import com.example.project02.services.PageNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminWorksheetController {
    @FXML
    private ChoiceBox<String> statusChoiceBox;
    @FXML
    private Label loginNameLabel;
    @FXML
    private Label workLabel;
    @FXML
    private Label detailsLabel;
    @FXML
    private ImageView imageView;
    @FXML
    private Label statusLabel;
    private Account loginAccount;
    private Work work;
    private List<Worksheet> worksheetList = new ArrayList<>();
    private Account selectedWorker;
    private Worksheet selectedWorksheet;

    public void initialize() {
        loadDataFromLoggingInCSV();
        loadDataFromSelectedWorkCSV();
        loadDataFromWorksheetsCSV();
        loadDataFromSelectedWorkerCSV();
        for (Worksheet worksheet : worksheetList) {
            if (Objects.equals(worksheet.getUsername(), selectedWorker.getUsername()) && Objects.equals(work.getId(), worksheet.getWorkID())) {
                selectedWorksheet = worksheet;
                break;
            }
        }
        loginNameLabel.setText(loginAccount.getName());
        workLabel.setText("work ID " + work.getId() + " : " + work.getName() + " | " + selectedWorker.getName());
        detailsLabel.setText(selectedWorksheet.getDetails());
        statusLabel.setText(selectedWorksheet.getStatus());
        statusChoiceBox.getItems().addAll( "แก้ไข", "เสร็จสิ้น");
        String photoPath = "/com/example/project02/images/" + selectedWorksheet.getPhoto();
        URL imageUrl = getClass().getResource(photoPath);

        if (imageUrl != null) {
            Image image = new Image(imageUrl.toExternalForm());
            imageView.setImage(image);
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
                    worksheetList.add(worksheet);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDataFromSelectedWorkerCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("csv/selected_worker.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) { // Assuming there are 4 fields in the CSV
                    String username = parts[0];
                    String name = parts[1];
                    String password = parts[2];
                    boolean isAdmin = Boolean.parseBoolean(parts[3]);
                    selectedWorker = new Account(username, name, password, isAdmin);
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
        PageNavigator.navigateToPage("/com/example/project02/views/adminWorkDetails.fxml");
    }

    public void handleChangeStatusButtonAction() {
        String newStatus = statusChoiceBox.getValue();

        if (newStatus != null) {
            selectedWorksheet.setStatus(newStatus);
            for (Worksheet worksheet : worksheetList) {
                if (selectedWorksheet == worksheet) {
                    worksheet.setStatus(newStatus);
                }
            }
            statusLabel.setText(newStatus);
            saveDataToWorksheetsCSV(worksheetList);
        }
    }

}
