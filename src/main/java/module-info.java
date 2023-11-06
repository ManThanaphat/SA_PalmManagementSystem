module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.project02 to javafx.fxml;
    exports com.example.project02;
    exports com.example.project02.controllers;
    opens com.example.project02.controllers to javafx.fxml;

    opens com.example.project02.models to javafx.base; // Open the package to javafx.base

}