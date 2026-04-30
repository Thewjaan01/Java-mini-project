package lms.model;

/**
 * TechnicalOfficer - extends User
 * Demonstrates: Inheritance
 */
public class TechnicalOfficer extends User {

    private int officerId;
    private String department;

    public TechnicalOfficer(int id, String username, String password,
                             String fullName, String email, String phone,
                             int officerId, String department) {
        super(id, username, password, "TECHNICAL_OFFICER", fullName, email, phone);
        this.officerId  = officerId;
        this.department = department;
    }

    @Override
    public String getDashboardTitle() {
        return "Technical Officer Dashboard";
    }

    public int getOfficerId()     { return officerId; }
    public String getDepartment() { return department; }
    public void setDepartment(String dept) { this.department = dept; }
}
