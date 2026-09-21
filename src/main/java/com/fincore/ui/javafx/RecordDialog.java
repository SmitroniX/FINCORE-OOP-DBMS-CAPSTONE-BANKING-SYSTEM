package com.fincore.ui.javafx;

import com.fincore.model.FinancialRecord;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

/**
 * JavaFX 21 Modal Dialog for INSERT and UPDATE operations on Financial Records.
 * Demonstrates CRUD data capture, validation, and domain object construction.
 */
public class RecordDialog extends Dialog<FinancialRecord> {

    private final ToggleGroup typeGroup;
    private final RadioButton incomeRadio;
    private final RadioButton expenseRadio;
    private final ComboBox<String> categoryCombo;
    private final TextField amountField;
    private final ComboBox<String> accountCombo;
    private final TextField descriptionField;
    private final DatePicker datePicker;

    public RecordDialog(FinancialRecord recordToEdit, List<String> availableAccounts) {
        setTitle(recordToEdit == null ? "Add Financial Record (INSERT DML)" : "Edit Financial Record (UPDATE DML)");
        setHeaderText(recordToEdit == null ? "Insert a new Income or Expense record into Oracle DB" : "Modify existing record #" + recordToEdit.getId());

        // Dialog Pane styling
        DialogPane dialogPane = getDialogPane();
        dialogPane.getStyleClass().add("kpi-card");
        dialogPane.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());

        // Buttons
        ButtonType saveButtonType = new ButtonType(recordToEdit == null ? "Insert Record" : "Update Record", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        Button saveButton = (Button) dialogPane.lookupButton(saveButtonType);
        saveButton.getStyleClass().addAll("button", "btn-primary");

        // Form Layout
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);
        grid.setPadding(new Insets(16, 20, 16, 20));

        // 1. Record Type
        Label typeLabel = new Label("Record Type:*");
        typeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #cbd5e1;");
        typeGroup = new ToggleGroup();
        incomeRadio = new RadioButton("INCOME (+)");
        incomeRadio.setToggleGroup(typeGroup);
        incomeRadio.setStyle("-fx-text-fill: #34d399; -fx-font-weight: bold;");

        expenseRadio = new RadioButton("EXPENSE (-)");
        expenseRadio.setToggleGroup(typeGroup);
        expenseRadio.setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");
        expenseRadio.setSelected(true);

        HBox typeBox = new HBox(16, expenseRadio, incomeRadio);
        grid.add(typeLabel, 0, 0);
        grid.add(typeBox, 1, 0);

        // 2. Category
        Label catLabel = new Label("Category:*");
        catLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #cbd5e1;");
        categoryCombo = new ComboBox<>();
        categoryCombo.setEditable(true);
        categoryCombo.getItems().addAll(
                "Housing", "Groceries", "Utilities", "Entertainment", "Healthcare",
                "Transportation", "Salary", "Consulting", "Investment", "Miscellaneous"
        );
        categoryCombo.setValue("Groceries");
        categoryCombo.setPrefWidth(260);
        grid.add(catLabel, 0, 1);
        grid.add(categoryCombo, 1, 1);

        // 3. Amount
        Label amountLabel = new Label("Amount ($):*");
        amountLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #cbd5e1;");
        amountField = new TextField("50.00");
        amountField.setPromptText("e.g. 150.00");
        grid.add(amountLabel, 0, 2);
        grid.add(amountField, 1, 2);

        // 4. Linked Account
        Label accLabel = new Label("Linked Account:");
        accLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #cbd5e1;");
        accountCombo = new ComboBox<>();
        accountCombo.getItems().add("-- No Account --");
        if (availableAccounts != null) {
            accountCombo.getItems().addAll(availableAccounts);
        }
        accountCombo.setValue(accountCombo.getItems().get(accountCombo.getItems().size() > 1 ? 1 : 0));
        accountCombo.setPrefWidth(260);
        grid.add(accLabel, 0, 3);
        grid.add(accountCombo, 1, 3);

        // 5. Description
        Label descLabel = new Label("Description:");
        descLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #cbd5e1;");
        descriptionField = new TextField();
        descriptionField.setPromptText("Optional notes or description");
        grid.add(descLabel, 0, 4);
        grid.add(descriptionField, 1, 4);

        // 6. Date
        Label dateLabel = new Label("Date:*");
        dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #cbd5e1;");
        datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(260);
        grid.add(dateLabel, 0, 5);
        grid.add(datePicker, 1, 5);

        // Populate fields if editing
        if (recordToEdit != null) {
            if (recordToEdit.getRecordType() == FinancialRecord.Type.INCOME) {
                incomeRadio.setSelected(true);
            } else {
                expenseRadio.setSelected(true);
            }
            categoryCombo.setValue(recordToEdit.getCategory());
            amountField.setText(String.format("%.2f", recordToEdit.getAmount()));
            if (recordToEdit.getAccountNumber() != null && !recordToEdit.getAccountNumber().isEmpty()) {
                accountCombo.setValue(recordToEdit.getAccountNumber());
            }
            descriptionField.setText(recordToEdit.getDescription());
            if (recordToEdit.getRecordDate() != null) {
                datePicker.setValue(recordToEdit.getRecordDate());
            }
        }

        dialogPane.setContent(grid);

        // Result Converter
        setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                FinancialRecord.Type type = incomeRadio.isSelected() ? FinancialRecord.Type.INCOME : FinancialRecord.Type.EXPENSE;
                String cat = categoryCombo.getValue();
                if (cat == null || cat.trim().isEmpty()) {
                    cat = "General";
                }
                double amt = 0.0;
                try {
                    amt = Double.parseDouble(amountField.getText().trim().replace("$", "").replace(",", ""));
                } catch (NumberFormatException ex) {
                    amt = 1.0;
                }
                String acc = accountCombo.getValue();
                if ("-- No Account --".equals(acc)) {
                    acc = null;
                }
                String desc = descriptionField.getText().trim();
                LocalDate dt = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();

                Long id = recordToEdit != null ? recordToEdit.getId() : null;
                return new FinancialRecord(id, type, cat, amt, acc, desc, dt, null);
            }
            return null;
        });
    }
}
