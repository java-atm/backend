package database_client;

import utils.exceptions.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class DatabaseClient{

    public static final String URL = detectConnectionURL();
    public static final String USERNAME = "atm";
    public static final String PASSWORD = "atm-java";

    public static final String DEPOSIT_QUERY;
    public static final String WITHDRAW_QUERY;
    public static final String ACCOUNT_BY_CARD_QUERY;
    public static final String CUSTOMER_ID_BY_ACCOUNT_QUERY;
    public static final String ACCOUNTS_OF_CUSTOMER_QUERY;

    static {
        DEPOSIT_QUERY = "UPDATE accounts SET balance = balance + ? WHERE accountNumber = ? AND currency = ?;";
        WITHDRAW_QUERY = "UPDATE accounts SET balance = balance - ? WHERE accountNumber = ? AND currency = ?;";
        ACCOUNT_BY_CARD_QUERY = "SELECT accountNumber FROM cards WHERE pin=? AND cardNumber=?;";
        CUSTOMER_ID_BY_ACCOUNT_QUERY = "SELECT customerID FROM accounts WHERE accountNumber=?;";
        ACCOUNTS_OF_CUSTOMER_QUERY = "SELECT * FROM accounts WHERE customerID=?;";
    }

    private static String detectConnectionURL() {
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (IOException e) {
            return "jdbc:mysql://localhost:3306/bank_db";
        }
        if (hostname.equals("ip-172-31-47-225")) {
            return "jdbc:mysql://database-1.cbvxvxkpbwei.us-east-2.rds.amazonaws.com:3306/bank_db";
        }
        return "jdbc:mysql://localhost:3306/bank_db";
    }
    private static Connection getConnection() throws ConnectionFailedException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.err.println("Connecting process...");
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException | ClassNotFoundException throwable) {
            throwable.printStackTrace();
            System.err.println("Connection failed");
            throw new ConnectionFailedException("Failed to connect to the DB");
        }
    }

    public static String getCustomerIDByCardID(String cardID, String pin) throws CustomerNotFoundException {
        try {
            // @TODO This is probably not needed. Remove it.
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = getConnection();

            PreparedStatement statement = connection.prepareStatement(ACCOUNT_BY_CARD_QUERY);
            statement.setString(1, pin);
            statement.setString(2, cardID);
            ResultSet result = statement.executeQuery();

            String acc = null;
            while (result.next()) {
                acc = result.getString("accountNUmber");
                System.out.println("Account number: " + acc);
            }

            if (acc == null) {
                System.out.println("Invalid Credentials");
                throw new CustomerNotFoundException("Invalid credentials.");
            }

            statement = connection.prepareStatement(CUSTOMER_ID_BY_ACCOUNT_QUERY);
            statement.setString(1, acc);
            result = statement.executeQuery();

            String customerID = null;
            while(result.next()) {
                customerID = result.getString("customerID");
            }

            if (customerID == null) {
                System.out.println("Customer not found");
                throw new CustomerNotFoundException("Customer not found");
            }

            return customerID;
        } catch (ConnectionFailedException | SQLException | ClassNotFoundException throwable) {
            System.out.println("Customer not found again");
            throw new CustomerNotFoundException("Customer not found");
        }
    }

    public static HashMap<String, BigDecimal> getCustomerBalances(String customerID) throws CustomerNotFoundException {
        try {
            Connection connection = getConnection();

            PreparedStatement statement = connection.prepareStatement(ACCOUNTS_OF_CUSTOMER_QUERY);
            statement.setString(1, customerID);
            ResultSet result = statement.executeQuery();

            String accountNumber;
            BigDecimal balance;
            HashMap<String, BigDecimal> hashMap = new HashMap<>();

            while (result.next()) {
                accountNumber = result.getString("accountNumber");
                balance = result.getBigDecimal("balance");
                hashMap.put(accountNumber, balance);
            }

            return hashMap;
        } catch (ConnectionFailedException | SQLException throwable) {
            throwable.printStackTrace();
            throw new CustomerNotFoundException("Something went wrong");
        }
    }

    private static void executeUpdate(String accountNumber, BigDecimal amount, String currency, PreparedStatement statement) throws SQLException, AccountNotFoundException {
        statement.setBigDecimal(1, amount);
        statement.setString(2, accountNumber);
        statement.setString(3, currency);
        int result = statement.executeUpdate();
        if (result == 1) {
            System.out.println("Successful update with " + amount + " on " + accountNumber);
        }
        else {
            throw new AccountNotFoundException("No account/currency matched");
        }
    }

    public static void withdrawFromAccount(String accountNumber, BigDecimal amount, String currency) throws NoEnoughMoneyException, ConnectionFailedException, AccountNotFoundException{
        Connection connection = null;
        try {
            connection = getConnection();
        } catch (ConnectionFailedException throwable) {
            throwable.printStackTrace();
        }
        if (connection == null) {
            throw new ConnectionFailedException("Failed to connect to the DB.");
        }
        try {
            PreparedStatement statement = connection.prepareStatement(WITHDRAW_QUERY);
            executeUpdate(accountNumber, amount, currency, statement);
            System.out.println("Successfully withdrawn " + amount + " from " + accountNumber);
        } catch (SQLException throwable) {
            throwable.printStackTrace();
            if (throwable.getMessage().contains("Out of range value for column 'balance'")) {
                throw new NoEnoughMoneyException("Insufficient finances.");
            }
            throwable.printStackTrace();
            throw new ConnectionFailedException("Failed to withdraw.");
        }
    }

    public static void depositToAccount(String accountNumber, BigDecimal amount, String currency) throws AccountNotFoundException, ConnectionFailedException{
        Connection connection = null;
        try {
            connection = getConnection();
        } catch (ConnectionFailedException throwable) {
            throwable.printStackTrace();
        }
        if (connection == null) {
            throw new ConnectionFailedException("Failed to connect to the DB.");
        }
        try {
            PreparedStatement statement = connection.prepareStatement(DEPOSIT_QUERY);
            executeUpdate(accountNumber, amount, currency, statement);
            System.out.println("Successfully deposited " + amount + " to " + accountNumber);

        } catch (SQLException throwable) {
            throwable.printStackTrace();
            throw new ConnectionFailedException("Failed to withdraw.");
        }
    }

    public static void transfer(String fromAccount, String toAccount, BigDecimal amount, String currency) throws ConnectionFailedException, AccountNotFoundException, NoEnoughMoneyException{
        Connection connection = null;
        try {
            connection = getConnection();
        } catch (ConnectionFailedException throwable) {
            throwable.printStackTrace();
        }
        if (connection == null) {
            throw new ConnectionFailedException("Failed to connect to the DB.");
        }
        try (PreparedStatement withdraw = connection.prepareStatement(WITHDRAW_QUERY);
             PreparedStatement deposit = connection.prepareStatement(DEPOSIT_QUERY)) {

            connection.setAutoCommit(false);
            withdraw.setBigDecimal(1, amount);
            withdraw.setString(2, fromAccount);
            withdraw.setString(3, currency);

            deposit.setBigDecimal(1, amount);
            deposit.setString(2, toAccount);
            deposit.setString(3, currency);

            int withdrawResult = withdraw.executeUpdate();
            int depositResult = deposit.executeUpdate();
            if (withdrawResult == 1 && depositResult == 1) {
                System.out.println("Successfully transferred");
                connection.commit();
            }
            else {
                System.out.println("Result of the updates: deposit: " + depositResult + " withdraw: " + withdrawResult);
                throw new AccountNotFoundException("Failed to transfer money");
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
                e.printStackTrace();
                if (e.getMessage().contains("Out of range value for column 'balance'")) {
                    throw new NoEnoughMoneyException("Insufficient finances.");
                }
            } catch (SQLException throwable) {
                throwable.printStackTrace();
                if (throwable.getMessage().contains("Out of range value for column 'balance'")) {
                    throw new NoEnoughMoneyException("Insufficient finances.");
                }
            }
        }
    }

    public static void main(String[] args) throws CustomerNotFoundException, NoEnoughMoneyException, AccountNotFoundException, ConnectionFailedException, TransferFailedException {
        String customerID = getCustomerIDByCardID("9999999999999999", "9999");
        System.out.println("Customer ID is : " + customerID);

        HashMap<String, BigDecimal> result = getCustomerBalances(customerID);

        for (Map.Entry<String, BigDecimal> pair : result.entrySet()) {
            System.out.println("Balance of " + pair.getKey() + " is " + pair.getValue());
        }
        String accountNumber = "1111111111111111";
        BigDecimal amount = new BigDecimal("3467");
        withdrawFromAccount(accountNumber, amount, "AMD");
        amount = new BigDecimal("4444.34");
        depositToAccount(accountNumber, amount, "AMD");

        String toAccount = "2222222222222222";
        String fromAccount = "111111111111111";
        BigDecimal transferAmount = new BigDecimal("4444.34");
        transfer(fromAccount, toAccount, transferAmount, "AMD");
    }
}
