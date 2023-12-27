package com.example.project02.controllers;

import com.example.project02.models.Account;
import com.example.project02.models.Plan;
import com.example.project02.models.Work;
import com.example.project02.models.Worksheet;
import com.example.project02.services.PageNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminPlanAssignController {
    @FXML
    private Label loginNameLabel;
    @FXML
    private TextField nameField;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TextArea detailsArea;
    @FXML
    private Label addedLabel;
    @FXML
    private Label workersLabel;
    @FXML
    private ChoiceBox<String> workersChoiceBox;
    private Account loginAccount;
    private List<Work> workList = new ArrayList<>();
    private List<Worksheet> worksheetList = new ArrayList<>();
    private List<Account> workerList = new ArrayList<>();
    private Plan plan;
    List<String> workers = new ArrayList<>();
    String url = "jdbc:postgresql://db.wxxhmqjeruggsslfbkhs.supabase.co/postgres";
    String user = "postgres"; // Replace with your DB username
    String password = "8hlUWjTUakLNou2C"; // Replace with your DB password

    public void initialize() {
        loadDataFromAccountsDatabaseToAddChoiceBox();
        loadDataFromAccountsDatabase();
        loadDataFromLoggingInCSV();
        loadDataFromWorksheetsDatabase();
        loginNameLabel.setText(loginAccount.getName());
        addedLabel.setText("");
        loadDataFromWorksDatabase();
        loadDataFromSelectedPlanCSV();
        workersLabel.setText("-");
    }

    public void loadDataFromWorksheetsDatabase() {
        String sqlSelect = "SELECT worksheet_id, work_id, worker_username, worksheet_description, worksheet_photo, worksheet_status FROM worksheets";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = conn.prepareStatement(sqlSelect);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String worksheetID = rs.getString("worksheet_id");
                String workID = rs.getString("work_id");
                String username = rs.getString("worker_username");
                String details = rs.getString("worksheet_description");
                String photo = rs.getString("worksheet_photo");
                String status = rs.getString("worksheet_status");

                Worksheet worksheet = new Worksheet(worksheetID, workID, username, details, photo, status);
                worksheetList.add(worksheet);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle or log the exception as needed
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

    public void loadDataFromWorksDatabase() {
        String sqlSelect = "SELECT work_id, work_name, work_start_date, work_end_date, plan_id, work_description, work_status FROM works";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = conn.prepareStatement(sqlSelect);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("work_id");
                String name = rs.getString("work_name");
                String startDate = rs.getString("work_start_date");
                String endDate = rs.getString("work_end_date");
                String planID = rs.getString("plan_id");
                String details = rs.getString("work_description");
                String status = rs.getString("work_status");

                Work work = new Work(id, name, startDate, endDate, planID, details, status);
                workList.add(work);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle or log the exception as needed
        }
    }

    public void loadDataFromAccountsDatabaseToAddChoiceBox() {
        String sqlSelect = "SELECT name FROM accounts WHERE is_admin = false";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = conn.prepareStatement(sqlSelect);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("name");
                workersChoiceBox.getItems().add(name);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle or log the exception as needed
        }
    }


    public void loadDataFromAccountsDatabase() {
        String sqlSelect = "SELECT username, name, password, is_admin FROM accounts WHERE is_admin = false";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = conn.prepareStatement(sqlSelect);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                String name = rs.getString("name");
                String password = rs.getString("password");
                boolean isAdmin = rs.getBoolean("is_admin");

                Account worker = new Account(username, name, password, isAdmin);
                workerList.add(worker);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle or log the exception as needed
        }
    }

    public void saveDataToWorksDatabase(Work work) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String sql = "INSERT INTO works (work_id, work_name, work_start_date, work_end_date, plan_id, work_description, work_status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, work.getId());
                preparedStatement.setString(2, work.getName());
                preparedStatement.setDate(3, java.sql.Date.valueOf(work.getStartDate()));
                preparedStatement.setDate(4, java.sql.Date.valueOf(work.getEndDate()));
                preparedStatement.setString(5, work.getPlanID());
                preparedStatement.setString(6, work.getDetails());
                preparedStatement.setString(7, work.getStatus());

                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's error-handling strategy
        }
    }

    public void saveDataToWorksheetsDatabase(Worksheet worksheet) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String sql = "INSERT INTO worksheets (worksheet_id, work_id, worker_username, worksheet_description, worksheet_photo, worksheet_status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, worksheet.getWorksheetID());
                preparedStatement.setString(2, worksheet.getWorkID());
                preparedStatement.setString(3, worksheet.getUsername());
                preparedStatement.setString(4, worksheet.getDetails());
                preparedStatement.setString(5, worksheet.getPhoto());
                preparedStatement.setString(6, worksheet.getStatus());

                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's error-handling strategy
        }
    }

    public void handleAddButtonAction() {
        int[] currentWorkId = {workList.stream()
                .mapToInt(work -> Integer.parseInt(work.getId()))
                .max()
                .orElse(0)};

        currentWorkId[0]++;

        String id = String.valueOf(currentWorkId[0]);
        String name = nameField.getText();
        LocalDate startDateValue = startDatePicker.getValue();
        LocalDate endDateValue = endDatePicker.getValue();
        String details = detailsArea.getText();

        LocalDate today = LocalDate.now();
        LocalDate planStartDate = LocalDate.parse(plan.getStartDate()); // Assuming plan's start date is in YYYY-MM-DD format
        LocalDate planEndDate = LocalDate.parse(plan.getEndDate()); // Assuming plan's end date is in YYYY-MM-DD format

        // Check if the generated ID already exists in the workList
        boolean isIdAlreadyUsed = workList.stream().anyMatch(work -> work.getId().equals(String.valueOf(currentWorkId[0])));


        if (id.isEmpty() || name.isEmpty() || details.isEmpty()) {
            addedLabel.setText("กรอกข้อมูลให้ครบถ้วน");
            addedLabel.setTextFill(Color.RED);
        } else if (startDateValue == null || endDateValue == null) {
            addedLabel.setText("กรุณาเลือกวันที่เริ่มและสิ้นสุด");
            addedLabel.setTextFill(Color.RED);
        } else if (startDateValue.isBefore(today)) {
            addedLabel.setText("วันที่เริ่มต้องไม่น้อยกว่าวันที่ปัจจุบัน");
            addedLabel.setTextFill(Color.RED);
        } else if (startDateValue.isBefore(planStartDate)) {
            addedLabel.setText("วันที่เริ่มต้องไม่น้อยกว่าวันที่เริ่มของแผน");
            addedLabel.setTextFill(Color.RED);
        } else if (startDateValue.isAfter(endDateValue)) {
            addedLabel.setText("วันที่เริ่มต้นต้องอยู่ก่อนวันที่สิ้นสุด");
            addedLabel.setTextFill(Color.RED);
        } else if (endDateValue.isAfter(planEndDate)) {
            addedLabel.setText("วันที่สิ้นสุดต้องอยู่หลังจากวันที่สิ้นสุดของแผน");
            addedLabel.setTextFill(Color.RED);
        } else if (workers.isEmpty()) {
            addedLabel.setText("กรุณาเลือกผู้รับงาน");
            addedLabel.setTextFill(Color.RED);
        } else if (isIdAlreadyUsed) {
            addedLabel.setText("รหัสงานซ้ำ กรุณาลองอีกครั้ง");
            addedLabel.setTextFill(Color.RED);
        } else {
            String startDate = startDateValue.toString();
            String endDate = endDateValue.toString();

            // All required fields are filled and start date is before end date, proceed with creating the work item
            Work newWork = new Work(id, name, startDate, endDate, plan.getId(), details, "ยังไม่เริ่มต้น");
            workList.add(newWork);
            saveDataToWorksDatabase(newWork);

            int i = 1;
            String username = null;
            for (String workerName : workers) {
                for (Account worker : workerList) {
                    if (Objects.equals(workerName, worker.getName())) {
                        username = worker.getUsername();
                    }
                }
                Worksheet newWorksheet = new Worksheet(id + "WS" + i, id, username, "-", "", "ยังไม่เสร็จ");
                worksheetList.add(newWorksheet);
                saveDataToWorksheetsDatabase(newWorksheet);
                i++;
            }
            addedLabel.setText("มอบหมายงานสำเร็จ");
            addedLabel.setTextFill(Color.GREEN);
            nameField.clear();
            detailsArea.clear();
            workers.clear();
            workersLabel.setText("-");
        }
    }

    public void handleAddWorkerButtonAction() {
        String worker = workersChoiceBox.getValue();
        if (worker != null) {
            workers.add(worker);
            String workersStr = new String();
            for (String workerStr : workers) {
                workersStr += workerStr + " | ";
            }
            workersLabel.setText(workersStr);
        }
    }

    public void loadDataFromSelectedPlanCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("csv/selected_plan.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) { // Assuming there are 6 fields in the CSV
                    String id = parts[0];
                    String name = parts[1];
                    String startDate = parts[2];
                    String endDate = parts[3];
                    String details = parts[4];
                    String status = parts[5];

                    plan = new Plan(id, name, startDate, endDate, details, status);
                }
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
}
