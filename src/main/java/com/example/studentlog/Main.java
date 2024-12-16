package com.example.studentlog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    private static H2DatabaseManager dbManager;
    private static DatabaseService databaseService;

    public static void main(String[] args) throws SQLException {
        try {
            dbManager = new H2DatabaseManager();
            dbManager.startServer();

            if (!dbManager.databaseFileExists()) {
                System.out.println("Database file not found. Initializing new database...");
                try (Connection connection = DriverManager.getConnection("jdbc:h2:~/student_academic_performance", "sa", "")) {
                    System.out.println("Database created locally");
                }
                Connection connection = dbManager.connectToDatabase();
                dbManager.initializeDatabase();
                dbManager.populateDatabase();
                databaseService = new DatabaseService(connection);
            } else {
                System.out.println("Database file found. Connecting...");
                Connection connection = dbManager.connectToDatabase();
                databaseService = new DatabaseService(connection);
            }

            App.launch(App.class, args);
        } finally {
            if (dbManager != null) {
                dbManager.closeConnection();
                dbManager.stopServer();
            }
        }
    }

    public static DatabaseService getDatabaseService() {
        return databaseService;
    }
}

