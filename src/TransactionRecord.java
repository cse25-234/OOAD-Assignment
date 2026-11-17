import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionRecord(
        String type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        LocalDateTime dateTime) {
}