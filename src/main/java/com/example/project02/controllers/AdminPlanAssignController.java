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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminPlanAssignController {
    @FXML
    private Label loginNameLabel;
    @FXML
    private TextField idField;
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

    public void initialize() {
        loadDataFromAccountsCSVtoAddChoiceBox();
        loadDataFromAccountsCSV();
        loadDataFromLoggingInCSV();
        loadDataFromWorksheetsCSV();
        loginNameLabel.setText(loginAccount.getName());
        addedLabel.setText("");
        loadDataFromWorksCSV();
        loadDataFromSelectedPlanCSV();
        workersLabel.setText("-");
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

    public void loadDataFromWorksCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("csv/works.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 7) { // Assuming there are 6 fields in the CSV
                    String id = parts[0];
                    String name = parts[1];
                    String startDate = parts[2];
                    String endDate = parts[3];
                    String planID = parts[4];
                    String details = parts[5];
                    String status = parts[6];

                    Work work = new Work(id,name,startDate,endDate,planID,details,status);
                    workList.add(work);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDataToWorksCSV(List<Work> workList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("csv/works.csv"))) {
            for (Work work : workList) {
                writer.write(work.getId() + "," +
                        work.getName() + "," +
                        work.getStartDate() + "," +
                        work.getEndDate() + "," +
                        work.getPlanID() + "," +
                        work.getDetails() + "," +
                        work.getStatus());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDataFromAccountsCSVtoAddChoiceBox() {
        try (BufferedReader reader = new BufferedReader(new FileReader("csv/accounts.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) { // Assuming there are 4 fields in the CSV
                    String name = parts[1];
                    boolean isAdmin = Boolean.parseBoolean(parts[3]);
                    if (!isAdmin) workersChoiceBox.getItems().add(name);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDataFromAccountsCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("csv/accounts.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) { // Assuming there are 4 fields in the CSV
                    String username = parts[0];
                    String name = parts[1];
                    String password = parts[2];
                    boolean isAdmin = Boolean.parseBoolean(parts[3]);
                    if (!isAdmin) {
                        Account worker = new Account(username,name,password,isAdmin);
                        workerList.add(worker);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleAddButtonAction() {
        String id = idField.getText();
        String name = nameField.getText();
        LocalDate startDateValue = startDatePicker.getValue();
        LocalDate endDateValue = endDatePicker.getValue();
        String details = detailsArea.getText();

        // Check if the id is already in use
        boolean isIdAlreadyUsed = workList.stream().anyMatch(work -> work.getId().equals(id));

        if (id.isEmpty() || name.isEmpty() || details.isEmpty()) {
            addedLabel.setText("กรอกข้อมูลให้ครบถ้วน");
            addedLabel.setTextFill(Color.RED);
        } else if (startDateValue == null || endDateValue == null) {
            addedLabel.setText("กรุณาเลือกวันที่เริ่มและสิ้นสุด");
            addedLabel.setTextFill(Color.RED);
        } else if (startDateValue.isAfter(endDateValue)) {
            addedLabel.setText("วันที่เริ่มต้นต้องอยู่ก่อนวันที่สิ้นสุด");
            addedLabel.setTextFill(Color.RED);
        } else if (workers.isEmpty()) {
            addedLabel.setText("กรุณาเลือกผู้รับงาน");
            addedLabel.setTextFill(Color.RED);
        } else if (isIdAlreadyUsed) {
            addedLabel.setText("กรุณากรอก ID ใหม่ เนื่องจากซ้ำกับงานอื่น");
            addedLabel.setTextFill(Color.RED);
        } else {
            String startDate = startDateValue.toString();
            String endDate = endDateValue.toString();

            // All required fields are filled and start date is before end date, proceed with creating the work item
            Work newWork = new Work(id, name, startDate, endDate, plan.getId(), details, "ยังไม่เริ่มต้น");
            workList.add(newWork);
            saveDataToWorksCSV(workList);

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
                i++;
            }
            saveDataToWorksheetsCSV(worksheetList);

            addedLabel.setText("มอบหมายงานสำเร็จ");
            addedLabel.setTextFill(Color.GREEN);
            idField.clear();
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
