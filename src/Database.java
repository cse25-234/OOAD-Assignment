import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String DATABASE_URL = "jdbc:sqlite:banking.db";

    public static void initialize() {
        String[] createTableStatements = {
            """
            CREATE TABLE IF NOT EXISTS customers (
                customer_id TEXT PRIMARY KEY,
                full_name TEXT NOT NULL,
                address TEXT NOT NULL,
                employed INTEGER NOT NULL,
                employer_name TEXT,
                employer_address TEXT
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS login_users (
                username TEXT PRIMARY KEY,
                password_hash TEXT NOT NULL,
                salt TEXT NOT NULL,
                role TEXT NOT NULL CHECK (role IN ('CUSTOMER', 'ADMIN')),
                customer_id TEXT,
                failed_attempts INTEGER NOT NULL DEFAULT 0,
                locked INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS accounts (
                account_number TEXT PRIMARY KEY,
                account_type TEXT NOT NULL
                    CHECK (account_type IN ('Savings', 'Investment', 'Cheque')),
                branch TEXT NOT NULL,
                customer_id TEXT NOT NULL,
                date_opened TEXT NOT NULL,
                balance TEXT NOT NULL,
                FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS transactions (
                transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
                account_number TEXT NOT NULL,
                type TEXT NOT NULL,
                amount TEXT NOT NULL,
                balance_after TEXT NOT NULL,
                date_time TEXT NOT NULL,
                FOREIGN KEY (account_number)
                    REFERENCES accounts(account_number)
                    ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS interest_payments (
                account_number TEXT NOT NULL,
                year_month TEXT NOT NULL,
                PRIMARY KEY (account_number, year_month),
                FOREIGN KEY (account_number)
                    REFERENCES accounts(account_number)
                    ON DELETE CASCADE
            )
            """
        };

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            for (String sql : createTableStatements) {
                statement.execute(sql);
            }

            System.out.println("SQLite database and tables are ready.");

        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize the database.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        System.out.println("SQLite database path: "
                + new java.io.File("banking.db").getAbsolutePath());

        Connection connection = DriverManager.getConnection(DATABASE_URL);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }

        return connection;
    }
}