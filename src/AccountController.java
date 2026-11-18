import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class AccountController {
    @FXML
    private ListView<String> accountListView;

    @FXML
    private Label balanceLabel;

    @FXML
    private ListView<String> transactionListView;

    private final Map<String, AccountRecord> accountsByName = new HashMap<>();

    @FXML
    private void initialize() {
        try {
            Customer customer = new CustomerDAO().findById("C001");

            if (customer == null) {
                accountListView.getItems().setAll("Customer C001 was not found.");
                return;
            }

            for (AccountRecord account :
                    new AccountDAO().findAllForCustomer(customer.getCustomerId())) {
                String displayName = account.accountNumber()
                        + " - " + account.accountType();

                accountsByName.put(displayName, account);
                accountListView.getItems().add(displayName);
            }

            accountListView.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((observable, oldValue, newValue) ->
                            showAccount(newValue));

            if (accountListView.getItems().isEmpty()) {
                accountListView.getItems().add("No accounts found.");
            } else {
                accountListView.getSelectionModel().selectFirst();
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
                    new TransactionDAO().findAllForAccount(account.accountNumber())) {
                transactionListView.getItems().add(
                        transaction.dateTime()
                                + " | " + transaction.type()
                                + " | BWP " + transaction.amount()
                                + " | Balance: BWP "
                                + transaction.balanceAfter());
            }
        } catch (SQLException e) {
            transactionListView.getItems().setAll("Could not load transactions.");
        }
    }
}