import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Account {
    private final String accountNumber;
    private final String branch;
    private final Customer owner;
    private final LocalDate dateOpened;
    private BigDecimal balance = BigDecimal.ZERO;
    private final List<Transaction> transactions = new ArrayList<>();
    private final Set<YearMonth> interestPaidMonths = new HashSet<>();

    protected Account(String accountNumber, String branch, Customer owner) {
        this.accountNumber = accountNumber;
        this.branch = branch;
        this.owner = owner;
        this.dateOpened = LocalDate.now();
    }

    public abstract String getAccountType();

    public void deposit(BigDecimal amount) {
        validatePositiveAmount(amount);

        balance = balance.add(amount);
        transactions.add(new Transaction("DEPOSIT", amount, balance));
    }

    public void withdraw(BigDecimal amount) {
        validatePositiveAmount(amount);

        if (!allowsWithdrawals()) {
            throw new IllegalStateException(
                    "Withdrawals are not allowed from a " + getAccountType() + " account.");
        }

        if (amount.compareTo(balance) > 0) {
            throw new IllegalArgumentException("Insufficient funds.");
        }

        balance = balance.subtract(amount);
        transactions.add(new Transaction("WITHDRAWAL", amount, balance));
    }

    protected boolean allowsWithdrawals() {
        return false;
    }

    protected final void postMonthlyInterest(BigDecimal rate, YearMonth month) {
        if (month == null) {
            throw new IllegalArgumentException("Interest month cannot be empty.");
        }

        if (!interestPaidMonths.add(month)) {
            throw new IllegalStateException(
                    "Interest has already been paid for " + month + ".");
        }

        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal interest = balance
                .multiply(rate)
                .setScale(2, RoundingMode.HALF_UP);

        if (interest.compareTo(BigDecimal.ZERO) > 0) {
            balance = balance.add(interest);
            transactions.add(new Transaction("INTEREST", interest, balance));
        }
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getBranch() {
        return branch;
    }

    public Customer getOwner() {
        return owner;
    }

    public LocalDate getDateOpened() {
        return dateOpened;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }
}