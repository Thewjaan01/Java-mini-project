package lms.model;

/**
 * Student - extends User
 * Demonstrates: Inheritance, Encapsulation
 */
public class Student extends User {

    private int studentId;
    private String regNumber;
    private String batch;
    private String department;
    private boolean isRepeat;
    private boolean isBatchMissed;

    public Student(int id, String username, String password,
                   String fullName, String email, String phone,
                   int studentId, String regNumber, String batch,
                   String department, boolean isRepeat, boolean isBatchMissed) {
        super(id, username, password, "STUDENT", fullName, email, phone);
        this.studentId    = studentId;
        this.regNumber    = regNumber;
        this.batch        = batch;
        this.department   = department;
        this.isRepeat     = isRepeat;
        this.isBatchMissed = isBatchMissed;
    }

    @Override
    public String getDashboardTitle() {
        return "Student Dashboard";
    }

    @Override
    public String getWelcomeMessage() {
        return "Welcome, " + getFullName() + " (" + regNumber + ")!";
    }

    public int getStudentId()      { return studentId; }
    public String getRegNumber()   { return regNumber; }
    public String getBatch()       { return batch; }
    public String getDepartment()  { return department; }
    public boolean isRepeat()      { return isRepeat; }
    public boolean isBatchMissed() { return isBatchMissed; }

    public String getStudentType() {
        if (isRepeat)      return "Repeat";
        if (isBatchMissed) return "Batch Missed";
        return "Regular";
    }
}
