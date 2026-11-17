public class ChequeAccount extends Account {
    private final String employerName;
    private final String employerAddress;

    public ChequeAccount(String accountNumber, String branch, Customer owner) {
        super(accountNumber, branch, owner);

        if (owner == null || !owner.isEmployed()) {
            throw new IllegalArgumentException(
                    "A Cheque Account can only be opened by an employed customer.");
        }

        if (owner.getEmployerName() == null || owner.getEmployerName().isBlank()
                || owner.getEmployerAddress() == null || owner.getEmployerAddress().isBlank()) {
            throw new IllegalArgumentException(
                    "Employer name and address are required for a Cheque Account.");
        }

        this.employerName = owner.getEmployerName();
        this.employerAddress = owner.getEmployerAddress();
    }

    @Override
    public String getAccountType() {
        return "Cheque";
    }

    @Override
    protected boolean allowsWithdrawals() {
        return true;
    }

    public String getEmployerName() {
        return employerName;
    }

    public String getEmployerAddress() {
        return employerAddress;
    }
}