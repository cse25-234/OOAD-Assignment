import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.YearMonth;

public class Main {
    public static void main(String[] args) {
        Database.initialize();

        try {
            if (new AccountDAO().exists("A1001")) {
                printSavedData();
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not check the database.", e);
        }

        Customer customer = new Customer(
                "C001",
                "Thato Molefe",
                "Gaborone",
                true,
                "Example Company",
                "Gaborone");

        try {
            new CustomerDAO().save(customer);
        } catch (SQLException e) {
            throw new RuntimeException("Could not save the customer.", e);
        }

        Account savings = new SavingsAccount(
                "A1001",
                "Main Branch",
                customer);

        Account investment = new InvestmentAccount(
                "A1002",
                "Main Branch",
                customer,
                new BigDecimal("1000.00"));

        Account cheque = new ChequeAccount(
                "A1003",
                "Main Branch",
                customer);

        customer.addAccount(savings);
        customer.addAccount(investment);
        customer.addAccount(cheque);

        savings.deposit(new BigDecimal("500.00"));
        investment.withdraw(new BigDecimal("200.00"));
        cheque.deposit(new BigDecimal("2500.00"));
        cheque.withdraw(new BigDecimal("300.00"));

        YearMonth currentMonth = YearMonth.now();
        ((InterestBearing) savings).payMonthlyInterest(currentMonth);
        ((InterestBearing) investment).payMonthlyInterest(currentMonth);

        try {
            AccountDAO accountDAO = new AccountDAO();
            TransactionDAO transactionDAO = new TransactionDAO();

            for (Account account : customer.getAccounts()) {
                accountDAO.save(account);
                transactionDAO.saveAll(account);
            }

            System.out.println("Accounts and transactions saved to SQLite.");
            printSavedData();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save or read account data.", e);
        }
    }

    private static void printSavedData() throws SQLException {
        Customer customer = new CustomerDAO().findById("C001");

        if (customer == null) {
            System.out.println("Customer was not found in the database.");
            return;
        }

        System.out.println("\nCustomer loaded from SQLite: " + customer.getFullName());
        System.out.println("Customer ID: " + customer.getCustomerId());

        AccountDAO accountDAO = new AccountDAO();
        TransactionDAO transactionDAO = new TransactionDAO();

        for (AccountRecord account : accountDAO.findAllForCustomer(customer.getCustomerId())) {
            System.out.println("\n" + account.accountType()
                    + " account: " + account.accountNumber());
            System.out.println("Branch: " + account.branch());
            System.out.println("Balance: BWP " + account.balance());
            System.out.println("Transactions:");

            for (TransactionRecord transaction
                    : transactionDAO.findAllForAccount(account.accountNumber())) {
                System.out.println("  " + transaction.dateTime()
                        + " | " + transaction.type()
                        + " | BWP " + transaction.amount()
                        + " | Balance after: BWP " + transaction.balanceAfter());
            }
        }
    }
}