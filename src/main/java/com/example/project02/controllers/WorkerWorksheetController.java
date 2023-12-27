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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorkerWorksheetController {
    @FXML
    private Label loginNameLabel;
    @FXML
    private Label workLabel;
    @FXML
    private Label detailsLabel;
    @FXML
    private Button selectButton;
    @FXML
    private TextArea detailsArea;
    @FXML
    private Label checkedLabel;
    @FXML
    private ImageView imageView;
    private Account loginAccount;
    private Work work;
    private Worksheet selectedWorksheet;
    private List<Worksheet> worksheetList = new ArrayList<>();
    private List<Worksheet> allWorksheetList = new ArrayList<>();
    private java.io.File selectedFile;
    String url = "jdbc:postgresql://db.wxxhmqjeruggsslfbkhs.supabase.co/postgres";
    String user = "postgres"; // Replace with your DB username
    String password = "8hlUWjTUakLNou2C"; // Replace with your DB password

    public void initialize() {
        checkedLabel.setText("");
        loadDataFromSelectedWorkCSV();
        workLabel.setText("ID " + work.getId() + " : " + work.getName());
        loadDataFromLoggingInCSV();
        loginNameLabel.setText(loginAccount.getName());
        loadDataFromWorksheetsDatabase();
        loadDataFromWorksheetsDatabaseAll();
        for (Worksheet worksheet : worksheetList) {
            if (Objects.equals(worksheet.getWorkID(), work.getId()) && Objects.equals(worksheet.getUsername(), loginAccount.getUsername())) {
                selectedWorksheet = worksheet;
            }
        }
        detailsLabel.setText(selectedWorksheet.getDetails());
        String photoPath = "/com/example/project02/images/" + selectedWorksheet.getPhoto();
        URL imageUrl = getClass().getResource(photoPath);
        if (imageUrl != null) {
            Image image = new Image(imageUrl.toExternalForm());
            imageView.setImage(image);
        } else {
            // Handle the case where the image resource could not be found
            // You can log an error or provide a default image in this case
            // For example:
            Image defaultImage = new Image("/com/example/project02/images/default.png");
            imageView.setImage(defaultImage);
        }
    }

    public void loadDataFromWorksheetsDatabaseAll() {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM worksheets")) {

            while (resultSet.next()) {
                String worksheetID = resultSet.getString("worksheet_id");
                String workID = resultSet.getString("work_id");
                String username = resultSet.getString("worker_username");
                String details = resultSet.getString("worksheet_description");
                String photo = resultSet.getString("worksheet_photo");
                String status = resultSet.getString("worksheet_status");

                Worksheet worksheet = new Worksheet(worksheetID, workID, username, details, photo, status);
                allWorksheetList.add(worksheet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions or errors here
        }
    }


    public void saveDataToWorksheetsDatabase(List<Worksheet> worksheetList) {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {

            for (Worksheet worksheet : worksheetList) {
                String updateQuery = "UPDATE worksheets SET " +
                        "worksheet_description = '" + worksheet.getDetails() + "', " +
                        "worksheet_photo = '" + worksheet.getPhoto() + "', " +
                        "worksheet_status = '" + worksheet.getStatus() + "' " +
                        "WHERE worksheet_id = '" + worksheet.getWorksheetID() + "'";
                statement.executeUpdate(updateQuery);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions or errors here
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

    public void loadDataFromWorksheetsDatabase() {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM worksheets WHERE worker_username = '" + loginAccount.getUsername() + "'")) {

            while (resultSet.next()) {
                String worksheetID = resultSet.getString("worksheet_id");
                String workID = resultSet.getString("work_id");
                String username = resultSet.getString("worker_username");
                String details = resultSet.getString("worksheet_description");
                String photo = resultSet.getString("worksheet_photo");
                String status = resultSet.getString("worksheet_status");

                Worksheet worksheet = new Worksheet(worksheetID, workID, username, details, photo, status);
                worksheetList.add(worksheet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions or errors here
        }
    }


    public void handleSelectButtonAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"));
        fileChooser.setTitle("Select an Image");

        // Show the file dialog
        Stage stage = (Stage) selectButton.getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage); // Assign the selected file to the instance variable

        if (selectedFile != null) {
            // Load and display the selected image in the ImageView
            Image image = new Image(selectedFile.toURI().toString());
            imageView.setImage(image);
        }
    }

    public void handleSendButtonAction() {
        if (Objects.equals(selectedWorksheet.getPhoto(), "") && selectedFile == null){
            saveDataToWorksheetsDatabase(allWorksheetList);
            checkedLabel.setText("ต้องมีรายละเอียดและรูปภาพก่อนส่งงาน");
            checkedLabel.setTextFill(Color.YELLOW);
            return;
        }
        if (selectedFile != null || !Objects.equals(selectedWorksheet.getPhoto(), "")) { // Check if a file has been selected
            String details = selectedWorksheet.getDetails();
            if (!Objects.equals(detailsArea.getText(), "")) {
                details = detailsArea.getText();
                detailsArea.clear();
                detailsLabel.setText(details);
            }
            if (Objects.equals(details, "-")) {
                checkedLabel.setText("ต้องมีรายละเอียดและรูปภาพก่อนส่งงาน");
                checkedLabel.setTextFill(Color.YELLOW);
                return;
            }
            if (selectedFile != null) {
                // Copy the selected image to the resources directory and get the new copy image name
                String newCopyImageName = copyImageToResourcesDirectory(selectedFile, details);
                for (Worksheet worksheet : allWorksheetList) {
                    if (Objects.equals(worksheet.getWorksheetID(), selectedWorksheet.getWorksheetID())) {
                        worksheet.setDetails(details);
                        worksheet.setStatus("รอตรวจ");
                        worksheet.setPhoto(newCopyImageName);
                    }
                }
            } else {
                for (Worksheet worksheet : allWorksheetList) {
                    if (Objects.equals(worksheet.getWorksheetID(), selectedWorksheet.getWorksheetID())) {
                        worksheet.setDetails(details);
                        worksheet.setStatus("รอตรวจ");
                    }
                }
            }
            // Save the updated worksheet data to CSV
            saveDataToWorksheetsDatabase(allWorksheetList);
            checkedLabel.setText("ส่งงานเรียบร้อย รอตรวจสอบ");
            checkedLabel.setTextFill(Color.WHITE);
        } else {
            // Handle the case where no file has been selected
            // You might want to show an error message or take appropriate action here
        }
    }

    private String copyImageToResourcesDirectory(File file, String details) {
        // Ensure the target directory exists (adjust the path as needed)
        String targetDirectoryPath = "src/main/resources/com/example/project02/images";
        File targetDirectory = new File(targetDirectoryPath);
        if (!targetDirectory.exists()) {
            if (targetDirectory.mkdirs()) {
                System.out.println("Created target directory: " + targetDirectoryPath);
            } else {
                System.err.println("Failed to create target directory: " + targetDirectoryPath);
                return null; // Return null if directory creation fails
            }
        }

        // Generate a unique filename for the image
        String originalFileName = file.getName();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uniqueFileName = generateUniqueFileName(targetDirectoryPath, originalFileName, fileExtension);
        Path targetPath = Path.of(targetDirectoryPath, uniqueFileName);

        try {
            Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Image copied to resources directory with unique name: " + uniqueFileName);
            return uniqueFileName; // Return the new copy image name
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error copying the image to the resources directory.");
            return null; // Return null if an error occurs during copying
        }
    }



    private String generateUniqueFileName(String directory, String baseName, String extension) {
        // Check if the file with the same name exists in the directory
        Path targetPath = Path.of(directory, baseName);
        int count = 1;

        while (Files.exists(targetPath)) {
            String fileNameWithoutExtension = baseName.substring(0, baseName.lastIndexOf('.'));
            String newFileName = fileNameWithoutExtension + "_" + count + extension;
            targetPath = Path.of(directory, newFileName);
            count++;
        }

        return targetPath.getFileName().toString();
    }



    public void handleBackButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/worker.fxml");
    }

    public void handleLogoutButtonAction() {
        PageNavigator.navigateToPage("/com/example/project02/views/home.fxml");
    }

}
