package com.fincore.ui.swing;

import com.fincore.db.DatabaseManager;
import com.fincore.model.AuthUser;
import com.fincore.service.AuthService;
import com.fincore.service.BankingService;
import com.fincore.service.CustomerService;
import com.fincore.service.FinanceService;
import com.fincore.service.ReportService;
import com.fincore.service.exception.BankingException;
import com.fincore.ui.DemoRunner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Java Swing Login Frame providing Database-backed Authentication (Oracle 10g XE / SQLite).
 * Connects to the database `users` table, verifies credentials with PreparedStatements,
 * and launches the FinanceDashboardFrame on successful login.
 */
public class LoginFrame extends JFrame {

    private final AuthService authService;
    private final FinanceService financeService;
    private final BankingService bankingService;
    private final CustomerService customerService;
    private final ReportService reportService;
    private final DemoRunner demoRunner;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JButton loginButton;

    public LoginFrame(AuthService authService,
                      FinanceService financeService,
                      BankingService bankingService,
                      CustomerService customerService,
                      ReportService reportService,
                      DemoRunner demoRunner) {
        super("FinCore Banking & Finance System - Login");
        this.authService = authService;
        this.financeService = financeService;
        this.bankingService = bankingService;
        this.customerService = customerService;
        this.reportService = reportService;
        this.demoRunner = demoRunner;

        setSize(520, 560);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 1. Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(22, 25, 20, 25));

