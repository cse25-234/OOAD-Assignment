import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;

public class AccountController {

    @FXML
    private ListView<String> accountListView;

    @FXML
    private Label balanceLabel;

    @FXML
    private ListView<String> transactionListView;

    @FXML
    private Button depositButton;

    @FXML
    private Button withdrawButton;

    private final Map<String, AccountRecord> accountsByName = new HashMap<>();

    @FXML
    private void initialize() {
        depositButton.setOnAction(event -> handleDeposit());
        withdrawButton.setOnAction(event -> handleWithdrawal());

        accountListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) ->
                        showAccount(newValue));

        loadAccounts(null);
    }

    private void loadAccounts(String accountNumberToSelect) {
        accountsByName.clear();
        accountListView.getItems().clear();

        try {
            Customer customer = new CustomerDAO().findById("C001");

            if (customer == null) {
                accountListView.getItems().setAll("Customer C001 was not found.");
                balanceLabel.setText("Balance: BWP 0.00");
                transactionListView.getItems().clear();
                return;
            }

            for (AccountRecord account :
                    new AccountDAO().findAllForCustomer(customer.getCustomerId())) {
                String displayName =
                        account.accountNumber() + " - " + account.accountType();

                accountsByName.put(displayName, account);
                accountListView.getItems().add(displayName);
            }

            if (accountListView.getItems().isEmpty()) {
                accountListView.getItems().add("No accounts found.");
                balanceLabel.setText("Balance: BWP 0.00");
                transactionListView.getItems().clear();
                return;
            }

            String nameToSelect = null;
            for (Map.Entry<String, AccountRecord> entry
                    : accountsByName.entrySet()) {
                if (entry.getValue().accountNumber()
                        .equals(accountNumberToSelect)) {
                    nameToSelect = entry.getKey();
                    break;
                }
            }

            if (nameToSelect == null) {
                accountListView.getSelectionModel().selectFirst();
            } else {
                accountListView.getSelectionModel().select(nameToSelect);
            }
        } catch (SQLException e) {
            accountListView.getItems().setAll("Could not load accounts.");
            transactionListView.getItems().setAll(e.getMessage());
        }
    }

    private void showAccount(String displayName) {
        AccountRecord account = accountsByName.get(displayName);

        if (account == null) {
            return;
        }

        balanceLabel.setText("Balance: BWP " + account.balance().toPlainString());
        transactionListView.getItems().clear();

        try {
            for (TransactionRecord transaction :
                    new TransactionDAO().findAllForAccount(
                            account.accountNumber())) {
                transactionListView.getItems().add(
                        transaction.dateTime()
                                + " | " + transaction.type()
                                + " | BWP " + transaction.amount().toPlainString()
                                + " | Balance: BWP "
                                + transaction.balanceAfter().toPlainString());
            }
        } catch (SQLException e) {
            transactionListView.getItems()
                    .setAll("Could not load transactions.");
        }
    }

    private void handleDeposit() {
        String selectedName =
                accountListView.getSelectionModel().getSelectedItem();
        AccountRecord account = accountsByName.get(selectedName);

        if (account == null) {
            showMessage(Alert.AlertType.WARNING, "Select an account",
                    "Choose an account before making a deposit.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Deposit");
        dialog.setHeaderText("Deposit into " + account.accountNumber());
        dialog.setContentText("Amount in BWP:");

        Optional<String> input = dialog.showAndWait();

        if (input.isEmpty()) {
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(input.get().trim());
            new AccountDAO().deposit(account.accountNumber(), amount);

            loadAccounts(account.accountNumber());

            showMessage(Alert.AlertType.INFORMATION, "Deposit successful",
                    "The deposit was saved.");
        } catch (NumberFormatException e) {
            showMessage(Alert.AlertType.ERROR, "Invalid amount",
                    "Enter a number, for example 100 or 100.50.");
        } catch (IllegalArgumentException e) {
            showMessage(Alert.AlertType.ERROR, "Deposit not completed",
                    e.getMessage());
        } catch (SQLException e) {
            showMessage(Alert.AlertType.ERROR, "Database error",
                    "The deposit could not be saved: " + e.getMessage());
        }
    }

    private void handleWithdrawal() {
        String selectedName =
                accountListView.getSelectionModel().getSelectedItem();
        AccountRecord account = accountsByName.get(selectedName);

        if (account == null) {
            showMessage(Alert.AlertType.WARNING, "Select an account",
                    "Choose an account before making a withdrawal.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Withdraw");
        dialog.setHeaderText("Withdraw from " + account.accountNumber());
        dialog.setContentText("Amount in BWP:");

        Optional<String> input = dialog.showAndWait();

        if (input.isEmpty()) {
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(input.get().trim());
            new AccountDAO().withdraw(account.accountNumber(), amount);

            loadAccounts(account.accountNumber());

            showMessage(Alert.AlertType.INFORMATION, "Withdrawal successful",
                    "The withdrawal was saved.");
        } catch (NumberFormatException e) {
            showMessage(Alert.AlertType.ERROR, "Invalid amount",
                    "Enter a number, for example 100 or 100.50.");
        } catch (IllegalArgumentException e) {
            showMessage(Alert.AlertType.ERROR, "Withdrawal not completed",
                    e.getMessage());
        } catch (SQLException e) {
            showMessage(Alert.AlertType.ERROR, "Database error",
                    "The withdrawal could not be saved: " + e.getMessage());
        }
    }

    private void showMessage(
            Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
