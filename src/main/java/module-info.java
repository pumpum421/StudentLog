module com.example.studentlog {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.h2database;


    opens com.example.studentlog to javafx.fxml;
    exports com.example.studentlog;
    exports com.example.studentlog.domain;
    opens com.example.studentlog.domain to javafx.fxml;
}