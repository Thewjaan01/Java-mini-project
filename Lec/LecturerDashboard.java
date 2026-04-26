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

import lms.model.Lecturer;
import lms.util.DatabaseConnection;
import lms.util.GradeCalculator;

import java.sql.*;

/**
 * LecturerDashboard
 * Demonstrates: GUI, Database Handling, Inheritance usage
 */
public class LecturerDashboard {

    private Lecturer lecturer;
    private Stage stage;
    private BorderPane root;

    public LecturerDashboard(Lecturer lecturer) {
        this.lecturer = lecturer;
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("LMS - " + lecturer.getDashboardTitle());

        root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f6f9;");
        root.setTop(createTopBar());
        root.setLeft(createSidebar());
        root.setCenter(createUploadMarksPanel());

        Scene scene = new Scene(root, 1100, 700);
        stage.setScene(scene);
        stage.show();
    }

    private HBox createTopBar() {
        Label title = new Label("LMS  —  Lecturer Panel");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        Label userInfo = new Label(lecturer.getFullName() + "  |  " + lecturer.getDepartment());
        userInfo.setTextFill(Color.LIGHTGRAY);

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> { stage.close(); new LoginScreen().start(new Stage()); });

        HBox bar = new HBox(10, title);
        HBox.setHgrow(title, Priority.ALWAYS);
        bar.getChildren().addAll(userInfo, logoutBtn);
        bar.setPadding(new Insets(14, 20, 14, 20));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #1b5e20;");
        return bar;
    }

    private VBox createSidebar() {
        Label menuLabel = new Label("MENU");
        menuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        menuLabel.setTextFill(Color.GRAY);
        menuLabel.setPadding(new Insets(10, 0, 5, 10));

        Button[] buttons = {
            sideBtn("📝  Upload Marks",        () -> root.setCenter(createUploadMarksPanel())),
            sideBtn("👥  View Students",        () -> root.setCenter(createViewStudentsPanel())),
            sideBtn("📊  Marks Summary",        () -> root.setCenter(createMarksSummaryPanel())),
            sideBtn("✅  Eligibility Check",    () -> root.setCenter(createEligibilityPanel())),
            sideBtn("📢  Notices",              () -> root.setCenter(createNoticesPanel())),
            sideBtn("⚙️  My Profile",           () -> root.setCenter(createProfilePanel())),
        };

        VBox sidebar = new VBox(4, menuLabel);
        sidebar.getChildren().addAll(buttons);
        sidebar.setPrefWidth(210);
        sidebar.setPadding(new Insets(10, 0, 10, 0));
        sidebar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 0 0;");
        return sidebar;
    }

    private Button sideBtn(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setPrefWidth(210);
        btn.setPrefHeight(40);
        btn.setAlignment(Pos.CENTER_LEFT);
        String base = "-fx-background-color: transparent; -fx-font-size: 13; -fx-cursor: hand; -fx-padding: 0 0 0 15;";
        String hover = "-fx-background-color: #e8f5e9; -fx-font-size: 13; -fx-cursor: hand; -fx-padding: 0 0 0 15;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        btn.setOnAction(e -> action.run());
        return btn;
    }

    // ---- UPLOAD MARKS PANEL ----
    private VBox createUploadMarksPanel() {
        Label title = new Label("Upload / Update Marks");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Select course
        ComboBox<String> courseBox = new ComboBox<>();
        courseBox.setPromptText("Select Course");
        loadCoursesIntoCombo(courseBox);

        ComboBox<String> examTypeBox = new ComboBox<>();
        examTypeBox.getItems().addAll("CA1", "CA2", "CA3", "PRACTICAL", "FINAL");
        examTypeBox.setPromptText("Exam Type");

        Button loadBtn = new Button("Load Students");
        loadBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");

        TableView<MarkRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<MarkRow, String> colReg   = new TableColumn<>("Reg No");
        TableColumn<MarkRow, String> colName  = new TableColumn<>("Name");
        TableColumn<MarkRow, String> colMarks = new TableColumn<>("Marks (0-100)");

        colReg.setCellValueFactory(new PropertyValueFactory<>("regNo"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colMarks.setCellValueFactory(new PropertyValueFactory<>("marks"));

        // Make marks column editable
        colMarks.setCellFactory(col -> new TableCell<MarkRow, String>() {
            private final TextField tf = new TextField();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                tf.setText(item);
                tf.textProperty().addListener((obs, o, n) -> {
                    MarkRow row = getTableView().getItems().get(getIndex());
                    row.setMarks(n);
                });
                setGraphic(tf);
            }
        });
        colMarks.setEditable(true);
        table.setEditable(true);
        table.getColumns().addAll(colReg, colName, colMarks);

        Label msgLabel = new Label("");

        loadBtn.setOnAction(e -> {
            if (courseBox.getValue() == null || examTypeBox.getValue() == null) {
                msgLabel.setText("Please select course and exam type.");
                msgLabel.setTextFill(Color.RED);
                return;
            }
            table.getItems().clear();
            int courseId = getCourseIdFromDisplay(courseBox.getValue());
            loadStudentsForMarks(table, courseId, examTypeBox.getValue());
        });

        Button saveBtn = new Button("Save All Marks");
        saveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            int courseId = getCourseIdFromDisplay(courseBox.getValue());
            String examType = examTypeBox.getValue();
            int saved = 0;
            for (MarkRow row : table.getItems()) {
                try {
                    double m = Double.parseDouble(row.getMarks());
                    if (m < 0 || m > 100) continue;
                    String sql = "INSERT INTO marks (student_id, course_id, exam_type, marks_obtained, uploaded_by) " +
                                 "VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE marks_obtained=?, uploaded_by=?";
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, row.getStudentId());
                        stmt.setInt(2, courseId);
                        stmt.setString(3, examType);
                        stmt.setDouble(4, m);
                        stmt.setInt(5, lecturer.getId());
                        stmt.setDouble(6, m);
                        stmt.setInt(7, lecturer.getId());
                        stmt.executeUpdate();
                        saved++;
                    }
                } catch (NumberFormatException | SQLException ex) {
                    ex.printStackTrace();
                }
            }
            msgLabel.setText("Saved marks for " + saved + " students.");
            msgLabel.setTextFill(Color.GREEN);
        });

        HBox controls = new HBox(10, courseBox, examTypeBox, loadBtn, saveBtn);
        VBox panel = new VBox(14, title, controls, table, msgLabel);
        panel.setPadding(new Insets(24));
        return panel;
    }

    // ---- VIEW STUDENTS PANEL ----
    private VBox createViewStudentsPanel() {
        Label title = new Label("Undergraduate Details");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        TableView<StudentRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        String[] cols = {"Reg No", "Full Name", "Batch", "Type", "Email"};
        String[] props = {"regNo", "fullName", "batch", "type", "email"};
        for (int i = 0; i < cols.length; i++) {
            TableColumn<StudentRow, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(new PropertyValueFactory<>(props[i]));
            table.getColumns().add(col);
        }

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT u.full_name, u.email, s.reg_number, s.batch, s.is_repeat, s.is_batch_missed " +
                 "FROM students s JOIN users u ON s.user_id = u.id ORDER BY s.reg_number")) {
            while (rs.next()) {
                String type = rs.getBoolean("is_repeat") ? "Repeat" :
                              rs.getBoolean("is_batch_missed") ? "Batch Missed" : "Regular";
                table.getItems().add(new StudentRow(
                    rs.getString("reg_number"), rs.getString("full_name"),
                    rs.getString("batch"), type, rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        VBox panel = new VBox(14, title, table);
        panel.setPadding(new Insets(24));
        return panel;
    }

    // ---- MARKS SUMMARY PANEL ----
    private VBox createMarksSummaryPanel() {
        Label title = new Label("Marks Summary");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        ComboBox<String> courseBox = new ComboBox<>();
        courseBox.setPromptText("Select Course");
        loadCoursesIntoCombo(courseBox);

        TableView<SummaryRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        String[] cols = {"Reg No", "Name", "CA1", "CA2", "CA3", "Practical", "Final", "Grade", "CA Eligible"};
        String[] props = {"regNo","name","ca1","ca2","ca3","practical","finalMark","grade","eligible"};
        for (int i = 0; i < cols.length; i++) {
            TableColumn<SummaryRow, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(new PropertyValueFactory<>(props[i]));
            table.getColumns().add(col);
        }

        Button loadBtn = new Button("Load Summary");
        loadBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
        loadBtn.setOnAction(e -> {
            if (courseBox.getValue() == null) return;
            table.getItems().clear();
            int courseId = getCourseIdFromDisplay(courseBox.getValue());
            loadMarksSummary(table, courseId);
        });

        HBox controls = new HBox(10, courseBox, loadBtn);
        VBox panel = new VBox(14, title, controls, table);
        panel.setPadding(new Insets(24));
        return panel;
    }

    // ---- ELIGIBILITY PANEL ----
    private VBox createEligibilityPanel() {
        Label title = new Label("Eligibility Check (Attendance + CA)");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        ComboBox<String> courseBox = new ComboBox<>();
        courseBox.setPromptText("Select Course");
        loadCoursesIntoCombo(courseBox);

        TableView<EligRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        String[] cols = {"Reg No","Name","Attendance %","CA Avg","Att. Eligible","CA Eligible","Overall"};
        String[] props = {"regNo","name","attPct","caAvg","attElig","caElig","overall"};
        for (int i = 0; i < cols.length; i++) {
            TableColumn<EligRow, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(new PropertyValueFactory<>(props[i]));
            table.getColumns().add(col);
        }

        Button loadBtn = new Button("Check Eligibility");
        loadBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-cursor: hand;");
        loadBtn.setOnAction(e -> {
            if (courseBox.getValue() == null) return;
            table.getItems().clear();
            int courseId = getCourseIdFromDisplay(courseBox.getValue());
            loadEligibility(table, courseId);
        });

        HBox controls = new HBox(10, courseBox, loadBtn);
        VBox panel = new VBox(14, title, controls, table);
        panel.setPadding(new Insets(24));
        return panel;
    }

    // ---- NOTICES PANEL ----
    private VBox createNoticesPanel() {
        Label title = new Label("Notices");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        ListView<String> list = new ListView<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT title, content, posted_at FROM notices WHERE target_role='ALL' OR target_role='LECTURER' ORDER BY posted_at DESC")) {
            while (rs.next()) {
                list.getItems().add("[" + rs.getString("posted_at") + "] " + rs.getString("title") + "\n" + rs.getString("content"));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        VBox panel = new VBox(12, title, list);
        panel.setPadding(new Insets(24));
        return panel;
    }

    // ---- PROFILE PANEL ----
    private VBox createProfilePanel() {
        Label title = new Label("My Profile");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        TextField tfEmail  = new TextField(lecturer.getEmail());
        TextField tfPhone  = new TextField(lecturer.getPhone());
        TextField tfAddr   = new TextField(lecturer.getAddress() != null ? lecturer.getAddress() : "");
        Label msgLabel = new Label("");

        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            try {
                lms.dao.UserDAO dao = new lms.dao.UserDAO();
                boolean ok = dao.updateProfile(lecturer.getId(), tfEmail.getText(), tfPhone.getText(), tfAddr.getText());
                msgLabel.setText(ok ? "Profile updated!" : "Update failed.");
                msgLabel.setTextFill(ok ? Color.GREEN : Color.RED);
            } catch (Exception ex) {
                msgLabel.setText("Error: " + ex.getMessage());
                msgLabel.setTextFill(Color.RED);
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12);
        grid.addRow(0, new Label("Name:"),   new Label(lecturer.getFullName()));
        grid.addRow(1, new Label("Dept:"),   new Label(lecturer.getDepartment()));
        grid.addRow(2, new Label("Email:"),  tfEmail);
        grid.addRow(3, new Label("Phone:"),  tfPhone);
        grid.addRow(4, new Label("Address:"),tfAddr);

        VBox panel = new VBox(14, title, grid, saveBtn, msgLabel);
        panel.setPadding(new Insets(24));
        return panel;
    }

    // ---- DB Helper Methods ----

    private void loadCoursesIntoCombo(ComboBox<String> box) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, course_code, course_name FROM courses ORDER BY course_code")) {
            while (rs.next()) {
                box.getItems().add(rs.getInt("id") + ": " + rs.getString("course_code") + " - " + rs.getString("course_name"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private int getCourseIdFromDisplay(String display) {
        if (display == null) return -1;
        return Integer.parseInt(display.split(":")[0].trim());
    }

    private void loadStudentsForMarks(TableView<MarkRow> table, int courseId, String examType) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT s.id, s.reg_number, u.full_name, COALESCE(m.marks_obtained, 0) as marks " +
                 "FROM enrollments e JOIN students s ON e.student_id = s.id " +
                 "JOIN users u ON s.user_id = u.id " +
                 "LEFT JOIN marks m ON m.student_id = s.id AND m.course_id = ? AND m.exam_type = ? " +
                 "WHERE e.course_id = ? ORDER BY s.reg_number")) {
            stmt.setInt(1, courseId);
            stmt.setString(2, examType);
            stmt.setInt(3, courseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                table.getItems().add(new MarkRow(
                    rs.getInt("id"),
                    rs.getString("reg_number"),
                    rs.getString("full_name"),
                    String.valueOf(rs.getDouble("marks"))
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadMarksSummary(TableView<SummaryRow> table, int courseId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT s.id, s.reg_number, u.full_name, " +
                 "MAX(CASE WHEN m.exam_type='CA1' THEN m.marks_obtained ELSE 0 END) ca1, " +
                 "MAX(CASE WHEN m.exam_type='CA2' THEN m.marks_obtained ELSE 0 END) ca2, " +
                 "MAX(CASE WHEN m.exam_type='CA3' THEN m.marks_obtained ELSE 0 END) ca3, " +
                 "MAX(CASE WHEN m.exam_type='PRACTICAL' THEN m.marks_obtained ELSE 0 END) prac, " +
                 "MAX(CASE WHEN m.exam_type='FINAL' THEN m.marks_obtained ELSE 0 END) final_mark " +
                 "FROM enrollments e JOIN students s ON e.student_id = s.id JOIN users u ON s.user_id = u.id " +
                 "LEFT JOIN marks m ON m.student_id = s.id AND m.course_id = ? " +
                 "WHERE e.course_id = ? GROUP BY s.id, s.reg_number, u.full_name ORDER BY s.reg_number")) {
            stmt.setInt(1, courseId);
            stmt.setInt(2, courseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                double ca1 = rs.getDouble("ca1"), ca2 = rs.getDouble("ca2"),
                       ca3 = rs.getDouble("ca3"), prac = rs.getDouble("prac"),
                       fin = rs.getDouble("final_mark");
                double caAvg = (ca1 + ca2 + ca3) / 3.0;
                String grade = GradeCalculator.getGrade(fin > 0 ? fin : caAvg);
                String eligible = GradeCalculator.isCAEligible(caAvg) ? "✅ Yes" : "❌ No";
                table.getItems().add(new SummaryRow(
                    rs.getString("reg_number"), rs.getString("full_name"),
                    f(ca1), f(ca2), f(ca3), f(prac), f(fin), grade, eligible
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadEligibility(TableView<EligRow> table, int courseId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT s.id, s.reg_number, u.full_name, " +
                 "SUM(CASE WHEN a.is_present THEN 1 ELSE 0 END) attended, COUNT(a.id) total, " +
                 "AVG(CASE WHEN m.exam_type IN ('CA1','CA2','CA3') THEN m.marks_obtained END) ca_avg " +
                 "FROM enrollments e JOIN students s ON e.student_id = s.id JOIN users u ON s.user_id = u.id " +
                 "LEFT JOIN attendance a ON a.student_id = s.id AND a.course_id = ? " +
                 "LEFT JOIN marks m ON m.student_id = s.id AND m.course_id = ? " +
                 "WHERE e.course_id = ? GROUP BY s.id, s.reg_number, u.full_name ORDER BY s.reg_number")) {
            stmt.setInt(1, courseId); stmt.setInt(2, courseId); stmt.setInt(3, courseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int attended = rs.getInt("attended"), total = rs.getInt("total");
                double caAvg = rs.getDouble("ca_avg");
                double attPct = GradeCalculator.getAttendancePercentage(attended, total > 0 ? total : 15);
                boolean attOk = attPct >= 80.0;
                boolean caOk  = GradeCalculator.isCAEligible(caAvg);
                table.getItems().add(new EligRow(
                    rs.getString("reg_number"), rs.getString("full_name"),
                    f(attPct) + "%", f(caAvg),
                    attOk ? "✅" : "❌", caOk ? "✅" : "❌",
                    (attOk && caOk) ? "✅ Eligible" : "❌ Not Eligible"
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private String f(double d) { return String.format("%.2f", d); }

    // ---- Row classes ----
    public static class MarkRow {
        private int studentId;
        private String regNo, name, marks;
        public MarkRow(int sid, String r, String n, String m) { studentId=sid; regNo=r; name=n; marks=m; }
        public int getStudentId()  { return studentId; }
        public String getRegNo()   { return regNo; }
        public String getName()    { return name; }
        public String getMarks()   { return marks; }
        public void setMarks(String m) { this.marks = m; }
    }

    public static class StudentRow {
        private String regNo, fullName, batch, type, email;
        public StudentRow(String r, String fn, String b, String t, String e) { regNo=r; fullName=fn; batch=b; type=t; email=e; }
        public String getRegNo()    { return regNo; }
        public String getFullName() { return fullName; }
        public String getBatch()    { return batch; }
        public String getType()     { return type; }
        public String getEmail()    { return email; }
    }

    public static class SummaryRow {
        private String regNo, name, ca1, ca2, ca3, practical, finalMark, grade, eligible;
        public SummaryRow(String r,String n,String c1,String c2,String c3,String p,String f,String g,String e) {
            regNo=r; name=n; ca1=c1; ca2=c2; ca3=c3; practical=p; finalMark=f; grade=g; eligible=e;
        }
        public String getRegNo()     { return regNo; }
        public String getName()      { return name; }
        public String getCa1()       { return ca1; }
        public String getCa2()       { return ca2; }
        public String getCa3()       { return ca3; }
        public String getPractical() { return practical; }
        public String getFinalMark() { return finalMark; }
        public String getGrade()     { return grade; }
        public String getEligible()  { return eligible; }
    }

    public static class EligRow {
        private String regNo, name, attPct, caAvg, attElig, caElig, overall;
        public EligRow(String r,String n,String ap,String ca,String ae,String ce,String o) {
            regNo=r; name=n; attPct=ap; caAvg=ca; attElig=ae; caElig=ce; overall=o;
        }
        public String getRegNo()   { return regNo; }
        public String getName()    { return name; }
        public String getAttPct()  { return attPct; }
        public String getCaAvg()   { return caAvg; }
        public String getAttElig() { return attElig; }
        public String getCaElig()  { return caElig; }
        public String getOverall() { return overall; }
    }
}