        JLabel titleLabel = new JLabel("FinCore Banking System", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));

        String dbType = DatabaseManager.getInstance().getConfig().getDbType().toUpperCase();
        JLabel subLabel = new JLabel("Default DBMS: Oracle 10g XE | Active: " + dbType + " via JDBC", SwingConstants.CENTER);
        subLabel.setForeground(new Color(176, 196, 222));
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(Box.createVerticalStrut(6), BorderLayout.CENTER);
        headerPanel.add(subLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Form
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(25, 45, 20, 45));
        formPanel.setBackground(new Color(248, 249, 250));

        // Subheading
        JLabel formTitle = new JLabel("Sign In to Finance Dashboard");
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        formTitle.setForeground(new Color(33, 37, 41));
        formTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(formTitle);
        formPanel.add(Box.createVerticalStrut(18));

        // Username Field
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(userLabel);
        formPanel.add(Box.createVerticalStrut(4));

        usernameField = new JTextField("admin");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        usernameField.setPreferredSize(new Dimension(380, 34));
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        formPanel.add(usernameField);
        formPanel.add(Box.createVerticalStrut(12));

        // Password Field
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(passLabel);
        formPanel.add(Box.createVerticalStrut(4));

        passwordField = new JPasswordField("admin123");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        passwordField.setPreferredSize(new Dimension(380, 34));
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(10));

        // Status / Error label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(211, 47, 47));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(statusLabel);
        formPanel.add(Box.createVerticalStrut(10));

        // Login Button
        loginButton = new JButton("Login (Authenticate via DB)");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        loginButton.setBackground(new Color(25, 118, 210));
        loginButton.setForeground(Color.BLACK);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        loginButton.setPreferredSize(new Dimension(380, 38));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> performLogin());
        formPanel.add(loginButton);
        formPanel.add(Box.createVerticalStrut(15));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        formPanel.add(sep);
        formPanel.add(Box.createVerticalStrut(12));

        // Quick Login Helper Buttons
        JLabel quickLabel = new JLabel("Quick Login Presets (Database Seed):");
        quickLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        quickLabel.setForeground(Color.GRAY);
        quickLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(quickLabel);
        formPanel.add(Box.createVerticalStrut(8));

        JPanel quickPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        quickPanel.setOpaque(false);
        quickPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JButton quickAdminBtn = new JButton("admin / admin123");
        quickAdminBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        quickAdminBtn.addActionListener(e -> {
            usernameField.setText("admin");
            passwordField.setText("admin123");
            performLogin();
        });

        JButton quickAsmitBtn = new JButton("asmit / password123");
        quickAsmitBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        quickAsmitBtn.addActionListener(e -> {
            usernameField.setText("asmit");
            passwordField.setText("password123");
            performLogin();
        });

        quickPanel.add(quickAdminBtn);
        quickPanel.add(quickAsmitBtn);
        formPanel.add(quickPanel);
        formPanel.add(Box.createVerticalStrut(14));

        // Run Demo Runner Button
        JButton demoBtn = new JButton("🚀 Run Full Capstone Demonstration");
        demoBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        demoBtn.setBackground(new Color(46, 125, 50));
        demoBtn.setForeground(Color.BLACK);
        demoBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        demoBtn.setPreferredSize(new Dimension(380, 34));
        demoBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        demoBtn.addActionListener(e -> runCapstoneDemonstrationDialog());
        formPanel.add(demoBtn);

        // Enter key listeners
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        usernameField.addKeyListener(enterListener);
        passwordField.addKeyListener(enterListener);

        add(formPanel, BorderLayout.CENTER);

        // 3. Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(new Color(238, 238, 238));
        JLabel footerLabel = new JLabel("FinCore OOP-DBMS Capstone • Oracle 10g XE / SQLite • Java Swing & JDBC");
        footerLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        footerLabel.setForeground(Color.GRAY);
        footerPanel.add(footerLabel);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        try {
            statusLabel.setForeground(new Color(25, 118, 210));
            statusLabel.setText("Authenticating with database...");

            AuthUser user = authService.login(username, password);

            statusLabel.setForeground(new Color(46, 125, 50));
            statusLabel.setText("Login successful! Welcome, " + user.getFullName());

            // Open Dashboard
            SwingUtilities.invokeLater(() -> {
                FinanceDashboardFrame dashboard = new FinanceDashboardFrame(
                        authService,
                        financeService,
                        bankingService,
                        customerService,
                        reportService,
                        () -> {
                            // On logout, re-show this login window
                            passwordField.setText("");
                            statusLabel.setText("Logged out successfully.");
                            statusLabel.setForeground(Color.DARK_GRAY);
                            setVisible(true);
                        }
                );
                dashboard.setVisible(true);
                setVisible(false);
            });

        } catch (BankingException ex) {
            statusLabel.setForeground(new Color(211, 47, 47));
            statusLabel.setText(ex.getMessage());
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        } catch (Exception ex) {
            statusLabel.setForeground(new Color(211, 47, 47));
            statusLabel.setText("Database Connection Error: " + ex.getMessage());
        }
    }

    private void runCapstoneDemonstrationDialog() {
        JDialog demoDialog = new JDialog(this, "Capstone Live Demonstration (CRUD & SQL)", true);
        demoDialog.setSize(750, 520);
        demoDialog.setLocationRelativeTo(this);
        demoDialog.setLayout(new BorderLayout(8, 8));

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(20, 24, 33));
        logArea.setForeground(new Color(78, 201, 176));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(logArea);
        demoDialog.add(scroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> demoDialog.dispose());
        bottomPanel.add(closeBtn);
        demoDialog.add(bottomPanel, BorderLayout.SOUTH);

        // Execute demo in background thread and pipe output
        new Thread(() -> {
            logArea.append(">>> Launching FinCore Automated Capstone Demonstration...\n");
            try {
                if (demoRunner != null) {
                    demoRunner.runFullDemonstration();
                    logArea.append("\n>>> Capstone Demonstration completed successfully!\n");
                    logArea.append(">>> Check the terminal or console for detailed colored logs and SQL trace.\n");
                }
            } catch (Exception ex) {
                logArea.append("\n[ERROR] Demo encountered an exception: " + ex.getMessage() + "\n");
            }
        }).start();

        demoDialog.setVisible(true);
    }
}
