package lms.util;

/**
 * GradeCalculator - Calculates grades, GPA based on UGC Circular No. 12-2024
 * Demonstrates: Encapsulation, Static methods, Error handling
 */
public class GradeCalculator {

    // UGC Commission Circular No. 12-2024 grade boundaries
    public static String getGrade(double marks) {
        if (marks < 0 || marks > 100) {
            throw new IllegalArgumentException("Marks must be between 0 and 100");
        }
        if (marks >= 85)      return "A+";
        else if (marks >= 75) return "A";
        else if (marks >= 70) return "A-";
        else if (marks >= 65) return "B+";
        else if (marks >= 60) return "B";
        else if (marks >= 55) return "B-";
        else if (marks >= 50) return "C+";
        else if (marks >= 45) return "C";
        else if (marks >= 40) return "C-";
        else if (marks >= 35) return "D+";
        else if (marks >= 30) return "D";
        else                  return "E";  // Fail
    }

    // Grade point for each grade
    public static double getGradePoint(String grade) {
        switch (grade) {
            case "A+": return 4.0;
            case "A":  return 4.0;
            case "A-": return 3.7;
            case "B+": return 3.3;
            case "B":  return 3.0;
            case "B-": return 2.7;
            case "C+": return 2.3;
            case "C":  return 2.0;
            case "C-": return 1.7;
            case "D+": return 1.3;
            case "D":  return 1.0;
            case "E":  return 0.0;
            default:   return 0.0;
        }
    }

    /**
     * Calculate SGPA (Semester GPA)
     * @param grades  array of grade strings e.g. {"A", "B+", "C"}
     * @param credits array of credit values for each course
     */
    public static double calculateSGPA(String[] grades, int[] credits) {
        if (grades.length != credits.length) {
            throw new IllegalArgumentException("Grades and credits arrays must have same length");
        }

        double totalPoints = 0;
        int totalCredits = 0;

        for (int i = 0; i < grades.length; i++) {
            double gradePoint = getGradePoint(grades[i]);
            totalPoints  += gradePoint * credits[i];
            totalCredits += credits[i];
        }

        if (totalCredits == 0) return 0.0;
        return Math.round((totalPoints / totalCredits) * 100.0) / 100.0;
    }

    /**
     * Calculate CGPA from multiple semesters
     * @param sgpaList   array of SGPA values
     * @param creditList total credits per semester
     */
    public static double calculateCGPA(double[] sgpaList, int[] creditList) {
        double totalWeighted = 0;
        int totalCredits = 0;
        for (int i = 0; i < sgpaList.length; i++) {
            totalWeighted += sgpaList[i] * creditList[i];
            totalCredits  += creditList[i];
        }
        if (totalCredits == 0) return 0.0;
        return Math.round((totalWeighted / totalCredits) * 100.0) / 100.0;
    }

    /**
     * Check eligibility: CA marks must be >= 40
     */
    public static boolean isCAEligible(double caMarks) {
        return caMarks >= 40.0;
    }

    /**
     * Check attendance eligibility: must be >= 80%
     */
    public static boolean isAttendanceEligible(int attended, int total) {
        if (total == 0) return false;
        double percent = (attended * 100.0) / total;
        return percent >= 80.0;
    }

    /**
     * Calculate attendance percentage
     */
    public static double getAttendancePercentage(int attended, int total) {
        if (total == 0) return 0.0;
        return Math.round((attended * 100.0 / total) * 100.0) / 100.0;
    }
}
