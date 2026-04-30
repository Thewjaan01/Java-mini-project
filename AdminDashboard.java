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

import lms.dao.UserDAO;
import lms.model.Admin;

import java.sql.*;
import lms.util.DatabaseConnection;

/**
 * AdminDashboard - Full admin panel
 * Demonstrates: GUI, Database Handling, Exception Handling
 */
public class AdminDashboard {

    private Admin admin;
    private Stage stage;
    private BorderPane root;

    public AdminDashboard(Admin admin) {
        this.admin = admin;
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("LMS - " + admin.getDashboardTitle());

        root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f6f9;");

        // Top bar
        root.setTop(createTopBar());

        // Left sidebar
        root.setLeft(createSidebar());

        // Default center: show manage users
        root.setCenter(createManageUsersPanel());

        Scene scene = new Scene(root, 1100, 700);
        stage.setScene(scene);
        stage.show();
    }

    // ---- TOP BAR ----
    private HBox createTopBar() {
        Label title = new Label("University of Ruhuna LMS  —  Admin Panel");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        Label userInfo = new Label("Logged in: " + admin.getFullName());
        userInfo.setTextFill(Color.LIGHTGRAY);
        userInfo.setFont(Font.font("Arial", 12));

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> {
            stage.close();
            new LoginScreen().start(new Stage());
        });

        HBox topBar = new HBox(10, title);
        HBox.setHgrow(title, Priority.ALWAYS);
        topBar.getChildren().addAll(userInfo, logoutBtn);
        topBar.setPadding(new Insets(14, 20, 14, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #1a237e;");
        return topBar;
    }

    // ---- SIDEBAR ----
    private VBox createSidebar() {
        Label menuLabel = new Label("MENU");
        menuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        menuLabel.setTextFill(Color.GRAY);
        menuLabel.setPadding(new Insets(10, 0, 5, 10));

        Button[] buttons = {
            createSideBtn("👥  Manage Users",    () -> root.setCenter(createManageUsersPanel())),
            createSideBtn("📚  Manage Courses",  () -> root.setCenter(createManageCoursesPanel())),
            createSideBtn("📢  Manage Notices",  () -> root.setCenter(createManageNoticesPanel())),
            createSideBtn("🗓  Timetables",      () -> root.setCenter(createTimetablePanel())),
        };

        VBox sidebar = new VBox(4, menuLabel);
        sidebar.getChildren().addAll(buttons);
        sidebar.setPadding(new Insets(10, 0, 10, 0));
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 0 0;");
        return sidebar;
    }

    private Button createSideBtn(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setPrefWidth(200);
        btn.setPrefHeight(40);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle("-fx-background-color: transparent; -fx-font-size: 13; -fx-cursor: hand; -fx-padding: 0 0 0 15;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #e8eaf6; -fx-font-size: 13; -fx-cursor: hand; -fx-padding: 0 0 0 15;"));
        btn.setOnMouseExited(e  -> btn.setStyle("-fx-background-color: transparent; -fx-font-size: 13; -fx-cursor: hand; -fx-padding: 0 0 0 15;"));
        btn.setOnAction(e -> action.run());
        return btn;
    }

    // ---- MANAGE USERS PANEL ----
    private VBox createManageUsersPanel() {
        Label title = new Label("Manage Users");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Table
        TableView<UserRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<UserRow, String> colId       = new TableColumn<>("ID");
        TableColumn<UserRow, String> colUsername = new TableColumn<>("Username");
        TableColumn<UserRow, String> colName     = new TableColumn<>("Full Name");
        TableColumn<UserRow, String> colRole     = new TableColumn<>("Role");
        TableColumn<UserRow, String> colEmail    = new TableColumn<>("Email");
        TableColumn<UserRow, String> colPhone    = new TableColumn<>("Phone");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        table.getColumns().addAll(colId, colUsername, colName, colRole, colEmail, colPhone);
        loadUsersIntoTable(table);

        // Add User Form
        Label addTitle = new Label("Add New User");
        addTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        TextField tfUsername  = new TextField(); tfUsername.setPromptText("Username");
        TextField tfPassword  = new TextField(); tfPassword.setPromptText("Password");
        TextField tfFullName  = new TextField(); tfFullName.setPromptText("Full Name");
        TextField tfEmail     = new TextField(); tfEmail.setPromptText("Email");
        TextField tfPhone     = new TextField(); tfPhone.setPromptText("Phone");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("ADMIN", "LECTURER", "STUDENT", "TECHNICAL_OFFICER");
        roleBox.setPromptText("Select Role");

        Label msgLabel = new Label("");
        msgLabel.setTextFill(Color.GREEN);

        Button addBtn = new Button("Add User");
        addBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
        addBtn.setOnAction(e -> {
            try {
                UserDAO dao = new UserDAO();
                boolean ok = dao.createUser(
                    tfUsername.getText(), tfPassword.getText(),
                    roleBox.getValue(), tfFullName.getText(),
                    tfEmail.getText(), tfPhone.getText()
                );
                if (ok) {
                    msgLabel.setText("User added successfully!");
                    msgLabel.setTextFill(Color.GREEN);
                    loadUsersIntoTable(table);
                    tfUsername.clear(); tfPassword.clear(); tfFullName.clear();
                    tfEmail.clear(); tfPhone.clear(); roleBox.setValue(null);
                } else {
                    msgLabel.setText("Failed to add user.");
                    msgLabel.setTextFill(Color.RED);
                }
            } catch (Exception ex) {
                msgLabel.setText("Error: " + ex.getMessage());
                msgLabel.setTextFill(Color.RED);
            }
        });

        HBox formRow1 = new HBox(10, tfUsername, tfPassword, tfFullName);
        HBox formRow2 = new HBox(10, tfEmail, tfPhone, roleBox, addBtn);
        formRow1.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        formRow2.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        VBox panel = new VBox(14, title, table, addTitle, formRow1, formRow2, msgLabel);
        panel.setPadding(new Insets(24));
        return panel;
    }

