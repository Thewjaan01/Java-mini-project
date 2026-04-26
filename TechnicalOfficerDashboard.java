package lms.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

import lms.model.TechnicalOfficer;
import lms.util.DatabaseConnection;
import lms.util.GradeCalculator;

import java.sql.*;
import java.time.LocalDate;

/**
 * TechnicalOfficerDashboard
 * Demonstrates: GUI, Database Handling, Inheritance
 */
public class TechnicalOfficerDashboard {

    private TechnicalOfficer officer;
    private Stage stage;
    private BorderPane root;

    public TechnicalOfficerDashboard(TechnicalOfficer officer) {
        this.officer = officer;
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("LMS - " + officer.getDashboardTitle());

        root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f6f9;");
        root.setTop(createTopBar());
        root.setLeft(createSidebar());
        root.setCenter(createAttendancePanel());

        Scene scene = new Scene(root, 1100, 700);
        stage.setScene(scene);
        stage.show();
    }

    private HBox createTopBar() {
        Label title = new Label("LMS  —  Technical Officer Panel");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        Label info = new Label(officer.getFullName() + "  |  " + officer.getDepartment());
        info.setTextFill(Color.LIGHTGRAY);

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> { stage.close(); new LoginScreen().start(new Stage()); });

        HBox bar = new HBox(10, title);
        HBox.setHgrow(title, Priority.ALWAYS);
        bar.getChildren().addAll(info, logoutBtn);
        bar.setPadding(new Insets(14, 20, 14, 20));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #bf360c;");
        return bar;
    }

    private VBox createSidebar() {
        Label menuLabel = new Label("MENU");
        menuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        menuLabel.setTextFill(Color.GRAY);
        menuLabel.setPadding(new Insets(10, 0, 5, 10));

        Button[] btns = {
            sBtn("📅  Mark Attendance",    () -> root.setCenter(createAttendancePanel())),
            sBtn("📊  Attendance Summary", () -> root.setCenter(createAttendanceSummaryPanel())),
            sBtn("🏥  Medical Records",    () -> root.setCenter(createMedicalPanel())),
            sBtn("📢  Notices",            () -> root.setCenter(createNoticesPanel())),
            sBtn("🗓  Timetable",          () -> root.setCenter(createTimetablePanel())),
            sBtn("⚙️  My Profile",         () -> root.setCenter(createProfilePanel())),
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
        btn.setPrefWidth(210); btn.setPrefHeight(40);
        btn.setAlignment(Pos.CENTER_LEFT);
        String base  = "-fx-background-color: transparent; -fx-font-size: 13; -fx-cursor: hand; -fx-padding: 0 0 0 15;";
        String hover = "-fx-background-color: #fbe9e7; -fx-font-size: 13; -fx-cursor: hand; -fx-padding: 0 0 0 15;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        btn.setOnAction(e -> action.run());
        return btn;
    }

    // ---- MARK ATTENDANCE ----
    private VBox createAttendancePanel() {
        Label title = new Label("Mark Attendance");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        ComboBox<String> courseBox = new ComboBox<>();
        courseBox.setPromptText("Select Course");
        loadCourses(courseBox);

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("THEORY", "PRACTICAL");
        typeBox.setPromptText("Session Type");

        TextField tfSession = new TextField();
        tfSession.setPromptText("Session No. (1-15)");
        tfSession.setPrefWidth(120);

        DatePicker datePicker = new DatePicker(LocalDate.now());

        Button loadBtn = new Button("Load Students");
        loadBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");

        // Student list with checkboxes
        VBox studentCheckboxes = new VBox(8);
        studentCheckboxes.setPadding(new Insets(10));
        ScrollPane scroll = new ScrollPane(studentCheckboxes);
        scroll.setPrefHeight(350);
        scroll.setFitToWidth(true);

        Label msgLabel = new Label("");

        loadBtn.setOnAction(e -> {
            studentCheckboxes.getChildren().clear();
            if (courseBox.getValue() == null || typeBox.getValue() == null) {
                msgLabel.setText("Please select course and session type.");
                msgLabel.setTextFill(Color.RED);
                return;
            }
            int courseId = Integer.parseInt(courseBox.getValue().split(":")[0].trim());
            int session  = 1;
            try { session = Integer.parseInt(tfSession.getText()); } catch (NumberFormatException ex) {}
            String sessionDate = datePicker.getValue().toString();

            // Load existing attendance for this session
            final int finalCourseId = courseId;
            final int finalSession  = session;
            final String finalType  = typeBox.getValue();
            final String finalDate  = sessionDate;

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT s.id, s.reg_number, u.full_name, " +
                     "COALESCE((SELECT is_present FROM attendance WHERE student_id=s.id AND course_id=? AND session_number=? AND session_type=?), FALSE) as present " +
                     "FROM enrollments en JOIN students s ON en.student_id = s.id " +
                     "JOIN users u ON s.user_id = u.id WHERE en.course_id = ? ORDER BY s.reg_number")) {
                stmt.setInt(1, finalCourseId); stmt.setInt(2, finalSession);
                stmt.setString(3, finalType);  stmt.setInt(4, finalCourseId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int studentId = rs.getInt("id");
                    CheckBox cb = new CheckBox(rs.getString("reg_number") + "  " + rs.getString("full_name"));
                    cb.setSelected(rs.getBoolean("present"));
                    cb.setUserData(studentId);
                    studentCheckboxes.getChildren().add(cb);
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        Button saveBtn = new Button("Save Attendance");
        saveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            if (courseBox.getValue() == null) return;
            int courseId = Integer.parseInt(courseBox.getValue().split(":")[0].trim());
            int session; try { session = Integer.parseInt(tfSession.getText()); } catch (NumberFormatException ex) { session = 1; }
            String sessionType = typeBox.getValue();
            String sessionDate = datePicker.getValue().toString();
            int saved = 0;
            for (javafx.scene.Node node : studentCheckboxes.getChildren()) {
                if (node instanceof CheckBox) {
                    CheckBox cb = (CheckBox) node;
                    int studentId = (int) cb.getUserData();
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(
                             "INSERT INTO attendance (student_id, course_id, session_number, session_type, session_date, is_present, marked_by) " +
                             "VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE is_present=?, session_date=?, marked_by=?")) {
                        stmt.setInt(1, studentId);   stmt.setInt(2, courseId);
                        stmt.setInt(3, session);      stmt.setString(4, sessionType);
                        stmt.setString(5, sessionDate); stmt.setBoolean(6, cb.isSelected());
                        stmt.setInt(7, officer.getId()); stmt.setBoolean(8, cb.isSelected());
                        stmt.setString(9, sessionDate);  stmt.setInt(10, officer.getId());
                        stmt.executeUpdate();
                        saved++;
                    } catch (SQLException ex) { ex.printStackTrace(); }
                }
            }
            msgLabel.setText("Attendance saved for " + saved + " students.");
            msgLabel.setTextFill(Color.GREEN);
        });

        HBox controls = new HBox(10, courseBox, typeBox, tfSession, datePicker, loadBtn);
        VBox panel = new VBox(14, title, controls, scroll, saveBtn, msgLabel);
        panel.setPadding(new Insets(24));
        return panel;
    }

    // ---- ATTENDANCE SUMMARY ----
    private VBox createAttendanceSummaryPanel() {
        Label title = new Label("Attendance Summary — Whole Batch");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        ComboBox<String> courseBox = new ComboBox<>();
        courseBox.setPromptText("Select Course");
        loadCourses(courseBox);

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("THEORY", "PRACTICAL", "COMBINED");
        typeBox.setValue("COMBINED");

        TableView<String[]> table = buildTable(new String[]{"Reg No","Name","Attended","Total","Percentage","Status"});

        Button loadBtn = new Button("Load Summary");
        loadBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-cursor: hand;");
        loadBtn.setOnAction(e -> {
            if (courseBox.getValue() == null) return;
            table.getItems().clear();
            int courseId = Integer.parseInt(courseBox.getValue().split(":")[0].trim());
            String type = typeBox.getValue();

            String typeFilter = type.equals("COMBINED") ? "1=1" :
                                type.equals("THEORY")   ? "a.session_type='THEORY'" : "a.session_type='PRACTICAL'";

            String sql = "SELECT s.reg_number, u.full_name, " +
                         "SUM(CASE WHEN a.is_present AND " + typeFilter + " THEN 1 ELSE 0 END) attended, " +
                         "SUM(CASE WHEN " + typeFilter + " THEN 1 ELSE 0 END) total_sessions " +
                         "FROM enrollments en JOIN students s ON en.student_id = s.id JOIN users u ON s.user_id = u.id " +
                         "LEFT JOIN attendance a ON a.student_id = s.id AND a.course_id = en.course_id " +
                         "WHERE en.course_id = ? GROUP BY s.id ORDER BY s.reg_number";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, courseId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int att = rs.getInt("attended"), tot = rs.getInt("total_sessions");
                    double pct = tot > 0 ? GradeCalculator.getAttendancePercentage(att, tot) : 0;
                    String status = pct >= 80 ? "✅ OK" : pct == 80 ? "✅ Exactly 80%" : "❌ Low";
                    table.getItems().add(new String[]{
                        rs.getString("reg_number"), rs.getString("full_name"),
                        String.valueOf(att), String.valueOf(tot),
                        String.format("%.1f%%", pct), status
                    });
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        HBox controls = new HBox(10, courseBox, typeBox, loadBtn);
        VBox panel = new VBox(14, title, controls, table);
        panel.setPadding(new Insets(24));
        return panel;
    }

    // ---- MEDICAL PANEL ----
    private VBox createMedicalPanel() {
        Label title = new Label("Manage Medical Records");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        TableView<String[]> table = buildTable(new String[]{"Reg No","Name","Date","Reason","Status"});
        loadMedicals(table);

        // Add medical
        ComboBox<String> studentBox = new ComboBox<>();
        studentBox.setPromptText("Select Student");
        loadStudents(studentBox);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField tfReason = new TextField(); tfReason.setPromptText("Reason");
        CheckBox cbApproved = new CheckBox("Approved");
        Label msgLabel = new Label("");

        Button addBtn = new Button("Add Medical");
        addBtn.setStyle("-fx-background-color: #9c27b0; -fx-text-fill: white; -fx-cursor: hand;");
        addBtn.setOnAction(e -> {
            if (studentBox.getValue() == null) { msgLabel.setText("Select a student."); return; }
            int studentId = Integer.parseInt(studentBox.getValue().split(":")[0].trim());
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO medicals (student_id, medical_date, reason, is_approved, approved_by) VALUES (?,?,?,?,?)")) {
                stmt.setInt(1, studentId);
                stmt.setString(2, datePicker.getValue().toString());
                stmt.setString(3, tfReason.getText());
                stmt.setBoolean(4, cbApproved.isSelected());
                stmt.setInt(5, cbApproved.isSelected() ? officer.getId() : 0);
                stmt.executeUpdate();
                msgLabel.setText("Medical record added!"); msgLabel.setTextFill(Color.GREEN);
                loadMedicals(table);
                tfReason.clear();
            } catch (SQLException ex) {
                msgLabel.setText("Error: " + ex.getMessage()); msgLabel.setTextFill(Color.RED);
            }
        });

        HBox form = new HBox(10, studentBox, datePicker, tfReason, cbApproved, addBtn);
        VBox panel = new VBox(14, title, table, new Label("Add Medical Record:"), form, msgLabel);
        panel.setPadding(new Insets(24));
        return panel;
    }

    private void loadMedicals(TableView<String[]> table) {
        table.getItems().clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT s.reg_number, u.full_name, m.medical_date, m.reason, m.is_approved " +
                 "FROM medicals m JOIN students s ON m.student_id = s.id JOIN users u ON s.user_id = u.id " +
                 "ORDER BY m.medical_date DESC")) {
            while (rs.next()) {
                table.getItems().add(new String[]{
                    rs.getString("reg_number"), rs.getString("full_name"),
                    rs.getString("medical_date"), rs.getString("reason"),
                    rs.getBoolean("is_approved") ? "✅ Approved" : "⏳ Pending"
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ---- NOTICES ----
    private VBox createNoticesPanel() {
        Label title = new Label("Notices");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        ListView<String> list = new ListView<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT title, content, posted_at FROM notices WHERE target_role='ALL' OR target_role='TECHNICAL_OFFICER' ORDER BY posted_at DESC")) {
            while (rs.next()) {
                list.getItems().add("[" + rs.getString("posted_at") + "] " + rs.getString("title") + "\n" + rs.getString("content"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        VBox panel = new VBox(12, title, list);
        panel.setPadding(new Insets(24));
        return panel;
    }

    // ---- TIMETABLE ----
    private VBox createTimetablePanel() {
        Label title = new Label("Department Timetable");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        TableView<String[]> table = buildTable(new String[]{"Day","Course","Start","End","Location","Type"});

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT t.day_of_week, c.course_name, t.start_time, t.end_time, t.location, t.session_type " +
                 "FROM timetables t JOIN courses c ON t.course_id = c.id WHERE t.department = ? ORDER BY t.day_of_week, t.start_time")) {
            stmt.setString(1, officer.getDepartment());
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

    // ---- PROFILE ----
    private VBox createProfilePanel() {
        Label title = new Label("My Profile");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        TextField tfEmail = new TextField(officer.getEmail() != null ? officer.getEmail() : "");
        TextField tfPhone = new TextField(officer.getPhone() != null ? officer.getPhone() : "");
        TextField tfAddr  = new TextField(officer.getAddress() != null ? officer.getAddress() : "");
        Label msgLabel = new Label("");

        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            try {
                lms.dao.UserDAO dao = new lms.dao.UserDAO();
                boolean ok = dao.updateProfile(officer.getId(), tfEmail.getText(), tfPhone.getText(), tfAddr.getText());
                msgLabel.setText(ok ? "Updated!" : "Failed."); msgLabel.setTextFill(ok ? Color.GREEN : Color.RED);
            } catch (Exception ex) {
                msgLabel.setText("Error: " + ex.getMessage()); msgLabel.setTextFill(Color.RED);
            }
        });

        GridPane grid = new GridPane(); grid.setHgap(12); grid.setVgap(12);
        grid.addRow(0, new Label("Name:"),  new Label(officer.getFullName()));
        grid.addRow(1, new Label("Dept:"),  new Label(officer.getDepartment()));
        grid.addRow(2, new Label("Email:"), tfEmail);
        grid.addRow(3, new Label("Phone:"), tfPhone);
        grid.addRow(4, new Label("Addr:"),  tfAddr);

        VBox panel = new VBox(14, title, grid, saveBtn, msgLabel);
        panel.setPadding(new Insets(24));
        return panel;
    }

    // ---- Helpers ----
    private void loadCourses(ComboBox<String> box) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, course_code, course_name FROM courses ORDER BY course_code")) {
            while (rs.next()) {
                box.getItems().add(rs.getInt("id") + ": " + rs.getString("course_code") + " - " + rs.getString("course_name"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadStudents(ComboBox<String> box) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT s.id, s.reg_number, u.full_name FROM students s JOIN users u ON s.user_id = u.id ORDER BY s.reg_number")) {
            while (rs.next()) {
                box.getItems().add(rs.getInt("id") + ": " + rs.getString("reg_number") + " - " + rs.getString("full_name"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @SuppressWarnings("unchecked")
    private TableView<String[]> buildTable(String[] columns) {
        TableView<String[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        for (int i = 0; i < columns.length; i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(columns[i]);
            col.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().length > idx ? d.getValue()[idx] : ""
            ));
            table.getColumns().add(col);
        }
        return table;
    }
}
