import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerDAO {
    public void save(Customer customer) throws SQLException {
        String sql = """
                INSERT INTO customers (
                    customer_id,
                    full_name,
                    address,
                    employed,
                    employer_name,
                    employer_address
                )
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(customer_id) DO UPDATE SET
                    full_name = excluded.full_name,
                    address = excluded.address,
                    employed = excluded.employed,
                    employer_name = excluded.employer_name,
                    employer_address = excluded.employer_address
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customer.getCustomerId());
            statement.setString(2, customer.getFullName());
            statement.setString(3, customer.getAddress());
            statement.setInt(4, customer.isEmployed() ? 1 : 0);
            statement.setString(5, customer.getEmployerName());
            statement.setString(6, customer.getEmployerAddress());

            statement.executeUpdate();
        }
    }

    public Customer findById(String customerId) throws SQLException {
        String sql = """
                SELECT customer_id, full_name, address, employed,
                       employer_name, employer_address
                FROM customers
                WHERE customer_id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customerId);

            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return null;
                }

                return new Customer(
                        results.getString("customer_id"),
                        results.getString("full_name"),
                        results.getString("address"),
                        results.getInt("employed") == 1,
                        results.getString("employer_name"),
                        results.getString("employer_address"));
            }
        }
    }
}