    private void loadUsersIntoTable(TableView<UserRow> table) {
        table.getItems().clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, username, full_name, role, email, phone FROM users ORDER BY id")) {
            while (rs.next()) {
                table.getItems().add(new UserRow(
                    String.valueOf(rs.getInt("id")),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("role"),
                    rs.getString("email"),
                    rs.getString("phone")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---- MANAGE COURSES PANEL ----
    private VBox createManageCoursesPanel() {
        Label title = new Label("Manage Courses");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        TableView<CourseRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<CourseRow, String> colCode    = new TableColumn<>("Code");
        TableColumn<CourseRow, String> colName    = new TableColumn<>("Course Name");
        TableColumn<CourseRow, String> colCredits = new TableColumn<>("Credits");
        TableColumn<CourseRow, String> colTheory  = new TableColumn<>("Theory");
        TableColumn<CourseRow, String> colPrac    = new TableColumn<>("Practical");

        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCredits.setCellValueFactory(new PropertyValueFactory<>("credits"));
        colTheory.setCellValueFactory(new PropertyValueFactory<>("hasTheory"));
        colPrac.setCellValueFactory(new PropertyValueFactory<>("hasPractical"));

        table.getColumns().addAll(colCode, colName, colCredits, colTheory, colPrac);
        loadCoursesIntoTable(table);

        // Add course form
        TextField tfCode    = new TextField(); tfCode.setPromptText("Course Code e.g. ICT2101");
        TextField tfName    = new TextField(); tfName.setPromptText("Course Name");
        TextField tfCredits = new TextField(); tfCredits.setPromptText("Credits");
        CheckBox cbTheory   = new CheckBox("Has Theory");
        CheckBox cbPrac     = new CheckBox("Has Practical");
        cbTheory.setSelected(true);

        Label msgLabel = new Label("");
        Button addBtn  = new Button("Add Course");
        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
        addBtn.setOnAction(e -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO courses (course_code, course_name, credits, has_theory, has_practical, department, semester, batch) VALUES (?,?,?,?,?,?,?,?)")) {
                stmt.setString(1, tfCode.getText());
                stmt.setString(2, tfName.getText());
                stmt.setInt(3, Integer.parseInt(tfCredits.getText()));
                stmt.setBoolean(4, cbTheory.isSelected());
                stmt.setBoolean(5, cbPrac.isSelected());
                stmt.setString(6, "ICT");
                stmt.setString(7, "Semester 1");
                stmt.setString(8, "2021");
                stmt.executeUpdate();
                msgLabel.setText("Course added!");
                msgLabel.setTextFill(Color.GREEN);
                loadCoursesIntoTable(table);
                tfCode.clear(); tfName.clear(); tfCredits.clear();
            } catch (Exception ex) {
                msgLabel.setText("Error: " + ex.getMessage());
                msgLabel.setTextFill(Color.RED);
            }
        });

        HBox formRow = new HBox(10, tfCode, tfName, tfCredits, cbTheory, cbPrac, addBtn);
        VBox panel = new VBox(14, title, table, new Label("Add New Course:"), formRow, msgLabel);
        panel.setPadding(new Insets(24));
        return panel;
    }

    private void loadCoursesIntoTable(TableView<CourseRow> table) {
        table.getItems().clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM courses ORDER BY id")) {
            while (rs.next()) {
                table.getItems().add(new CourseRow(
                    rs.getString("course_code"),
                    rs.getString("course_name"),
                    String.valueOf(rs.getInt("credits")),
                    rs.getBoolean("has_theory") ? "Yes" : "No",
                    rs.getBoolean("has_practical") ? "Yes" : "No"
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---- MANAGE NOTICES PANEL ----
    private VBox createManageNoticesPanel() {
        Label title = new Label("Manage Notices");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        ListView<String> noticeList = new ListView<>();
        loadNotices(noticeList);

        TextField tfTitle = new TextField(); tfTitle.setPromptText("Notice Title");
        TextArea taContent = new TextArea(); taContent.setPromptText("Notice content..."); taContent.setPrefRowCount(3);
        ComboBox<String> targetBox = new ComboBox<>();
        targetBox.getItems().addAll("ALL", "STUDENT", "LECTURER", "TECHNICAL_OFFICER");
        targetBox.setValue("ALL");

        Label msgLabel = new Label("");
        Button postBtn = new Button("Post Notice");
        postBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-cursor: hand;");
        postBtn.setOnAction(e -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO notices (title, content, posted_by, target_role) VALUES (?,?,?,?)")) {
                stmt.setString(1, tfTitle.getText());
                stmt.setString(2, taContent.getText());
                stmt.setInt(3, admin.getId());
                stmt.setString(4, targetBox.getValue());
                stmt.executeUpdate();
                msgLabel.setText("Notice posted!");
                msgLabel.setTextFill(Color.GREEN);
                loadNotices(noticeList);
                tfTitle.clear(); taContent.clear();
            } catch (Exception ex) {
                msgLabel.setText("Error: " + ex.getMessage());
                msgLabel.setTextFill(Color.RED);
            }
        });

        HBox formRow = new HBox(10, tfTitle, targetBox, postBtn);
        VBox panel = new VBox(12, title, noticeList, new Label("Post New Notice:"), formRow, taContent, msgLabel);
        panel.setPadding(new Insets(24));
        return panel;
    }

    private void loadNotices(ListView<String> list) {
        list.getItems().clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT title, target_role, posted_at FROM notices ORDER BY posted_at DESC")) {
            while (rs.next()) {
                list.getItems().add("[" + rs.getString("target_role") + "] " +
                    rs.getString("title") + "  (" + rs.getString("posted_at") + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---- TIMETABLE PANEL ----
    private VBox createTimetablePanel() {
        Label title = new Label("Manage Timetables");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        TableView<TimetableRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<TimetableRow, String> colCourse  = new TableColumn<>("Course");
        TableColumn<TimetableRow, String> colDay     = new TableColumn<>("Day");
        TableColumn<TimetableRow, String> colStart   = new TableColumn<>("Start");
        TableColumn<TimetableRow, String> colEnd     = new TableColumn<>("End");
        TableColumn<TimetableRow, String> colLoc     = new TableColumn<>("Location");
        TableColumn<TimetableRow, String> colType    = new TableColumn<>("Type");

        colCourse.setCellValueFactory(new PropertyValueFactory<>("course"));
        colDay.setCellValueFactory(new PropertyValueFactory<>("day"));
        colStart.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEnd.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colLoc.setCellValueFactory(new PropertyValueFactory<>("location"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        table.getColumns().addAll(colCourse, colDay, colStart, colEnd, colLoc, colType);
        loadTimetable(table);

        VBox panel = new VBox(14, title, table);
        panel.setPadding(new Insets(24));
        return panel;
    }

    private void loadTimetable(TableView<TimetableRow> table) {
        table.getItems().clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT c.course_name, t.day_of_week, t.start_time, t.end_time, t.location, t.session_type " +
                 "FROM timetables t JOIN courses c ON t.course_id = c.id ORDER BY t.day_of_week, t.start_time")) {
            while (rs.next()) {
                table.getItems().add(new TimetableRow(
                    rs.getString("course_name"),
                    rs.getString("day_of_week"),
                    rs.getString("start_time"),
                    rs.getString("end_time"),
                    rs.getString("location"),
                    rs.getString("session_type")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---- Inner Row Classes (for TableView) ----

    public static class UserRow {
        private String id, username, fullName, role, email, phone;
        public UserRow(String id, String username, String fullName, String role, String email, String phone) {
            this.id=id; this.username=username; this.fullName=fullName;
            this.role=role; this.email=email; this.phone=phone;
        }
        public String getId()       { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getRole()     { return role; }
        public String getEmail()    { return email; }
        public String getPhone()    { return phone; }
    }

    public static class CourseRow {
        private String code, name, credits, hasTheory, hasPractical;
        public CourseRow(String code, String name, String credits, String hasTheory, String hasPractical) {
            this.code=code; this.name=name; this.credits=credits;
            this.hasTheory=hasTheory; this.hasPractical=hasPractical;
        }
        public String getCode()         { return code; }
        public String getName()         { return name; }
        public String getCredits()      { return credits; }
        public String getHasTheory()    { return hasTheory; }
        public String getHasPractical() { return hasPractical; }
    }

    public static class TimetableRow {
        private String course, day, startTime, endTime, location, type;
        public TimetableRow(String course, String day, String startTime, String endTime, String location, String type) {
            this.course=course; this.day=day; this.startTime=startTime;
            this.endTime=endTime; this.location=location; this.type=type;
        }
        public String getCourse()    { return course; }
        public String getDay()       { return day; }
        public String getStartTime() { return startTime; }
        public String getEndTime()   { return endTime; }
        public String getLocation()  { return location; }
        public String getType()      { return type; }
    }
}
