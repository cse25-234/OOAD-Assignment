import java.math.BigDecimal;
import java.time.YearMonth;

public class SavingsAccount extends Account implements InterestBearing {
    private static final BigDecimal MONTHLY_INTEREST_RATE =
            new BigDecimal("0.0005"); // 0.05%

    public SavingsAccount(String accountNumber, String branch, Customer owner) {
        super(accountNumber, branch, owner);
    }

    @Override
    public String getAccountType() {
        return "Savings";
    }

    @Override
    public void payMonthlyInterest(YearMonth month) {
        postMonthlyInterest(MONTHLY_INTEREST_RATE, month);
    }
}