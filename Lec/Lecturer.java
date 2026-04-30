package lms.model;

/**
 * Lecturer - extends User
 * Demonstrates: Inheritance, Encapsulation
 */
public class Lecturer extends User {

    private int lecturerId;
    private String department;
    private String specialization;

    public Lecturer(int id, String username, String password,
                    String fullName, String email, String phone,
                    int lecturerId, String department, String specialization) {
        super(id, username, password, "LECTURER", fullName, email, phone);
        this.lecturerId = lecturerId;
        this.department = department;
        this.specialization = specialization;
    }

    @Override
    public String getDashboardTitle() {
        return "Lecturer Dashboard";
    }

    @Override
    public String getWelcomeMessage() {
        return "Welcome, Dr./Mr./Ms. " + getFullName() + "!";
    }

    public int getLecturerId()       { return lecturerId; }
    public String getDepartment()    { return department; }
    public String getSpecialization(){ return specialization; }

    public void setDepartment(String dept)     { this.department = dept; }
    public void setSpecialization(String spec) { this.specialization = spec; }
}
