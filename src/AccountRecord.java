import java.math.BigDecimal;

public record AccountRecord(
        String accountNumber,
        String accountType,
        String branch,
        String customerId,
        String dateOpened,
        BigDecimal balance) {
}