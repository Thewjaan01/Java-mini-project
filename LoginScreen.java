package lms.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

import lms.dao.UserDAO;
import lms.model.*;

/**
 * LoginScreen - Main entry point of the LMS application
 * Demonstrates: GUI, Event Handling, Exception Handling
 */
public class LoginScreen extends Application {

    private TextField usernameField;
    private PasswordField passwordField;
    private Label messageLabel;
    private UserDAO userDAO = new UserDAO();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("University of Ruhuna - LMS Login");

        // ---- Title ----
        Label titleLabel = new Label("University of Ruhuna");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.WHITE);

        Label subTitle = new Label("Learning Management System");
        subTitle.setFont(Font.font("Arial", 14));
        subTitle.setTextFill(Color.LIGHTGRAY);

        VBox titleBox = new VBox(5, titleLabel, subTitle);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(20, 0, 30, 0));

        // ---- Username ----
        Label userLabel = new Label("Username");
        userLabel.setTextFill(Color.WHITE);
        usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setPrefHeight(38);
        usernameField.setStyle("-fx-background-radius: 5; -fx-font-size: 14;");

        // ---- Password ----
        Label passLabel = new Label("Password");
        passLabel.setTextFill(Color.WHITE);
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setPrefHeight(38);
        passwordField.setStyle("-fx-background-radius: 5; -fx-font-size: 14;");

        // Allow Enter key to login
        passwordField.setOnAction(e -> handleLogin(primaryStage));

        // ---- Message label (for errors) ----
        messageLabel = new Label("");
        messageLabel.setTextFill(Color.RED);
        messageLabel.setFont(Font.font("Arial", 12));

        // ---- Login Button ----
        Button loginBtn = new Button("Login");
        loginBtn.setPrefWidth(300);
        loginBtn.setPrefHeight(40);
        loginBtn.setStyle(
            "-fx-background-color: #2196F3; -fx-text-fill: white; " +
            "-fx-font-size: 14; -fx-background-radius: 5; -fx-cursor: hand;"
        );
        loginBtn.setOnAction(e -> handleLogin(primaryStage));

        // Hover effect
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(
            "-fx-background-color: #1976D2; -fx-text-fill: white; " +
            "-fx-font-size: 14; -fx-background-radius: 5; -fx-cursor: hand;"
        ));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(
            "-fx-background-color: #2196F3; -fx-text-fill: white; " +
            "-fx-font-size: 14; -fx-background-radius: 5; -fx-cursor: hand;"
        ));

        // ---- Layout ----
        VBox formBox = new VBox(12,
            userLabel, usernameField,
            passLabel, passwordField,
            messageLabel,
            loginBtn
        );
        formBox.setPadding(new Insets(30));
        formBox.setMaxWidth(340);
        formBox.setStyle(
            "-fx-background-color: #1e1e2e; -fx-background-radius: 10;"
        );

        VBox mainBox = new VBox(10, titleBox, formBox);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setStyle("-fx-background-color: #13131f;");

        Scene scene = new Scene(mainBox, 480, 500);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void handleLogin(Stage stage) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Basic validation
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password.");
            return;
        }

        try {
            User user = userDAO.login(username, password);

            if (user == null) {
                messageLabel.setText("Invalid username or password.");
                passwordField.clear();
                return;
            }

            // Open the correct dashboard based on role
            // Demonstrates: Polymorphism
            openDashboard(user, stage);

        } catch (Exception e) {
            messageLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Opens the correct dashboard based on user role
     * Demonstrates: Polymorphism - instanceof check
     */
    private void openDashboard(User user, Stage stage) {
        stage.close();

        if (user instanceof Admin) {
            new AdminDashboard((Admin) user).show();
        } else if (user instanceof Lecturer) {
            new LecturerDashboard((Lecturer) user).show();
        } else if (user instanceof Student) {
            new StudentDashboard((Student) user).show();
        } else if (user instanceof TechnicalOfficer) {
            new TechnicalOfficerDashboard((TechnicalOfficer) user).show();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
