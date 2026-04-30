package lms.model;

/**
 * Course - Model class for course details
 * Demonstrates: Encapsulation
 */
public class Course {

    private int id;
    private String courseCode;
    private String courseName;
    private int credits;
    private boolean hasTheory;
    private boolean hasPractical;
    private int theorySessions;
    private int practicalSessions;
    private String department;
    private int lecturerId;
    private String lecturerName;
    private String semester;
    private String batch;

    public Course(int id, String courseCode, String courseName, int credits,
                  boolean hasTheory, boolean hasPractical,
                  int theorySessions, int practicalSessions,
                  String department, int lecturerId, String semester, String batch) {
        this.id               = id;
        this.courseCode       = courseCode;
        this.courseName       = courseName;
        this.credits          = credits;
        this.hasTheory        = hasTheory;
        this.hasPractical     = hasPractical;
        this.theorySessions   = theorySessions;
        this.practicalSessions = practicalSessions;
        this.department       = department;
        this.lecturerId       = lecturerId;
        this.semester         = semester;
        this.batch            = batch;
    }

    // Getters
    public int getId()               { return id; }
    public String getCourseCode()    { return courseCode; }
    public String getCourseName()    { return courseName; }
    public int getCredits()          { return credits; }
    public boolean isHasTheory()     { return hasTheory; }
    public boolean isHasPractical()  { return hasPractical; }
    public int getTheorySessions()   { return theorySessions; }
    public int getPracticalSessions(){ return practicalSessions; }
    public String getDepartment()    { return department; }
    public int getLecturerId()       { return lecturerId; }
    public String getLecturerName()  { return lecturerName; }
    public String getSemester()      { return semester; }
    public String getBatch()         { return batch; }

    // Setters
    public void setCourseCode(String c)   { this.courseCode = c; }
    public void setCourseName(String n)   { this.courseName = n; }
    public void setCredits(int cr)        { this.credits = cr; }
    public void setLecturerName(String n) { this.lecturerName = n; }

    @Override
    public String toString() {
        return courseCode + " - " + courseName;
    }
}
