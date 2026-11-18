import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class LoginDAO {
    private static final String DEMO_USERNAME = "C001";
    private static final String DEMO_PASSWORD = "Bank123!";
    private static final String DEMO_CUSTOMER_ID = "C001";
    private static final int MAX_ATTEMPTS = 3;
    private static final int HASH_ITERATIONS = 120_000;
    private static final int HASH_BITS = 256;

    public void ensureDemoUser() throws SQLException {
        String findSql =
                "SELECT 1 FROM login_users WHERE username = ?";
        String insertSql = """
                INSERT INTO login_users (
                    username, password_hash, salt, role, customer_id
                )
                VALUES (?, ?, ?, 'CUSTOMER', ?)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement find = connection.prepareStatement(findSql)) {
            find.setString(1, DEMO_USERNAME);

            try (ResultSet results = find.executeQuery()) {
                if (results.next()) {
                    return;
                }
            }

            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);

            String encodedSalt = Base64.getEncoder().encodeToString(salt);
            String encodedHash = Base64.getEncoder().encodeToString(
                    hashPassword(DEMO_PASSWORD.toCharArray(), salt));

            try (PreparedStatement insert =
                         connection.prepareStatement(insertSql)) {
                insert.setString(1, DEMO_USERNAME);
                insert.setString(2, encodedHash);
                insert.setString(3, encodedSalt);
                insert.setString(4, DEMO_CUSTOMER_ID);
                insert.executeUpdate();
            }
        }
    }

    public AuthResult authenticate(String username, String password)
            throws SQLException {
        String sql = """
                SELECT password_hash, salt, role, customer_id,
                       failed_attempts, locked
                FROM login_users
                WHERE username = ?
                """;

        String savedHash;
        String savedSalt;
        String role;
        String customerId;
        int failedAttempts;
        boolean locked;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);

            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return new AuthResult(false, false, null, null,
                            MAX_ATTEMPTS);
                }

                savedHash = results.getString("password_hash");
                savedSalt = results.getString("salt");
                role = results.getString("role");
                customerId = results.getString("customer_id");
                failedAttempts = results.getInt("failed_attempts");
                locked = results.getInt("locked") == 1;
            }
        }

        if (locked) {
            return new AuthResult(false, true, null, role, 0);
        }

        byte[] salt = Base64.getDecoder().decode(savedSalt);
        byte[] expectedHash = Base64.getDecoder().decode(savedHash);
        byte[] actualHash = hashPassword(password.toCharArray(), salt);

        if (MessageDigest.isEqual(expectedHash, actualHash)) {
            resetLock(username);
            return new AuthResult(true, false, customerId, role,
                    MAX_ATTEMPTS);
        }

        int newFailures = failedAttempts + 1;
        boolean nowLocked = newFailures >= MAX_ATTEMPTS;

        String updateSql = """
                UPDATE login_users
                SET failed_attempts = ?, locked = ?
                WHERE username = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement update = connection.prepareStatement(updateSql)) {
            update.setInt(1, newFailures);
            update.setInt(2, nowLocked ? 1 : 0);
            update.setString(3, username);
            update.executeUpdate();
        }

        return new AuthResult(false, nowLocked, null, role,
                Math.max(0, MAX_ATTEMPTS - newFailures));
    }

    public void resetLock(String username) throws SQLException {
        String sql = """
                UPDATE login_users
                SET failed_attempts = 0, locked = 0
                WHERE username = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
    }

    private byte[] hashPassword(char[] password, byte[] salt)
            throws SQLException {
        PBEKeySpec spec =
                new PBEKeySpec(password, salt, HASH_ITERATIONS, HASH_BITS);

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new SQLException("Could not verify the password.", e);
        } finally {
            spec.clearPassword();
        }
    }

    public record AuthResult(
            boolean authenticated,
            boolean locked,
            String customerId,
            String role,
            int attemptsRemaining) {
    }
}