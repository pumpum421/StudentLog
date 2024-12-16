package com.example.studentlog;

import com.example.studentlog.domain.Student;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Date;

public class StudentView {
    private final DatabaseService databaseService;

    public StudentView(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public void show(Stage stage) {
        Label instructionLabel = new Label("Выберите студента:");
        ListView<Student> studentListView = new ListView<>();
        Button backButton = new Button("Назад");

        backButton.setOnAction(event -> new App().start(stage)); // Возврат в выбор роли

        try {
            studentListView.getItems().addAll(databaseService.getAllStudents());
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка: " + e.getMessage()).showAndWait();
        }

        studentListView.setOnMouseClicked(event -> {
            Student selectedStudent = studentListView.getSelectionModel().getSelectedItem();
            if (selectedStudent != null) {
                showStudentDetails(stage, selectedStudent);
            }
        });

        VBox layout = new VBox(10, backButton, instructionLabel, studentListView);
        layout.setPadding(new Insets(10));
        Scene scene = new Scene(layout, 400, 400);

        stage.setScene(scene);
        stage.show();
    }

    private void showStudentDetails(Stage stage, Student student) {
        VBox layout = new VBox(10);
        Label studentInfo = new Label("Данные для студента: " + student.getName() + " " + student.getSurname());
        TableView<WorkRow> tableView = new TableView<>();
        Button backButton = new Button("Назад");

        backButton.setOnAction(event -> show(stage)); // Возврат в список студентов

        // Настройка колонок таблицы
        TableColumn<WorkRow, String> typeColumn = new TableColumn<>("Тип");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<WorkRow, String> subjectColumn = new TableColumn<>("Предмет");
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));

        TableColumn<WorkRow, Date> dateColumn = new TableColumn<>("Дата");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<WorkRow, Boolean> attendedColumn = new TableColumn<>("Посещено");
        attendedColumn.setCellValueFactory(new PropertyValueFactory<>("attended"));

        TableColumn<WorkRow, Integer> gradeColumn = new TableColumn<>("Оценка");
        gradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));

        tableView.getColumns().addAll(typeColumn, subjectColumn, dateColumn, attendedColumn, gradeColumn);

        // Загрузка данных студента
        try {
            tableView.getItems().addAll(databaseService.getAllWorksByStudent(student.getId()));
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка при загрузке данных: " + e.getMessage()).showAndWait();
        }

        layout.getChildren().addAll(backButton, studentInfo, tableView);
        Scene scene = new Scene(layout, 600, 400);

        stage.setScene(scene);
        stage.show();
    }
}