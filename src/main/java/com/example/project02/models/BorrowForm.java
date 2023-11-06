package com.example.project02.models;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BorrowForm {
    private String id;
    private String borrower;
    private String equipmentName;
    private String borrowDate; // Represented as a String
    private int borrowNumber;
    private int returnedNumber;
    private String status;

    public BorrowForm(String id, String borrower, String equipmentName, String borrowDate, int borrowNumber, int returnedNumber, String status) {
        this.id = id;
        this.borrower = borrower;
        this.equipmentName = equipmentName;
        this.borrowDate = borrowDate;
        this.borrowNumber = borrowNumber;
        this.returnedNumber = returnedNumber;
        this.status = status;
    }

    public BorrowForm(String borrower, String equipmentName, int borrowNumber, int returnedNumber, String status) {
        this.id = generateUniqueId(); // Generate a unique ID
        this.borrower = borrower;
        this.equipmentName = equipmentName;
        this.borrowDate = getCurrentTime();
        this.borrowNumber = borrowNumber;
        this.returnedNumber = returnedNumber;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getBorrower() {
        return borrower;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public String getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate() {
        this.borrowDate = getCurrentTime();
    }

    public int getBorrowNumber() {
        return borrowNumber;
    }

    public void setBorrowNumber(int borrowNumber) {
        this.borrowNumber = borrowNumber;
    }

    public int getReturnedNumber() {
        return returnedNumber;
    }

    public void setReturnedNumber(int returnedNumber) {
        this.returnedNumber = returnedNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String generateUniqueId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date now = new Date();
        return sdf.format(now);
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        return sdf.format(now);
    }
}
