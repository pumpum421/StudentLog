package com.example.studentlog;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Учет посещений и оценок");

        // Создаем элементы интерфейса
        Label roleLabel = new Label("Выберите роль:");
        Button teacherButton = new Button("Преподаватель");
        Button studentButton = new Button("Студент");

        // События нажатия кнопок
        teacherButton.setOnAction(event -> openTeacherView(primaryStage));
        studentButton.setOnAction(event -> openStudentView(primaryStage));

        // Оформление стартового экрана
        VBox layout = new VBox(10, roleLabel, teacherButton, studentButton);
        layout.setPadding(new Insets(10));
        Scene scene = new Scene(layout, 300, 200);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openTeacherView(Stage stage) {
        TeacherView teacherView = new TeacherView(Main.getDatabaseService());
        teacherView.show(stage);
    }

    private void openStudentView(Stage stage) {
        StudentView studentView = new StudentView(Main.getDatabaseService());
        studentView.show(stage);
    }
}
