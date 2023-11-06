package com.example.project02.models;

public class Work {
    private String id;
    private String name;
    private String startDate;
    private String endDate;
    private String planID; // Change plan to planID
    private String details;
    private String status;

    public Work(String id, String name, String startDate, String endDate, String planID, String details, String status) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.planID = planID;
        this.status = status;
        this.details = details;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getPlanID() {
        return planID;
    }

    public String getStatus() {
        return status;
    }

    public String getDetails() {
        return details;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}





