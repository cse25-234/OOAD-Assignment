import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.math.RoundingMode;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ChoiceDialog;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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

    @FXML
private Button openAccountButton;

@FXML
private Button logoutButton;

    private final Map<String, AccountRecord> accountsByName = new HashMap<>();

    @FXML
    private void initialize() {
        depositButton.setOnAction(event -> handleDeposit());
        withdrawButton.setOnAction(event -> handleWithdrawal());
        openAccountButton.setOnAction(event -> handleOpenAccount());
        logoutButton.setOnAction(event -> handleLogout());

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
   private void handleOpenAccount() {
    ChoiceDialog<String> typeDialog =
            new ChoiceDialog<>("Savings", List.of("Savings", "Investment", "Cheque"));
    typeDialog.setTitle("Open Account");
    typeDialog.setHeaderText("Choose an account type");
    typeDialog.setContentText("Account type:");

    Optional<String> choice = typeDialog.showAndWait();

    if (choice.isEmpty()) {
        return;
    }

    try {
        Customer customer = new CustomerDAO().findById("C001");

        if (customer == null) {
            showMessage(Alert.AlertType.ERROR, "Customer not found",
                    "Customer C001 could not be found.");
            return;
        }

        String accountNumber = nextAccountNumber();
        Account newAccount;

        switch (choice.get()) {
            case "Savings" ->
                    newAccount = new SavingsAccount(
                            accountNumber, "Main Branch", customer);

            case "Investment" -> {
                TextInputDialog depositDialog =
                        new TextInputDialog("500.00");
                depositDialog.setTitle("Investment Opening Deposit");
                depositDialog.setHeaderText(
                        "An Investment account needs at least BWP 500.00.");
                depositDialog.setContentText("Opening deposit in BWP:");

                Optional<String> depositInput =
                        depositDialog.showAndWait();

                if (depositInput.isEmpty()) {
                    return;
                }

                BigDecimal openingDeposit;
                try {
                    openingDeposit = new BigDecimal(
                            depositInput.get().trim())
                            .setScale(2, RoundingMode.UNNECESSARY);
                } catch (NumberFormatException | ArithmeticException e) {
                    showMessage(Alert.AlertType.ERROR, "Invalid amount",
                            "Enter a valid amount with no more than two decimal places.");
                    return;
                }

                newAccount = new InvestmentAccount(
                        accountNumber, "Main Branch", customer,
                        openingDeposit);
            }

            case "Cheque" ->
                    newAccount = new ChequeAccount(
                            accountNumber, "Main Branch", customer);

            default -> {
                return;
            }
        }

        new AccountDAO().save(newAccount);
        new TransactionDAO().saveAll(newAccount);
        loadAccounts(accountNumber);

        showMessage(Alert.AlertType.INFORMATION, "Account opened",
                "Account " + accountNumber + " was created.");
    } catch (SQLException | IllegalArgumentException e) {
        showMessage(Alert.AlertType.ERROR, "Could not open account",
                e.getMessage());
    }
}

private String nextAccountNumber() throws SQLException {
    AccountDAO accountDAO = new AccountDAO();

    for (int number = 1001; number < 1_000_000; number++) {
        String candidate = "A" + number;

        if (!accountDAO.exists(candidate)) {
            return candidate;
        }
    }

    throw new SQLException("Could not generate an account number.");
}
private void handleLogout() {
    try {
        Parent loginView =
                FXMLLoader.load(getClass().getResource("/LoginView.fxml"));
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.setScene(new Scene(loginView));
        stage.setTitle("Banking System - Login");
    } catch (IOException | NullPointerException e) {
        showMessage(Alert.AlertType.ERROR, "Logout error",
                "Could not return to the login screen.");
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
