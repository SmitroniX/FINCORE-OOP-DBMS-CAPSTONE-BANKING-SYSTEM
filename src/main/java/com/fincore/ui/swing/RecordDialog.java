package com.fincore.ui.swing;

import com.fincore.model.Account;
import com.fincore.model.FinancialRecord;
import com.fincore.service.FinanceService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Modal dialog for INSERT (Add) and UPDATE (Edit) Financial Records.
 */
public class RecordDialog extends JDialog {

    private final FinanceService financeService;
    private final FinancialRecord existingRecord;
    private boolean saved = false;

    private JComboBox<FinancialRecord.Type> typeCombo;
    private JComboBox<String> categoryCombo;
    private JTextField amountField;
    private JComboBox<String> accountCombo;
    private JTextField descriptionField;
    private JTextField dateField;

    public RecordDialog(Frame owner, FinanceService financeService, FinancialRecord recordToEdit) {
        super(owner, recordToEdit == null ? "Add Financial Record (INSERT)" : "Edit Financial Record (UPDATE)", true);
        this.financeService = financeService;
        this.existingRecord = recordToEdit;

        setSize(460, 380);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        initUI();
    }

    private void initUI() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // 1. Record Type
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Record Type:"), gbc);
        typeCombo = new JComboBox<>(FinancialRecord.Type.values());
        gbc.gridx = 1;
        formPanel.add(typeCombo, gbc);

        // 2. Category
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Category:"), gbc);
        String[] defaultCategories = {
                "Salary", "Consulting", "Investments", "Housing",
                "Groceries", "Utilities", "Entertainment", "Healthcare", "Education", "Travel"
        };
        categoryCombo = new JComboBox<>(defaultCategories);
        categoryCombo.setEditable(true);
        gbc.gridx = 1;
        formPanel.add(categoryCombo, gbc);

        // 3. Amount
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Amount ($):"), gbc);
        amountField = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(amountField, gbc);

        // 4. Account
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Account:"), gbc);
        accountCombo = new JComboBox<>();
        List<Account> accounts = financeService.getAllAccounts();
        for (Account a : accounts) {
            accountCombo.addItem(a.getAccountNumber() + " (" + a.getAccountType() + ")");
        }
        if (accounts.isEmpty()) {
            accountCombo.addItem("CHK-100102 (CHECKING)");
        }
        gbc.gridx = 1;
        formPanel.add(accountCombo, gbc);

        // 5. Description
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Description:"), gbc);
        descriptionField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(descriptionField, gbc);

        // 6. Date
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        dateField = new JTextField(LocalDate.now().toString(), 15);
        gbc.gridx = 1;
        formPanel.add(dateField, gbc);

        // Populate fields if editing
        if (existingRecord != null) {
            typeCombo.setSelectedItem(existingRecord.getRecordType());
            categoryCombo.setSelectedItem(existingRecord.getCategory());
            amountField.setText(String.format("%.2f", existingRecord.getAmount()));
            descriptionField.setText(existingRecord.getDescription());
            dateField.setText(existingRecord.getRecordDate().toString());
            // match account
            for (int i = 0; i < accountCombo.getItemCount(); i++) {
                if (accountCombo.getItemAt(i).startsWith(existingRecord.getAccountNumber())) {
                    accountCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton saveBtn = new JButton(existingRecord == null ? "Save (INSERT)" : "Update (UPDATE)");
        saveBtn.setBackground(new Color(25, 118, 210));
        saveBtn.setForeground(Color.BLACK);
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 12));

        JButton cancelBtn = new JButton("Cancel");

        saveBtn.addActionListener(e -> onSave());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void onSave() {
        try {
            FinancialRecord.Type type = (FinancialRecord.Type) typeCombo.getSelectedItem();
            String category = (String) categoryCombo.getSelectedItem();
            if (category == null || category.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Category is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String amountText = amountField.getText().trim().replace("$", "").replace(",", "");
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be strictly positive.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String rawAcc = (String) accountCombo.getSelectedItem();
            String accountNum = rawAcc != null ? rawAcc.split(" ")[0] : "CHK-100102";
            String description = descriptionField.getText().trim();
            LocalDate date;
            try {
                date = LocalDate.parse(dateField.getText().trim());
            } catch (Exception ex) {
                date = LocalDate.now();
            }

            if (existingRecord == null) {
                // INSERT operation
                financeService.addRecord(type, category, amount, accountNum, description, date);
            } else {
                // UPDATE operation
                existingRecord.setRecordType(type);
                existingRecord.setCategory(category);
                existingRecord.setAmount(amount);
                existingRecord.setAccountNumber(accountNum);
                existingRecord.setDescription(description);
                existingRecord.setRecordDate(date);
                financeService.updateRecord(existingRecord);
            }

            saved = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount format. Please enter a valid number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving record: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
