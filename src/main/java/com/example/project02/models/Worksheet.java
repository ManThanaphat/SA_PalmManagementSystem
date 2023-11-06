package com.example.project02.models;

public class Worksheet {
    private String worksheetID;
    private String workID;
    private String username;
    private String details;
    private String photo;
    private String status;

    public Worksheet(String worksheetID, String workID, String username, String details, String photo, String status) {
        this.worksheetID = worksheetID;
        this.workID = workID;
        this.username = username;
        this.details = details;
        this.photo = photo;
        this.status = status;
    }

    public String getWorksheetID() {
        return worksheetID;
    }

    // Getters
    public String getWorkID() {
        return workID;
    }

    public String getDetails() {
        return details;
    }

    public String getPhoto() {
        return photo;
    }

    public String getStatus() {
        return status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
