package lms.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection - Singleton class to manage MySQL connection
 * Demonstrates: Encapsulation, Static methods
 */
public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/lms_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = ""; // Change this

    private static Connection connection = null;

    // Private constructor - no one can instantiate this class
    private DatabaseConnection() {}

    /**
     * Returns a single shared connection (Singleton pattern)
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Database connected successfully!");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found. Add mysql-connector-java.jar to your project.", e);
            }
        }
        return connection;
    }

    /**
     * Close the connection (call when app shuts down)
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
