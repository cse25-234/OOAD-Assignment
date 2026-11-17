import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private final String type;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final LocalDateTime dateTime;

    public Transaction(String type, BigDecimal amount, BigDecimal balanceAfter) {
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.dateTime = LocalDateTime.now();
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return dateTime + " | " + type + " | BWP " + amount
                + " | Balance after: BWP " + balanceAfter;
    }
}