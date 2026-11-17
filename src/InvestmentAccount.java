import java.math.BigDecimal;
import java.time.YearMonth;

public class InvestmentAccount extends Account implements InterestBearing {
    private static final BigDecimal MINIMUM_OPENING_DEPOSIT =
            new BigDecimal("500.00");

    private static final BigDecimal MONTHLY_INTEREST_RATE =
            new BigDecimal("0.05"); // 5%

    public InvestmentAccount(
            String accountNumber,
            String branch,
            Customer owner,
            BigDecimal openingDeposit) {
        super(accountNumber, branch, owner);

        if (openingDeposit == null
                || openingDeposit.compareTo(MINIMUM_OPENING_DEPOSIT) < 0) {
            throw new IllegalArgumentException(
                    "An Investment Account requires an opening deposit of at least BWP 500.00.");
        }

        deposit(openingDeposit);
    }

    @Override
    public String getAccountType() {
        return "Investment";
    }

    @Override
    protected boolean allowsWithdrawals() {
        return true;
    }

    @Override
    public void payMonthlyInterest(YearMonth month) {
        postMonthlyInterest(MONTHLY_INTEREST_RATE, month);
    }
}