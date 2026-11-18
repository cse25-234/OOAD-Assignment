import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.YearMonth;

public class Main {

    public static void main(String[] args) {
        Database.initialize();

        try {
            Customer customer1 = new Customer(
                    "C001", "Thato Molefe", "Gaborone",
                    true, "Example Company", "Gaborone");

            Customer customer2 = new Customer(
                    "C002", "Naledi Kgosidintsi", "Francistown",
                    true, "North Star Ltd", "Francistown");

            Customer customer3 = new Customer(
                    "C003", "Kabelo Motsumi", "Maun",
                    false, null, null);

            CustomerDAO customerDAO = new CustomerDAO();
            customerDAO.save(customer1);
            customerDAO.save(customer2);
            customerDAO.save(customer3);

            AccountDAO accountDAO = new AccountDAO();
            TransactionDAO transactionDAO = new TransactionDAO();

            SavingsAccount savings1 =
                    new SavingsAccount("A1001", "Main Branch", customer1);
            savings1.deposit(new BigDecimal("500.00"));
            savings1.payMonthlyInterest(YearMonth.now());
            saveIfMissing(savings1, accountDAO, transactionDAO);

            InvestmentAccount investment1 =
                    new InvestmentAccount(
                            "A1002", "Main Branch", customer1,
                            new BigDecimal("1000.00"));
            investment1.withdraw(new BigDecimal("200.00"));
            investment1.payMonthlyInterest(YearMonth.now());
            saveIfMissing(investment1, accountDAO, transactionDAO);

            ChequeAccount cheque1 =
                    new ChequeAccount("A1003", "Main Branch", customer1);
            cheque1.deposit(new BigDecimal("2500.00"));
            cheque1.withdraw(new BigDecimal("300.00"));
            saveIfMissing(cheque1, accountDAO, transactionDAO);

            SavingsAccount savings2 =
                    new SavingsAccount("A2001", "Main Branch", customer2);
            savings2.deposit(new BigDecimal("1500.00"));
            saveIfMissing(savings2, accountDAO, transactionDAO);

            SavingsAccount savings3 =
                    new SavingsAccount("A3001", "Main Branch", customer3);
            savings3.deposit(new BigDecimal("750.00"));
            saveIfMissing(savings3, accountDAO, transactionDAO);

            System.out.println("Demo customers and accounts are ready.");
            printSavedData();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Could not save or read the demo data.", e);
        }
    }

    private static void saveIfMissing(
            Account account,
            AccountDAO accountDAO,
            TransactionDAO transactionDAO) throws SQLException {

        if (!accountDAO.exists(account.getAccountNumber())) {
            accountDAO.save(account);
            transactionDAO.saveAll(account);
        }
    }

    private static void printSavedData() throws SQLException {
        CustomerDAO customerDAO = new CustomerDAO();
        AccountDAO accountDAO = new AccountDAO();
        TransactionDAO transactionDAO = new TransactionDAO();

        for (String customerId :
                new String[]{"C001", "C002", "C003"}) {
            Customer customer = customerDAO.findById(customerId);

            if (customer == null) {
                System.out.println("Customer " + customerId + " not found.");
                continue;
            }

            System.out.println("\nCustomer: " + customer.getFullName());
            System.out.println("Customer ID: " + customer.getCustomerId());

            for (AccountRecord account :
                    accountDAO.findAllForCustomer(customerId)) {
                System.out.println("\n" + account.accountType()
                        + " account: " + account.accountNumber());
                System.out.println("Balance: BWP " + account.balance());

                for (TransactionRecord transaction :
                        transactionDAO.findAllForAccount(
                                account.accountNumber())) {
                    System.out.println("  " + transaction.dateTime()
                            + " | " + transaction.type()
                            + " | BWP " + transaction.amount()
                            + " | Balance after: BWP "
                            + transaction.balanceAfter());
                }
            }
        }
    }
}