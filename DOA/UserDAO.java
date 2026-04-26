package lms.dao;

import lms.model.*;
import lms.util.DatabaseConnection;

import java.sql.*;

/**
 * UserDAO - Data Access Object for user operations
 * Demonstrates: Database Handling, Encapsulation, Error & Exception Handling
 */
public class UserDAO {

    /**
     * Login: checks username + password, returns the correct User subclass
     * Demonstrates: Polymorphism (returns different subclass based on role)
     */
    public User login(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                int userId  = rs.getInt("id");

                // Return the correct subclass based on role
                switch (role) {
                    case "ADMIN":
                        return new Admin(
                            userId,
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("phone")
                        );

                    case "LECTURER":
                        return getLecturerUser(rs);

                    case "STUDENT":
                        return getStudentUser(rs);

                    case "TECHNICAL_OFFICER":
                        return getTechnicalOfficerUser(rs);

                    default:
                        throw new SQLException("Unknown role: " + role);
                }
            }
            return null; // login failed
        }
    }

    // ---- Private helpers ----

    private Lecturer getLecturerUser(ResultSet rs) throws SQLException {
        int userId = rs.getInt("id");
        String sql2 = "SELECT * FROM lecturers WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
            stmt2.setInt(1, userId);
            ResultSet rs2 = stmt2.executeQuery();
            if (rs2.next()) {
                return new Lecturer(
                    userId,
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs2.getInt("id"),
                    rs2.getString("department"),
                    rs2.getString("specialization")
                );
            }
        }
        return null;
    }

    private Student getStudentUser(ResultSet rs) throws SQLException {
        int userId = rs.getInt("id");
        String sql2 = "SELECT * FROM students WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
            stmt2.setInt(1, userId);
            ResultSet rs2 = stmt2.executeQuery();
            if (rs2.next()) {
                return new Student(
                    userId,
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs2.getInt("id"),
                    rs2.getString("reg_number"),
                    rs2.getString("batch"),
                    rs2.getString("department"),
                    rs2.getBoolean("is_repeat"),
                    rs2.getBoolean("is_batch_missed")
                );
            }
        }
        return null;
    }

    private TechnicalOfficer getTechnicalOfficerUser(ResultSet rs) throws SQLException {
        int userId = rs.getInt("id");
        String sql2 = "SELECT * FROM technical_officers WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
            stmt2.setInt(1, userId);
            ResultSet rs2 = stmt2.executeQuery();
            if (rs2.next()) {
                return new TechnicalOfficer(
                    userId,
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs2.getInt("id"),
                    rs2.getString("department")
                );
            }
        }
        return null;
    }

    /**
     * Update user profile (email, phone, address)
     */
    public boolean updateProfile(int userId, String email, String phone, String address) throws SQLException {
        String sql = "UPDATE users SET email=?, phone=?, address=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, phone);
            stmt.setString(3, address);
            stmt.setInt(4, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Create a new user (Admin only)
     */
    public boolean createUser(String username, String password, String role,
                               String fullName, String email, String phone) throws SQLException {
        String sql = "INSERT INTO users (username, password, role, full_name, email, phone) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            stmt.setString(4, fullName);
            stmt.setString(5, email);
            stmt.setString(6, phone);
            int rows = stmt.executeUpdate();

            if (rows > 0 && role.equals("STUDENT")) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    int newUserId = keys.getInt(1);
                    // Also insert into students table
                    String sql2 = "INSERT INTO students (user_id, reg_number, batch, department) VALUES (?,?,?,?)";
                    try (PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
                        stmt2.setInt(1, newUserId);
                        stmt2.setString(2, "ICT/" + java.time.Year.now().getValue() + "/NEW");
                        stmt2.setString(3, String.valueOf(java.time.Year.now().getValue()));
                        stmt2.setString(4, "ICT");
                        stmt2.executeUpdate();
                    }
                }
            }
            return rows > 0;
        }
    }

    /**
     * Delete a user (Admin only)
     */
    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }
}
