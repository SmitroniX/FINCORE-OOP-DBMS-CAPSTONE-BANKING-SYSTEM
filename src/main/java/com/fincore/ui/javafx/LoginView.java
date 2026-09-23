package com.fincore.ui.javafx;

import com.fincore.db.DatabaseManager;
import com.fincore.model.AuthUser;
import com.fincore.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.function.Consumer;

/**
 * JavaFX 21 Modern Financial Authentication View.
 * Authenticates against Oracle Database (or SQLite fallback) with full security auditing.
 */
public class LoginView extends StackPane {

    private final AuthService authService;
    private final Consumer<AuthUser> onLoginSuccess;

    private TextField usernameField;
    private PasswordField passwordField;
    private Label errorLabel;
    private Button loginButton;

    public LoginView(AuthService authService, Consumer<AuthUser> onLoginSuccess) {
        this.authService = authService;
        this.onLoginSuccess = onLoginSuccess;

        getStyleClass().add("main-container");
        initUI();
    }

    private void initUI() {
        VBox card = new VBox(18);
        card.setMaxWidth(440);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("kpi-card");
        card.setPadding(new Insets(36, 40, 36, 40));

        // 1. Branding Header
        Label logoIcon = new Label("🏦");
        logoIcon.setStyle("-fx-font-size: 42px;");

        Label titleLabel = new Label("FinCore Banking");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        Label subtitleLabel = new Label("Enterprise Finance Management & DBMS Capstone");
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        // 2. Active Database Indicator Badge
        boolean isOracle = DatabaseManager.getInstance().isOracle();
        HBox dbBadge = new HBox(8);
        dbBadge.setAlignment(Pos.CENTER);
        Circle statusDot = new Circle(4.5, isOracle ? Color.web("#4ade80") : Color.web("#38bdf8"));
        Label dbLabel = new Label(isOracle ? "Oracle Database (Docker FREEPDB1:1521)" : "Oracle Database Engine");
        dbLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + (isOracle ? "#4ade80" : "#38bdf8") + ";");
        dbBadge.getChildren().addAll(statusDot, dbLabel);
        dbBadge.getStyleClass().add("badge-db-oracle");

        // 3. Login Input Form
        VBox form = new VBox(12);
        form.setAlignment(Pos.CENTER_LEFT);

        Label userPrompt = new Label("Username");
        userPrompt.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #cbd5e1;");
        usernameField = new TextField("admin");
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefHeight(38);

        Label passPrompt = new Label("Password");
        passPrompt.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #cbd5e1;");
        passwordField = new PasswordField();
        passwordField.setText("admin123");
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(38);

        form.getChildren().addAll(userPrompt, usernameField, passPrompt, passwordField);

        // 4. Action Buttons & Feedback
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px; -fx-font-weight: bold;");
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);

        loginButton = new Button("Sign In to FinCore");
        loginButton.getStyleClass().addAll("button", "btn-primary");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setPrefHeight(42);
        loginButton.setOnAction(e -> handleLogin());

        // Enable pressing Enter to submit
        usernameField.setOnAction(e -> handleLogin());
        passwordField.setOnAction(e -> handleLogin());

        // 5. Quick Demo Credentials
        VBox demoBox = new VBox(8);
        demoBox.setAlignment(Pos.CENTER);
        Label demoHeader = new Label("Quick Demonstration Profiles:");
        demoHeader.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        HBox demoButtons = new HBox(8);
        demoButtons.setAlignment(Pos.CENTER);

        Button btnAdmin = createDemoPill("Admin (Full CRUD)", "admin", "admin123");
        Button btnManager = createDemoPill("Manager (asmit)", "asmit", "password123");
        Button btnUser = createDemoPill("Customer (alice)", "alice", "alice123");

        demoButtons.getChildren().addAll(btnAdmin, btnManager, btnUser);
        demoBox.getChildren().addAll(demoHeader, demoButtons);

        // Assembly
        card.getChildren().addAll(logoIcon, titleLabel, subtitleLabel, dbBadge, form, errorLabel, loginButton, demoBox);
        getChildren().add(card);
        setAlignment(Pos.CENTER);
    }

    private Button createDemoPill(String label, String username, String password) {
        Button btn = new Button(label);
        btn.getStyleClass().addAll("button", "btn-secondary", "btn-pill");
        btn.setOnAction(e -> {
            usernameField.setText(username);
            passwordField.setText(password);
            handleLogin();
        });
        return btn;
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        try {
            AuthUser user = authService.login(username, password);
            if (user != null) {
                errorLabel.setVisible(false);
                onLoginSuccess.accept(user);
            } else {
                showError("Invalid credentials or account is locked.");
            }
        } catch (Exception ex) {
            showError("Authentication error: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
