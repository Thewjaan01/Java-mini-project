package lms.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

import lms.model.Student;
import lms.util.DatabaseConnection;
import lms.util.GradeCalculator;

import java.sql.*;

/**
 * StudentDashboard
 * Demonstrates: GUI, Inheritance, Database Handling
 */
public class StudentDashboard {

    private Student student;
    private Stage stage;
    private BorderPane root;

    public StudentDashboard(Student student) {
        this.student = student;
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("LMS - " + student.getDashboardTitle());

        root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f6f9;");
        root.setTop(createTopBar());
        root.setLeft(createSidebar());
        root.setCenter(createMyCoursesPanel());

        Scene scene = new Scene(root, 1100, 700);
        stage.setScene(scene);
        stage.show();
    }

    private HBox createTopBar() {
        Label title = new Label("LMS  —  Student Portal");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        Label info = new Label(student.getFullName() + "  |  " + student.getRegNumber() + "  |  " + student.getStudentType());
        info.setTextFill(Color.LIGHTGRAY);

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> { stage.close(); new LoginScreen().start(new Stage()); });

        HBox bar = new HBox(10, title);
        HBox.setHgrow(title, Priority.ALWAYS);
        bar.getChildren().addAll(info, logoutBtn);
        bar.setPadding(new Insets(14, 20, 14, 20));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #4a148c;");
        return bar;
    }

    private VBox createSidebar() {
        Label menuLabel = new Label("MENU");
        menuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        menuLabel.setTextFill(Color.GRAY);
        menuLabel.setPadding(new Insets(10, 0, 5, 10));

        Button[] btns = {
            sBtn("📚  My Courses",      () -> root.setCenter(createMyCoursesPanel())),
            sBtn("📊  My Marks & GPA",  () -> root.setCenter(createMarksPanel())),
            sBtn("📅  Attendance",      () -> root.setCenter(createAttendancePanel())),
            sBtn("🏥  Medical Records", () -> root.setCenter(createMedicalPanel())),
            sBtn("🗓  Timetable",       () -> root.setCenter(createTimetablePanel())),
            sBtn("📢  Notices",         () -> root.setCenter(createNoticesPanel())),
            sBtn("⚙️  My Profile",      () -> root.setCenter(createProfilePanel())),
        };

        VBox sidebar = new VBox(4, menuLabel);
        sidebar.getChildren().addAll(btns);
        sidebar.setPrefWidth(210);
        sidebar.setPadding(new Insets(10, 0, 10, 0));
        sidebar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 0 0;");
        return sidebar;
    }

    private Button sBtn(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setPrefWidth(210);
        btn.setPrefHeight(40);
        btn.setAlignment(Pos.CENTER_LEFT);
        String base  = "-fx-background-color: transparent; -fx-font-size: 13; -fx-cursor: hand; -fx-padding: 0 0 0 15;";
        String hover = "-fx-background-color: #f3e5f5; -fx-font-size: 13; -fx-cursor: hand; -fx-padding: 0 0 0 15;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        btn.setOnAction(e -> action.run());
        return btn;
    }

    // ---- MY COURSES ----
    private VBox createMyCoursesPanel() {
        Label title = new Label("My Enrolled Courses");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        TableView<String[]> table = buildTable(new String[]{"Code","Course Name","Credits","Theory","Practical","Lecturer"});

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT c.course_code, c.course_name, c.credits, c.has_theory, c.has_practical, u.full_name " +
                 "FROM enrollments e JOIN courses c ON e.course_id = c.id " +
                 "LEFT JOIN lecturers l ON c.lecturer_id = l.id LEFT JOIN users u ON l.user_id = u.id " +
                 "WHERE e.student_id = ?")) {
            stmt.setInt(1, student.getStudentId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                table.getItems().add(new String[]{
                    rs.getString("course_code"), rs.getString("course_name"),
                    String.valueOf(rs.getInt("credits")),
                    rs.getBoolean("has_theory") ? "Yes" : "No",
                    rs.getBoolean("has_practical") ? "Yes" : "No",
                    rs.getString("full_name") != null ? rs.getString("full_name") : "TBA"
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }

        VBox panel = new VBox(14, title, table);
        panel.setPadding(new Insets(24));
        return panel;
    }

    // ---- MARKS & GPA ----
    private VBox createMarksPanel() {
        Label title = new Label("My Marks, Grades & GPA");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        TableView<String[]> table = buildTable(new String[]{"Course","Credits","CA Avg","Final","Grade","Grade Point"});

        double[] totalGP = {0};
        int[]    totalCr = {0};

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT c.course_name, c.credits, " +
                 "AVG(CASE WHEN m.exam_type IN ('CA1','CA2','CA3') THEN m.marks_obtained END) ca_avg, " +
                 "MAX(CASE WHEN m.exam_type='FINAL' THEN m.marks_obtained END) final_mark " +
                 "FROM enrollments e JOIN courses c ON e.course_id = c.id " +
                 "LEFT JOIN marks m ON m.student_id = ? AND m.course_id = c.id " +
                 "WHERE e.student_id = ? GROUP BY c.id, c.course_name, c.credits ORDER BY c.course_code")) {
            stmt.setInt(1, student.getStudentId());
            stmt.setInt(2, student.getStudentId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                double caAvg = rs.getDouble("ca_avg");
                double fin   = rs.getDouble("final_mark");
                double score = fin > 0 ? fin : caAvg;
                int credits  = rs.getInt("credits");
                String grade = score > 0 ? GradeCalculator.getGrade(score) : "N/A";
                double gp    = score > 0 ? GradeCalculator.getGradePoint(grade) : 0;
                if (score > 0) { totalGP[0] += gp * credits; totalCr[0] += credits; }
                table.getItems().add(new String[]{
                    rs.getString("course_name"), String.valueOf(credits),
                    score > 0 ? String.format("%.2f", caAvg) : "-",
                    fin > 0   ? String.format("%.2f", fin) : "-",
                    grade, score > 0 ? String.format("%.1f", gp) : "-"
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }

        double sgpa = totalCr[0] > 0 ? Math.round((totalGP[0] / totalCr[0]) * 100.0) / 100.0 : 0;
        Label sgpaLabel = new Label("SGPA:  " + sgpa);
        sgpaLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        sgpaLabel.setStyle("-fx-background-color: #e8eaf6; -fx-padding: 10; -fx-background-radius: 6;");

        VBox panel = new VBox(14, title, table, sgpaLabel);
        panel.setPadding(new Insets(24));
        return panel;
    }

    // ---- ATTENDANCE ----
    private VBox createAttendancePanel() {
        Label title = new Label("My Attendance");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        TableView<String[]> table = buildTable(new String[]{"Course","Theory Att.","Theory %","Practical Att.","Practical %","Combined %","Status"});

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT c.course_name, c.theory_sessions, c.practical_sessions, c.has_practical, " +
                 "SUM(CASE WHEN a.session_type='THEORY' AND a.is_present THEN 1 ELSE 0 END) th_att, " +
                 "SUM(CASE WHEN a.session_type='PRACTICAL' AND a.is_present THEN 1 ELSE 0 END) pr_att " +
                 "FROM enrollments e JOIN courses c ON e.course_id = c.id " +
                 "LEFT JOIN attendance a ON a.student_id = ? AND a.course_id = c.id " +
                 "WHERE e.student_id = ? GROUP BY c.id ORDER BY c.course_code")) {
            stmt.setInt(1, student.getStudentId());
            stmt.setInt(2, student.getStudentId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int thSess = rs.getInt("theory_sessions");
                int prSess = rs.getBoolean("has_practical") ? rs.getInt("practical_sessions") : 0;
                int thAtt  = rs.getInt("th_att");
                int prAtt  = rs.getInt("pr_att");
                double thPct  = GradeCalculator.getAttendancePercentage(thAtt, thSess);
                double prPct  = prSess > 0 ? GradeCalculator.getAttendancePercentage(prAtt, prSess) : 0;
                double combPct = GradeCalculator.getAttendancePercentage(thAtt + prAtt, thSess + prSess);
                String status = combPct >= 80 ? "✅ OK" : "❌ Low";
                table.getItems().add(new String[]{
                    rs.getString("course_name"),
                    thAtt + "/" + thSess, String.format("%.1f%%", thPct),
                    prSess > 0 ? prAtt + "/" + prSess : "N/A",
                    prSess > 0 ? String.format("%.1f%%", prPct) : "N/A",
                    String.format("%.1f%%", combPct), status
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }

        VBox panel = new VBox(14, title, table);
        panel.setPadding(new Insets(24));
        return panel;
    }

    // ---- MEDICAL ----
    private VBox createMedicalPanel() {
        Label title = new Label("My Medical Records");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        TableView<String[]> table = buildTable(new String[]{"Date","Reason","Status","Submitted"});

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT medical_date, reason, is_approved, submitted_at FROM medicals WHERE student_id = ? ORDER BY medical_date DESC")) {
            stmt.setInt(1, student.getStudentId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                table.getItems().add(new String[]{
                    rs.getString("medical_date"),
                    rs.getString("reason"),
                    rs.getBoolean("is_approved") ? "✅ Approved" : "⏳ Pending",
                    rs.getString("submitted_at")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }

        VBox panel = new VBox(14, title, table);
        panel.setPadding(new Insets(24));
        return panel;
    }

    // ---- TIMETABLE ----
    private VBox createTimetablePanel() {
        Label title = new Label("My Timetable");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        TableView<String[]> table = buildTable(new String[]{"Day","Course","Start","End","Location","Type"});

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT t.day_of_week, c.course_name, t.start_time, t.end_time, t.location, t.session_type " +
                 "FROM timetables t JOIN courses c ON t.course_id = c.id " +
                 "JOIN enrollments e ON e.course_id = c.id WHERE e.student_id = ? ORDER BY t.day_of_week, t.start_time")) {
            stmt.setInt(1, student.getStudentId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                table.getItems().add(new String[]{
                    rs.getString("day_of_week"), rs.getString("course_name"),
                    rs.getString("start_time"), rs.getString("end_time"),
                    rs.getString("location") != null ? rs.getString("location") : "TBA",
                    rs.getString("session_type")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }

        VBox panel = new VBox(14, title, table);
        panel.setPadding(new Insets(24));
        return panel;
    }

    // ---- NOTICES ----
    private VBox createNoticesPanel() {
        Label title = new Label("Notices");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        ListView<String> list = new ListView<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT title, content, posted_at FROM notices WHERE target_role='ALL' OR target_role='STUDENT' ORDER BY posted_at DESC")) {
            while (rs.next()) {
                list.getItems().add("[" + rs.getString("posted_at") + "] " + rs.getString("title") + "\n" + rs.getString("content"));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        VBox panel = new VBox(12, title, list);
        panel.setPadding(new Insets(24));
        return panel;
    }

    // ---- PROFILE ----
    private VBox createProfilePanel() {
        Label title = new Label("My Profile");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        TextField tfPhone = new TextField(student.getPhone() != null ? student.getPhone() : "");
        TextField tfAddr  = new TextField(student.getAddress() != null ? student.getAddress() : "");
        // Students can only edit contact details
        Label msgLabel = new Label("");

        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            try {
                lms.dao.UserDAO dao = new lms.dao.UserDAO();
                boolean ok = dao.updateProfile(student.getId(), student.getEmail(), tfPhone.getText(), tfAddr.getText());
                msgLabel.setText(ok ? "Profile updated!" : "Failed.");
                msgLabel.setTextFill(ok ? Color.GREEN : Color.RED);
            } catch (Exception ex) {
                msgLabel.setText("Error: " + ex.getMessage());
                msgLabel.setTextFill(Color.RED);
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12);
        grid.addRow(0, new Label("Name:"),      new Label(student.getFullName()));
        grid.addRow(1, new Label("Reg No:"),    new Label(student.getRegNumber()));
        grid.addRow(2, new Label("Batch:"),     new Label(student.getBatch()));
        grid.addRow(3, new Label("Type:"),      new Label(student.getStudentType()));
        grid.addRow(4, new Label("Phone:"),     tfPhone);
        grid.addRow(5, new Label("Address:"),   tfAddr);

        VBox panel = new VBox(14, title, grid, saveBtn, msgLabel);
        panel.setPadding(new Insets(24));
        return panel;
    }

    // ---- Helper: generic table builder ----
    @SuppressWarnings("unchecked")
    private TableView<String[]> buildTable(String[] columns) {
        TableView<String[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        for (int i = 0; i < columns.length; i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(columns[i]);
            col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().length > idx ? data.getValue()[idx] : ""
            ));
            table.getColumns().add(col);
        }
        return table;
    }
}
