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
        String deleteSql =
                "DELETE FROM transactions WHERE account_number = ?";
        String insertSql = """
                INSERT INTO transactions (
                    account_number, type, amount, balance_after, date_time
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement delete =
                         connection.prepareStatement(deleteSql);
                 PreparedStatement insert =
                         connection.prepareStatement(insertSql)) {

                delete.setString(1, account.getAccountNumber());
                delete.executeUpdate();

                for (Transaction transaction : account.getTransactions()) {
                    insert.setString(1, account.getAccountNumber());
                    insert.setString(2, transaction.getType());
                    insert.setString(3, transaction.getAmount().toPlainString());
                    insert.setString(
                            4, transaction.getBalanceAfter().toPlainString());
                    insert.setString(5, transaction.getDateTime().toString());
                    insert.executeUpdate();
                }

                connection.commit();
            } catch (SQLException | RuntimeException e) {
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
                ORDER BY date_time, transaction_id
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
                            LocalDateTime.parse(
                                    results.getString("date_time"))));
                }
            }
        }

        return transactions;
    }
}