package com.example.studentlog;

import com.example.studentlog.domain.Student;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Date;

public class TeacherView {
    private final DatabaseService databaseService;

    public TeacherView(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public void show(Stage stage) {
        Label instructions = new Label("Выберите действие:");
        Button editGradesButton = new Button("Редактировать оценки и посещения");
        Button addWorkButton = new Button("Добавить работу");
        Button calculateStatsButton = new Button("Показать статистику");
        Button backButton = new Button("Назад");

        editGradesButton.setOnAction(event -> openEditGrades(stage));
        addWorkButton.setOnAction(event -> openAddWork(stage));
        calculateStatsButton.setOnAction(event -> openCalculateStats(stage));
        backButton.setOnAction(event -> new App().start(stage));

        VBox layout = new VBox(10, instructions, editGradesButton, addWorkButton, calculateStatsButton, backButton);
        layout.setPadding(new Insets(10));
        Scene scene = new Scene(layout, 300, 200);

        stage.setScene(scene);
        stage.show();
    }


    private void openEditGrades(Stage stage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label instructionLabel = new Label("Выберите студента для редактирования:");
        ListView<String> studentListView = new ListView<>();
        Button backButton = new Button("Назад");

        backButton.setOnAction(event -> show(stage));

        try {
            studentListView.getItems().addAll(databaseService.getFormattedStudentList());
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка: " + e.getMessage()).showAndWait();
        }

        studentListView.setOnMouseClicked(event -> {
            String selectedStudent = studentListView.getSelectionModel().getSelectedItem();
            if (selectedStudent != null) {
                long studentId = databaseService.extractStudentIdFromFormattedString(selectedStudent);
                openEditGradesForStudent(stage, studentId);
            }
        });

        layout.getChildren().addAll(backButton, instructionLabel, studentListView);
        Scene scene = new Scene(layout, 500, 600);
        stage.setScene(scene);
        stage.show();
    }
    private void openEditGradesForStudent(Stage stage, long studentId) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label studentInfo = new Label("Редактирование данных для студента ID: " + studentId);
        TableView<WorkRow> tableView = new TableView<>();
        Button saveButton = new Button("Сохранить изменения");
        Button backButton = new Button("Назад");

        backButton.setOnAction(event -> openEditGrades(stage));

        // Настройка колонок таблицы
        TableColumn<WorkRow, String> typeColumn = new TableColumn<>("Тип");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<WorkRow, String> subjectColumn = new TableColumn<>("Предмет");
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));

        TableColumn<WorkRow, Date> dateColumn = new TableColumn<>("Дата");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<WorkRow, Boolean> attendedColumn = new TableColumn<>("Посещено");
        attendedColumn.setCellValueFactory(new PropertyValueFactory<>("attended"));
        attendedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(attendedColumn));
        attendedColumn.setOnEditCommit(event -> {
            WorkRow row = event.getRowValue();
            row.setAttended(event.getNewValue());
        });

        TableColumn<WorkRow, Integer> gradeColumn = new TableColumn<>("Оценка");
        gradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));
        gradeColumn.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));
        gradeColumn.setOnEditCommit(event -> {
            WorkRow row = event.getRowValue();
            row.setGrade(event.getNewValue());
        });

        tableView.getColumns().addAll(typeColumn, subjectColumn, dateColumn, attendedColumn, gradeColumn);

        // Включение редактирования таблицы
        tableView.setEditable(true);

        // Загрузка данных студента
        try {
            tableView.getItems().addAll(databaseService.getAllWorksByStudent(studentId));
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка при загрузке данных: " + e.getMessage()).showAndWait();
        }

        saveButton.setOnAction(event -> {
            for (WorkRow row : tableView.getItems()) {
                try {
                    databaseService.updateWorkAttendanceAndGrade(
                            row.getTable(), row.getId(), row.getGrade(), row.isAttended()
                    );
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Ошибка при сохранении: " + e.getMessage()).showAndWait();
                }
            }
            new Alert(Alert.AlertType.INFORMATION, "Изменения сохранены").showAndWait();
        });

        layout.getChildren().addAll(backButton, studentInfo, tableView, saveButton);
        Scene scene = new Scene(layout, 600, 400);

        stage.setScene(scene);
        stage.show();
    }

    private void openAddWork(Stage stage) {
        VBox layout = new VBox(10);
        Button backButton = new Button("Назад");

        backButton.setOnAction(event -> show(stage));

        Label typeLabel = new Label("Выберите тип работы:");
        ChoiceBox<String> workType = new ChoiceBox<>();
        workType.getItems().addAll("Практическая", "Лабораторная", "Лекция");

        Label subjectLabel = new Label("Введите название предмета:");
        TextField subjectField = new TextField();

        Label dateLabel = new Label("Выберите дату проведения:");
        DatePicker datePicker = new DatePicker();

        Label groupLabel = new Label("Введите группу:");
        TextField groupField = new TextField();

        Button addButton = new Button("Добавить");

        addButton.setOnAction(event -> {
            String type = workType.getValue();
            String subject = subjectField.getText();
            String group = groupField.getText();
            Date date = datePicker.getValue() != null ? Date.valueOf(datePicker.getValue()) : null;

            if (type != null && !subject.isBlank() && date != null && !group.isBlank()) {
                try {
                    databaseService.addNewWorkForGroup(type, subject, date, group);
                    new Alert(Alert.AlertType.INFORMATION, "Работа добавлена!").showAndWait();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Ошибка: " + e.getMessage()).showAndWait();
                }
            } else {
                new Alert(Alert.AlertType.WARNING, "Заполните все поля!").showAndWait();
            }
        });

        layout.getChildren().addAll(backButton, typeLabel, workType, subjectLabel, subjectField, dateLabel, datePicker, groupLabel, groupField, addButton);
        Scene scene = new Scene(layout, 400, 400);

        stage.setScene(scene);
        stage.show();
    }

    private void openCalculateStats(Stage stage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Button backButton = new Button("Назад");
        backButton.setOnAction(event -> show(stage));

        Label studentOrGroupLabel = new Label("Выберите студента или группу:");
        ChoiceBox<String> studentOrGroupChoice = new ChoiceBox<>();
        studentOrGroupChoice.getItems().addAll("Студент", "Группа");

        Label groupLabel = new Label("Выберите группу:");
        ComboBox<String> groupComboBox = new ComboBox<>();
        groupComboBox.setDisable(true);

        Label studentLabel = new Label("Выберите студента:");
        ComboBox<String> studentComboBox = new ComboBox<>();
        studentComboBox.setDisable(true);

        Label subjectLabel = new Label("Выберите предмет:");
        ComboBox<String> subjectComboBox = new ComboBox<>();

        Button calculateButton = new Button("Рассчитать статистику");
        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);

        studentOrGroupChoice.setOnAction(event -> {
            if (studentOrGroupChoice.getValue().equals("Группа")) {
                groupComboBox.setDisable(false);
                studentComboBox.setDisable(true);
                subjectComboBox.getItems().clear();

                try {
                    groupComboBox.getItems().clear();
                    groupComboBox.getItems().addAll(databaseService.getAllGroups());
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Ошибка при загрузке групп: " + e.getMessage()).showAndWait();
                }
            } else if (studentOrGroupChoice.getValue().equals("Студент")) {
                groupComboBox.setDisable(true);
                studentComboBox.setDisable(false);
                subjectComboBox.getItems().clear();

                try {
                    studentComboBox.getItems().clear();
                    studentComboBox.getItems().addAll(databaseService.getFormattedStudentList());
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Ошибка при загрузке студентов: " + e.getMessage()).showAndWait();
                }
            }
        });

        // Подгрузка предметов при выборе группы
        groupComboBox.setOnAction(event -> {
            String group = groupComboBox.getValue();
            if (group != null) {
                try {
                    subjectComboBox.getItems().clear();
                    subjectComboBox.getItems().addAll(databaseService.getSubjectsByGroupOrStudent(group, null));
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Ошибка при загрузке предметов: " + e.getMessage()).showAndWait();
                }
            }
        });

        // Подгрузка предметов при выборе студента
        studentComboBox.setOnAction(event -> {
            String selectedStudent = studentComboBox.getValue();
            if (selectedStudent != null) {
                long studentId = databaseService.extractStudentIdFromFormattedString(selectedStudent);
                try {
                    subjectComboBox.getItems().clear();
                    subjectComboBox.getItems().addAll(databaseService.getSubjectsByGroupOrStudent(null, studentId));
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Ошибка при загрузке предметов: " + e.getMessage()).showAndWait();
                }
            }
        });

        calculateButton.setOnAction(event -> {
            String subject = subjectComboBox.getValue();
            String group = groupComboBox.isDisabled() ? null : groupComboBox.getValue();
            String student = studentComboBox.isDisabled() ? null : studentComboBox.getValue();

            if (subject == null || (group == null && student == null)) {
                new Alert(Alert.AlertType.WARNING, "Выберите предмет и студента или группу!").showAndWait();
                return;
            }

            try {
                String stats;
                if (group != null) {
                    stats = databaseService.calculateStatisticsByGroup(group, subject);
                } else {
                    stats = databaseService.calculateStatisticsByStudent(student, subject);
                }
                resultArea.setText(stats);
            } catch (Exception e) {
                resultArea.setText("Ошибка: " + e.getMessage());
            }
        });

        layout.getChildren().addAll(
                backButton,
                studentOrGroupLabel, studentOrGroupChoice,
                groupLabel, groupComboBox,
                studentLabel, studentComboBox,
                subjectLabel, subjectComboBox,
                calculateButton, resultArea
        );

        Scene scene = new Scene(layout, 500, 600);
        stage.setScene(scene);
        stage.show();
    }
}

