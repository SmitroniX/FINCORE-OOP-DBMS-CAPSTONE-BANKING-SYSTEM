package com.fincore.ui.javafx;

import com.fincore.model.Account;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JavaFX 21 Modal Dialog for initiating ACID Bank Transfers via PL/SQL Package.
 * Demonstrates Stored Procedure invocation (PKG_BANKING_OPERATIONS.TRANSFER_FUNDS).
 */
public class TransferDialog extends Dialog<TransferDialog.TransferRequest> {

    public record TransferRequest(String fromAccount, String toAccount, double amount, String remarks) {}

    private final ComboBox<String> fromCombo;
    private final ComboBox<String> toCombo;
    private final TextField amountField;
    private final TextField remarksField;
    private final Label balanceHintLabel;

    public TransferDialog(List<Account> accounts) {
        setTitle("Bank Fund Transfer (PL/SQL Stored Procedure)");
        setHeaderText("Execute atomic fund transfer via PKG_BANKING_OPERATIONS.TRANSFER_FUNDS");

        DialogPane dialogPane = getDialogPane();
        dialogPane.getStyleClass().add("kpi-card");
        dialogPane.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());

        ButtonType transferBtnType = new ButtonType("Execute Transfer", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(transferBtnType, ButtonType.CANCEL);

        Button transferBtn = (Button) dialogPane.lookupButton(transferBtnType);
        transferBtn.getStyleClass().addAll("button", "btn-primary");

        Map<String, Account> accountMap = accounts.stream()
                .collect(Collectors.toMap(Account::getAccountNumber, a -> a, (a1, a2) -> a1));

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);
        grid.setPadding(new Insets(16, 20, 16, 20));

        // Source Account
        Label fromLabel = new Label("From Account:*");
        fromLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #cbd5e1;");
        fromCombo = new ComboBox<>();
        accounts.forEach(a -> fromCombo.getItems().add(a.getAccountNumber()));
        if (!accounts.isEmpty()) {
            fromCombo.setValue(accounts.get(0).getAccountNumber());
        }
        grid.add(fromLabel, 0, 0);
        grid.add(fromCombo, 1, 0);

        // Balance Hint
        balanceHintLabel = new Label();
        balanceHintLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #34d399; -fx-font-weight: bold;");
        updateBalanceHint(accountMap);
        fromCombo.setOnAction(e -> updateBalanceHint(accountMap));
        grid.add(balanceHintLabel, 1, 1);

        // Destination Account
        Label toLabel = new Label("To Account:*");
        toLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #cbd5e1;");
        toCombo = new ComboBox<>();
        accounts.forEach(a -> toCombo.getItems().add(a.getAccountNumber()));
        if (accounts.size() > 1) {
            toCombo.setValue(accounts.get(1).getAccountNumber());
        }
        grid.add(toLabel, 0, 2);
        grid.add(toCombo, 1, 2);

        // Amount
        Label amtLabel = new Label("Transfer Amount ($):*");
        amtLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #cbd5e1;");
        amountField = new TextField("250.00");
        grid.add(amtLabel, 0, 3);
        grid.add(amountField, 1, 3);

        // Remarks
        Label remLabel = new Label("Remarks / Purpose:");
        remLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #cbd5e1;");
        remarksField = new TextField("Capstone Demonstration Transfer");
        grid.add(remLabel, 0, 4);
        grid.add(remarksField, 1, 4);

        dialogPane.setContent(grid);

        setResultConverter(buttonType -> {
            if (buttonType == transferBtnType) {
                String from = fromCombo.getValue();
                String to = toCombo.getValue();
                double amt = 0.0;
                try {
                    amt = Double.parseDouble(amountField.getText().trim().replace("$", "").replace(",", ""));
                } catch (Exception ignored) {
                }
                String remarks = remarksField.getText().trim();
                return new TransferRequest(from, to, amt, remarks);
            }
            return null;
        });
    }

    private void updateBalanceHint(Map<String, Account> accountMap) {
        String selected = fromCombo.getValue();
        if (selected != null && accountMap.containsKey(selected)) {
            Account acc = accountMap.get(selected);
            balanceHintLabel.setText(String.format("Available: $%.2f (%s)", acc.getBalance(), acc.getAccountType()));
        }
    }
}
