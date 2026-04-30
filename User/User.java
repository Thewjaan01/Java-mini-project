package lms.model;

/**
 * User - Abstract base class for all user types
 * Demonstrates: Abstraction, Encapsulation, Inheritance (parent class)
 */
public abstract class User {

    // Encapsulation: private fields with getters/setters
    private int id;
    private String username;
    private String password;
    private String role;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String profilePicture;

    // Constructor
    public User(int id, String username, String password, String role,
                String fullName, String email, String phone) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }

    // Abstract method - every subclass MUST implement this
    // Demonstrates: Abstraction
    public abstract String getDashboardTitle();

    // Demonstrates: Polymorphism - each subclass can override this
    public String getWelcomeMessage() {
        return "Welcome, " + fullName + "!";
    }

    // ---- Getters ----
    public int getId()             { return id; }
    public String getUsername()    { return username; }
    public String getPassword()    { return password; }
    public String getRole()        { return role; }
    public String getFullName()    { return fullName; }
    public String getEmail()       { return email; }
    public String getPhone()       { return phone; }
    public String getAddress()     { return address; }
    public String getProfilePicture() { return profilePicture; }

    // ---- Setters ----
    public void setFullName(String fullName)       { this.fullName = fullName; }
    public void setEmail(String email)             { this.email = email; }
    public void setPhone(String phone)             { this.phone = phone; }
    public void setAddress(String address)         { this.address = address; }
    public void setProfilePicture(String pic)      { this.profilePicture = pic; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role='" + role + "'}";
    }
}
