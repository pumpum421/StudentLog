package com.example.studentlog;

import com.example.studentlog.domain.Student;
import org.h2.tools.Server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class H2DatabaseManager {
    private Server server;
    private Connection connection;
    private static final String DB_PATH = System.getProperty("user.home") + "/student_academic_performance";

    public void startServer() {
        try {
            server = Server.createTcpServer("-tcpPort", "9092", "-tcpAllowOthers").start();
            System.out.println("H2 server started on port 9092");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to start H2 server", e);
        }
    }

    public void stopServer() {
        if (server != null) {
            server.stop();
            System.out.println("H2 server stopped");
        }
    }

    public Connection connectToDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:h2:tcp://localhost:9092/" + DB_PATH, "sa", "");
            System.out.println("Connected to the database");
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to the database", e);
        }
    }

    public boolean databaseFileExists() {
        File dbFile = new File(DB_PATH + ".mv.db");
        return dbFile.exists();
    }

    public void initializeDatabase() {
        try (var stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Students (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    student_name VARCHAR(100),
                    student_surname VARCHAR(100),
                    student_patronymic VARCHAR(100),
                    group_name VARCHAR(100)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS PracticalWork (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    student_id BIGINT NOT NULL,
                    practicalwork_date DATE NOT NULL,
                    grade TINYINT DEFAULT NULL,
                    subject_name VARCHAR(100),
                    attended BOOLEAN,
                    CONSTRAINT grade_ch CHECK (grade <= 5),
                    FOREIGN KEY(student_id) REFERENCES Students(id)
                    ON UPDATE CASCADE
                    ON DELETE CASCADE
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS LaboratoryWork (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    student_id BIGINT NOT NULL,
                    laboratorywork_date DATE NOT NULL,
                    grade TINYINT DEFAULT NULL,
                    subject_name VARCHAR(100),
                    attended BOOLEAN,
                    CONSTRAINT grade_ch_l CHECK (grade <= 5),
                    FOREIGN KEY(student_id) REFERENCES Students(id)
                    ON UPDATE CASCADE
                    ON DELETE CASCADE
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Lectures (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    student_id BIGINT NOT NULL,
                    lecture_date DATE NOT NULL,
                    subject_name VARCHAR(100),
                    attended BOOLEAN,
                    FOREIGN KEY(student_id) REFERENCES Students(id)
                    ON UPDATE CASCADE
                    ON DELETE CASCADE
                );
            """);

            System.out.println("Database schema initialized");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize the database schema", e);
        }
    }

    public void populateDatabase() {
        try (Statement stmt = connection.createStatement()) {
            // Заполнение таблицы Students
            stmt.executeUpdate("""
                INSERT INTO Students (student_name, student_surname, student_patronymic, group_name)
                VALUES ('Иван', 'Иванов', 'Иванович', 'Группа 101'),
                       ('Петр', 'Петров', 'Петрович', 'Группа 102'),
                       ('Мария', 'Сидорова', 'Алексеевна', 'Группа 101');
            """);

            // Заполнение таблицы PracticalWork
            stmt.executeUpdate("""
                INSERT INTO PracticalWork (student_id, practicalwork_date, grade, subject_name, attended)
                VALUES (1, '2024-12-10', 4, 'Математика', TRUE),
                       (2, '2024-12-11', 5, 'Физика', TRUE),
                       (3, '2024-12-12', NULL, 'Информатика', FALSE);
            """);

            // Заполнение таблицы LaboratoryWork
            stmt.executeUpdate("""
                INSERT INTO LaboratoryWork (student_id, laboratorywork_date, grade, subject_name, attended)
                VALUES (1, '2024-12-13', 3, 'Химия', TRUE),
                       (2, '2024-12-14', 4, 'Биология', TRUE),
                       (3, '2024-12-15', NULL, 'География', FALSE);
            """);

            // Заполнение таблицы Lectures
            stmt.executeUpdate("""
                INSERT INTO Lectures (student_id, lecture_date, subject_name, attended)
                VALUES (1, '2024-12-10', 'История', TRUE),
                       (2, '2024-12-11', 'Литература', FALSE),
                       (3, '2024-12-12', 'Философия', TRUE);
            """);

            System.out.println("Database populated with initial data");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to populate the database", e);
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close the database connection", e);
        }
    }
}