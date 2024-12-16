package com.example.studentlog;

import com.example.studentlog.domain.LaboratoryWork;
import com.example.studentlog.domain.Lecture;
import com.example.studentlog.domain.PracticalWork;
import com.example.studentlog.domain.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {
    private Connection connection;

    public DatabaseService(Connection connection) {
        this.connection = connection;
    }


    public List<String> getAllSubjects() throws SQLException {
        List<String> subjects = new ArrayList<>();
        String query = """
        SELECT DISTINCT subject_name FROM (
            SELECT subject_name FROM PracticalWork
            UNION ALL
            SELECT subject_name FROM LaboratoryWork
            UNION ALL
            SELECT subject_name FROM Lectures
        ) AS all_subjects;
    """;

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                subjects.add(rs.getString("subject_name"));
            }
        }
        return subjects;
    }
    public List<Student> getAllStudents() throws SQLException {
        String query = "SELECT * FROM Students";
        List<Student> students = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                students.add(new Student(
                        rs.getLong("id"),
                        rs.getString("student_name"),
                        rs.getString("student_surname"),
                        rs.getString("student_patronymic"),
                        rs.getString("group_name")
                ));
            }
        }
        return students;
    }
    public void addNewWorkForGroup(String type, String subject, Date date, String group) throws SQLException {
        String table;
        switch (type) {
            case "Практическая" -> table = "PracticalWork";
            case "Лабораторная" -> table = "LaboratoryWork";
            case "Лекция" -> table = "Lectures";
            default -> throw new IllegalArgumentException("Неизвестный тип работы: " + type);
        }

        String dateColumn = switch (type) {
            case "Практическая" -> "practicalwork_date";
            case "Лабораторная" -> "laboratorywork_date";
            case "Лекция" -> "lecture_date";
            default -> throw new IllegalArgumentException("Неизвестный тип работы: " + type);
        };

        String query = "INSERT INTO " + table + " (student_id, " + dateColumn + ", subject_name, attended) " +
                "SELECT id, ?, ?, FALSE FROM Students WHERE group_name = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, date);
            stmt.setString(2, subject);
            stmt.setString(3, group);
            stmt.executeUpdate();
        }
    }
    public List<String> getFormattedStudentList() throws SQLException {
        List<String> formattedStudents = new ArrayList<>();
        String query = "SELECT id, group_name, student_name, student_surname FROM Students";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                formattedStudents.add("Группа " + rs.getString("group_name") + ": " +
                        rs.getString("student_name") + " " + rs.getString("student_surname"));
            }
        }
        return formattedStudents;
    }
    public List<String> getAllGroups() throws SQLException {
        List<String> groups = new ArrayList<>();
        String query = "SELECT DISTINCT group_name FROM Students";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                groups.add(rs.getString("group_name"));
            }
        }
        return groups;
    }
    public long extractStudentIdFromFormattedString(String formattedStudent) {
        String studentName = formattedStudent.split(":")[1].trim();
        String query = "SELECT id FROM Students WHERE CONCAT(student_name, ' ', student_surname) = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, studentName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при извлечении ID студента: " + e.getMessage());
        }
        throw new RuntimeException("Студент не найден!");
    }
    public String calculateStatisticsByGroup(String groupName, String subjectName) throws SQLException {
        String query = """
        SELECT 
            AVG(CASE WHEN grade IS NOT NULL THEN grade END) AS avg_grade,
            100.0 * SUM(CASE WHEN attended THEN 1 ELSE 0 END) / COUNT(*) AS attendance_rate
        FROM (
            SELECT grade, attended FROM PracticalWork 
            WHERE subject_name = ? AND student_id IN (SELECT id FROM Students WHERE group_name = ?)
            UNION ALL
            SELECT grade, attended FROM LaboratoryWork 
            WHERE subject_name = ? AND student_id IN (SELECT id FROM Students WHERE group_name = ?)
            UNION ALL
            SELECT NULL AS grade, attended FROM Lectures 
            WHERE subject_name = ? AND student_id IN (SELECT id FROM Students WHERE group_name = ?)
        ) AS stats;
    """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, subjectName);
            stmt.setString(2, groupName);
            stmt.setString(3, subjectName);
            stmt.setString(4, groupName);
            stmt.setString(5, subjectName);
            stmt.setString(6, groupName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double avgGrade = rs.getDouble("avg_grade");
                    double attendanceRate = rs.getDouble("attendance_rate");
                    return String.format("Группа: %s\nСредняя оценка: %.2f\nПроцент посещений: %.2f%%",
                            groupName, avgGrade, attendanceRate);
                }
            }
        }
        return "Нет данных для указанной группы и предмета.";
    }
    public String calculateStatisticsByStudent(String formattedStudent, String subjectName) throws SQLException {
        long studentId = extractStudentIdFromFormattedString(formattedStudent);

        String query = """
        SELECT 
            AVG(CASE WHEN grade IS NOT NULL THEN grade END) AS avg_grade,
            100.0 * SUM(CASE WHEN attended THEN 1 ELSE 0 END) / COUNT(*) AS attendance_rate
        FROM (
            SELECT grade, attended FROM PracticalWork 
            WHERE subject_name = ? AND student_id = ?
            UNION ALL
            SELECT grade, attended FROM LaboratoryWork 
            WHERE subject_name = ? AND student_id = ?
            UNION ALL
            SELECT NULL AS grade, attended FROM Lectures 
            WHERE subject_name = ? AND student_id = ?
        ) AS stats;
    """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, subjectName);
            stmt.setLong(2, studentId);
            stmt.setString(3, subjectName);
            stmt.setLong(4, studentId);
            stmt.setString(5, subjectName);
            stmt.setLong(6, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double avgGrade = rs.getDouble("avg_grade");
                    double attendanceRate = rs.getDouble("attendance_rate");
                    return String.format("Студент: %s\nСредняя оценка: %.2f\nПроцент посещений: %.2f%%",
                            formattedStudent, avgGrade, attendanceRate);
                }
            }
        }
        return "Нет данных для указанного студента и предмета.";
    }

    public List<WorkRow> getAllWorksByStudent(long studentId) throws SQLException {
        List<WorkRow> results = new ArrayList<>();
        String query = """
        SELECT 'PracticalWork' AS table_name, id, 'Практическая' AS work_type, practicalwork_date AS work_date, subject_name, grade, attended
        FROM PracticalWork WHERE student_id = ?
        UNION ALL
        SELECT 'LaboratoryWork', id, 'Лабораторная', laboratorywork_date, subject_name, grade, attended
        FROM LaboratoryWork WHERE student_id = ?
        UNION ALL
        SELECT 'Lectures', id, 'Лекция', lecture_date, subject_name, NULL AS grade, attended
        FROM Lectures WHERE student_id = ?
    """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, studentId);
            stmt.setLong(2, studentId);
            stmt.setLong(3, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new WorkRow(
                            rs.getLong("id"),
                            rs.getString("table_name"),
                            rs.getString("work_type"),
                            rs.getString("subject_name"),
                            rs.getDate("work_date"),
                            rs.getBoolean("attended"),
                            rs.getObject("grade", Integer.class)
                    ));
                }
            }
        }
        return results;
    }


    public void updateWorkAttendanceAndGrade(String table, long workId, Integer grade, boolean attended) throws SQLException {
        String gradeField = table.equals("Lectures") ? null : "grade";
        String query = "UPDATE " + table + " SET attended = ?" +
                (gradeField != null ? ", grade = ?" : "") +
                " WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setBoolean(1, attended);
            if (gradeField != null) {
                stmt.setObject(2, grade, Types.INTEGER);
                stmt.setLong(3, workId);
            } else {
                stmt.setLong(2, workId);
            }
            stmt.executeUpdate();
        }
    }
    public List<String> getSubjectsByGroupOrStudent(String groupName, Long studentId) throws SQLException {
        List<String> subjects = new ArrayList<>();
        String query;

        if (studentId != null) {
            query = """
            SELECT DISTINCT subject_name FROM (
                SELECT subject_name FROM PracticalWork WHERE student_id = ?
                UNION ALL
                SELECT subject_name FROM LaboratoryWork WHERE student_id = ?
                UNION ALL
                SELECT subject_name FROM Lectures WHERE student_id = ?
            ) AS student_subjects;
        """;
        } else if (groupName != null) {
            query = """
            SELECT DISTINCT subject_name FROM (
                SELECT subject_name FROM PracticalWork 
                WHERE student_id IN (SELECT id FROM Students WHERE group_name = ?)
                UNION ALL
                SELECT subject_name FROM LaboratoryWork 
                WHERE student_id IN (SELECT id FROM Students WHERE group_name = ?)
                UNION ALL
                SELECT subject_name FROM Lectures 
                WHERE student_id IN (SELECT id FROM Students WHERE group_name = ?)
            ) AS group_subjects;
        """;
        } else {
            throw new IllegalArgumentException("Необходимо указать либо группу, либо студента.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            if (studentId != null) {
                stmt.setLong(1, studentId);
                stmt.setLong(2, studentId);
                stmt.setLong(3, studentId);
            } else {
                stmt.setString(1, groupName);
                stmt.setString(2, groupName);
                stmt.setString(3, groupName);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    subjects.add(rs.getString("subject_name"));
                }
            }
        }
        return subjects;
    }
}