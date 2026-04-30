package lms.model;

/**
 * Admin - extends User
 * Demonstrates: Inheritance, Polymorphism
 */
public class Admin extends User {

    public Admin(int id, String username, String password,
                 String fullName, String email, String phone) {
        super(id, username, password, "ADMIN", fullName, email, phone);
    }

    @Override
    public String getDashboardTitle() {
        return "Admin Dashboard";
    }

    @Override
    public String getWelcomeMessage() {
        return "Welcome, Administrator " + getFullName() + "!";
    }
}
