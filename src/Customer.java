import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Customer {
    private final String customerId;
    private final String fullName;
    private final String address;
    private final boolean employed;
    private final String employerName;
    private final String employerAddress;
    private final List<Account> accounts = new ArrayList<>();

    public Customer(
            String customerId,
            String fullName,
            String address,
            boolean employed,
            String employerName,
            String employerAddress) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.address = address;
        this.employed = employed;
        this.employerName = employerName;
        this.employerAddress = employerAddress;
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAddress() {
        return address;
    }

    public boolean isEmployed() {
        return employed;
    }

    public String getEmployerName() {
        return employerName;
    }

    public String getEmployerAddress() {
        return employerAddress;
    }

    public List<Account> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }
}