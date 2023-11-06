package com.example.project02.models;

public class Equipment {
    private String name;
    private int number;
    private int borrowing;

    public Equipment(String name, int number, int borrowing) {
        this.name = name;
        this.number = number;
        this.borrowing = borrowing;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getBorrowing() {
        return borrowing;
    }

    public void setBorrowing(int borrowing) {
        this.borrowing = borrowing;
    }
}