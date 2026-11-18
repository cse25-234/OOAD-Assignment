import java.time.LocalDateTime;
import java.math.RoundingMode;
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
    public void deposit(String accountNumber, BigDecimal amount)
        throws SQLException {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException(
                "Deposit amount must be greater than zero.");
    }

    try {
        amount = amount.setScale(2, RoundingMode.UNNECESSARY);
    } catch (ArithmeticException e) {
        throw new IllegalArgumentException(
                "Enter no more than two decimal places.");
    }

    String selectSql =
            "SELECT balance FROM accounts WHERE account_number = ?";
    String updateSql =
            "UPDATE accounts SET balance = ? WHERE account_number = ?";
    String insertSql = """
            INSERT INTO transactions (
                account_number, type, amount, balance_after, date_time
            )
            VALUES (?, 'DEPOSIT', ?, ?, ?)
            """;

    try (Connection connection = Database.getConnection()) {
        connection.setAutoCommit(false);

        try {
            BigDecimal currentBalance;

            try (PreparedStatement select =
                         connection.prepareStatement(selectSql)) {
                select.setString(1, accountNumber);

                try (ResultSet results = select.executeQuery()) {
                    if (!results.next()) {
                        throw new IllegalArgumentException(
                                "Account was not found.");
                    }

                    currentBalance = new BigDecimal(
                            results.getString("balance"));
                }
            }

            BigDecimal newBalance = currentBalance.add(amount);

            try (PreparedStatement update =
                         connection.prepareStatement(updateSql)) {
                update.setString(1, newBalance.toPlainString());
                update.setString(2, accountNumber);
                update.executeUpdate();
            }

            try (PreparedStatement insert =
                         connection.prepareStatement(insertSql)) {
                insert.setString(1, accountNumber);
                insert.setString(2, amount.toPlainString());
                insert.setString(3, newBalance.toPlainString());
                insert.setString(4, LocalDateTime.now().toString());
                insert.executeUpdate();
            }

            connection.commit();
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            throw e;
        }
    }
}
}