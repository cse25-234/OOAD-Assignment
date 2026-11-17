import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    public void saveAll(Account account) throws SQLException {
        String sql = """
                INSERT INTO transactions (
                    account_number, type, amount, balance_after, date_time
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (Transaction transaction : account.getTransactions()) {
                    statement.setString(1, account.getAccountNumber());
                    statement.setString(2, transaction.getType());
                    statement.setString(3, transaction.getAmount().toPlainString());
                    statement.setString(4, transaction.getBalanceAfter().toPlainString());
                    statement.setString(5, transaction.getDateTime().toString());
                    statement.addBatch();
                }

                statement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public List<TransactionRecord> findAllForAccount(String accountNumber)
            throws SQLException {
        String sql = """
                SELECT type, amount, balance_after, date_time
                FROM transactions
                WHERE account_number = ?
                ORDER BY transaction_id
                """;

        List<TransactionRecord> transactions = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, accountNumber);

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    transactions.add(new TransactionRecord(
                            results.getString("type"),
                            new BigDecimal(results.getString("amount")),
                            new BigDecimal(results.getString("balance_after")),
                            LocalDateTime.parse(results.getString("date_time"))));
                }
            }
        }

        return transactions;
    }
}