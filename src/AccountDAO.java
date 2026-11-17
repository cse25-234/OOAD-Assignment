import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    public boolean exists(String accountNumber) throws SQLException {
        String sql = "SELECT 1 FROM accounts WHERE account_number = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, accountNumber);

            try (ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        }
    }

    public void save(Account account) throws SQLException {
        String sql = """
                INSERT INTO accounts (
                    account_number, account_type, branch,
                    customer_id, date_opened, balance
                )
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(account_number) DO UPDATE SET
                    account_type = excluded.account_type,
                    branch = excluded.branch,
                    customer_id = excluded.customer_id,
                    balance = excluded.balance
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, account.getAccountNumber());
            statement.setString(2, account.getAccountType());
            statement.setString(3, account.getBranch());
            statement.setString(4, account.getOwner().getCustomerId());
            statement.setString(5, account.getDateOpened().toString());
            statement.setString(6, account.getBalance().toPlainString());

            statement.executeUpdate();
        }
    }

    public List<AccountRecord> findAllForCustomer(String customerId)
            throws SQLException {
        String sql = """
                SELECT account_number, account_type, branch,
                       customer_id, date_opened, balance
                FROM accounts
                WHERE customer_id = ?
                ORDER BY account_number
                """;

        List<AccountRecord> accounts = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customerId);

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    accounts.add(new AccountRecord(
                            results.getString("account_number"),
                            results.getString("account_type"),
                            results.getString("branch"),
                            results.getString("customer_id"),
                            results.getString("date_opened"),
                            new BigDecimal(results.getString("balance"))));
                }
            }
        }

        return accounts;
    }
}