package database_client;


import utils.exceptions.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DatabaseClient {

    public static final String URL = detectConnectionURL();
    public static final String USERNAME = "atm";
    public static final String PASSWORD = "atm-java";

    public static final String DEPOSIT_QUERY;
    public static final String WITHDRAW_QUERY;
    public static final String ACCOUNT_BY_CARD_QUERY;
    public static final String CUSTOMER_ID_BY_ACCOUNT_QUERY;
    public static final String CUSTOMER_NAME_BY_CUSTOMER_ID;
    public static final String ACCOUNTS_OF_CUSTOMER_QUERY;
    public static final String ACCOUNTS_WITHOUT_BALANCES_QUERY;
    public static final String PIN_CHANGE_QUERY;

    static {
        DEPOSIT_QUERY = "UPDATE accounts SET balance = balance + ? WHERE accountNumber = ? AND currency = ?;";
        WITHDRAW_QUERY = "UPDATE accounts SET balance = balance - ? WHERE accountNumber = ? AND currency = ?;";
        ACCOUNT_BY_CARD_QUERY = "SELECT accountNumber FROM cards WHERE pin = ? AND cardNumber = ?;";
        CUSTOMER_ID_BY_ACCOUNT_QUERY = "SELECT customerID FROM accounts WHERE accountNumber = ?;";
        CUSTOMER_NAME_BY_CUSTOMER_ID = "SELECT name, surname FROM customers WHERE id = ?;";
        ACCOUNTS_OF_CUSTOMER_QUERY = "SELECT accountNumber, balance FROM accounts WHERE customerID = ?;";
        ACCOUNTS_WITHOUT_BALANCES_QUERY = "SELECT accountNumber FROM accounts WHERE customerID = ?;";
        PIN_CHANGE_QUERY = "UPDATE cards SET pin = ? WHERE cardNumber=?;";
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

    private static void closeConnection(Connection connection) throws ConnectionFailedException {
        if (connection == null) {
            System.out.println("No connection to close.");
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            throw new ConnectionFailedException(e.getMessage());
        }
    }

    public static String getCustomerIDByCardID(String cardID, String pin) throws CustomerNotFoundException, ConnectionFailedException {
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
            while (result.next()) {
                customerID = result.getString("customerID");
            }

            if (customerID == null) {
                System.out.println("Customer not found");
                throw new CustomerNotFoundException("Customer not found");
            }

            return customerID;
        } catch (SQLException throwable) {
            System.out.println("Customer not found again");
            throw new CustomerNotFoundException("Customer not found");
        } finally {
            closeConnection(connection);
        }
    }

    public static ArrayList<String> getCustomerAccounts(String customerID) throws CustomerNotFoundException, ConnectionFailedException {
        Connection connection = null;
        try {
            connection = getConnection();
        } catch (ConnectionFailedException throwable) {
            throwable.printStackTrace();
        }
        if (connection == null) {
            throw new ConnectionFailedException("Failed to connect to the DB.");
        }

        try (PreparedStatement statement = connection.prepareStatement(ACCOUNTS_WITHOUT_BALANCES_QUERY)) {
            statement.setString(1, customerID);
            ResultSet result = statement.executeQuery();
            ArrayList<String> accounts = new ArrayList<>();
            String accountNumber;
            while (result.next()) {
                accountNumber = result.getString("accountNumber");
                accounts.add(accountNumber);
            }
            return accounts;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CustomerNotFoundException("Something went wrong");
        } finally {
            closeConnection(connection);
        }
    }

    public static HashMap<String, BigDecimal> getCustomerBalances(String customerID) throws CustomerNotFoundException, ConnectionFailedException {
        Connection connection = null;
        try {
            connection = getConnection();
        } catch (ConnectionFailedException throwable) {
            throwable.printStackTrace();
        }
        if (connection == null) {
            throw new ConnectionFailedException("Failed to connect to the DB.");
        }
        try (PreparedStatement statement = connection.prepareStatement(ACCOUNTS_OF_CUSTOMER_QUERY)) {
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
        } catch (SQLException throwable) {
            throwable.printStackTrace();
            throw new CustomerNotFoundException("Something went wrong");
        } finally {
            closeConnection(connection);
        }
    }

    private static void executeUpdate(String accountNumber, BigDecimal amount, String currency, PreparedStatement statement) throws SQLException, AccountNotFoundException {
        statement.setBigDecimal(1, amount);
        statement.setString(2, accountNumber);
        statement.setString(3, currency);
        int result = statement.executeUpdate();
        if (result == 1) {
            System.out.println("Successful update with " + amount + " on " + accountNumber);
        } else {
            throw new AccountNotFoundException("No account/currency matched");
        }
    }

    public static void withdrawFromAccount(String accountNumber, BigDecimal amount, String currency) throws NoEnoughMoneyException, ConnectionFailedException, AccountNotFoundException {
        Connection connection = null;
        try {
            connection = getConnection();
        } catch (ConnectionFailedException throwable) {
            throwable.printStackTrace();
        }
        if (connection == null) {
            throw new ConnectionFailedException("Failed to connect to the DB.");
        }
        try (PreparedStatement statement = connection.prepareStatement(WITHDRAW_QUERY)) {

            executeUpdate(accountNumber, amount, currency, statement);
            System.out.println("Successfully withdrawn " + amount + " from " + accountNumber);
        } catch (SQLException throwable) {
            throwable.printStackTrace();
            if (throwable.getMessage().contains("Out of range value for column 'balance'")) {
                throw new NoEnoughMoneyException("Insufficient finances.");
            }
            throwable.printStackTrace();
            throw new ConnectionFailedException("Failed to withdraw.");
        } finally {
            closeConnection(connection);
        }
    }

    public static void depositToAccount(String accountNumber, BigDecimal amount, String currency) throws AccountNotFoundException, ConnectionFailedException {
        Connection connection = null;
        try {
            connection = getConnection();
        } catch (ConnectionFailedException throwable) {
            throwable.printStackTrace();
        }
        if (connection == null) {
            throw new ConnectionFailedException("Failed to connect to the DB.");
        }
        try (PreparedStatement statement = connection.prepareStatement(DEPOSIT_QUERY)) {

            executeUpdate(accountNumber, amount, currency, statement);
            System.out.println("Successfully deposited " + amount + " to " + accountNumber);

        } catch (SQLException throwable) {
            throwable.printStackTrace();
            throw new ConnectionFailedException("Failed to withdraw.");
        } finally {
            closeConnection(connection);
        }
    }

    public static void transfer(String fromAccount, String toAccount, BigDecimal amount, String currency) throws ConnectionFailedException, AccountNotFoundException, NoEnoughMoneyException {
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
            } else {
                System.out.println("Result of the updates: deposit: " + depositResult + " withdraw: " + withdrawResult);
                throw new AccountNotFoundException("Failed to transfer money");
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
                e.printStackTrace();
                if (e.getMessage().contains("Out of range value for column 'balance'")) {
                    throw new NoEnoughMoneyException("Insufficient finances.");
                } else {
                    throw new AccountNotFoundException("Failed to transfer money");
                }
            } catch (SQLException throwable) {
                throwable.printStackTrace();
                if (throwable.getMessage().contains("Out of range value for column 'balance'")) {
                    throw new NoEnoughMoneyException("Insufficient finances.");
                } else {
                    throw new AccountNotFoundException("Failed to transfer money");
                }
            }
        } finally {
            closeConnection(connection);
        }
    }

    public static void changePin(String cardNumber, String newPin) throws ConnectionFailedException, PinChangeFailedException {
        Connection connection = null;
        try {
            connection = getConnection();
        } catch (ConnectionFailedException throwable) {
            throwable.printStackTrace();
        }
        if (connection == null) {
            throw new ConnectionFailedException("Failed to connect to the DB.");
        }
        try (PreparedStatement changePin = connection.prepareStatement(PIN_CHANGE_QUERY)) {
            connection.setAutoCommit(false);
            changePin.setString(1, newPin);
            changePin.setString(2, cardNumber);
            int result = changePin.executeUpdate();
            if (result == 1) {
                System.out.println("Pin changed successfully");
                connection.commit();
            } else {
                System.out.println("Result of the update: " + result);
                throw new PinChangeFailedException("Failed to change pin");
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
                e.printStackTrace();
                if (e.getMessage().contains("Data truncation: Data too long for column 'pin'")) {
                    throw new PinChangeFailedException("Pin too long");
                }
                throw new PinChangeFailedException("Failed to change the pin");
            } catch (SQLException throwable) {
                throwable.printStackTrace();
                if (e.getMessage().contains("Data truncation: Data too long for column 'pin'")) {
                    throw new PinChangeFailedException("Pin too long");
                }
                throw new PinChangeFailedException("Failed to change the pin");
            }
        } finally {
            closeConnection(connection);
        }
    }

    public static String getCustomerFullNameByAccountNumber(String accountNumber) throws CustomerNotFoundException, ConnectionFailedException {
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
            PreparedStatement statement = connection.prepareStatement(CUSTOMER_ID_BY_ACCOUNT_QUERY);

            statement.setString(1, accountNumber);
            ResultSet result = statement.executeQuery();

            String customerID = null;
            while (result.next()) {
                customerID = result.getString("customerID");
            }

            if (customerID == null) {
                System.out.println("Customer not found");
                throw new CustomerNotFoundException("Customer not found");
            }
            statement = connection.prepareStatement(CUSTOMER_NAME_BY_CUSTOMER_ID);
            statement.setString(1, customerID);
            result = statement.executeQuery();
            String customerName = null;

            while (result.next()) {
                customerName = result.getString("name");
                customerName += " ";
                customerName += result.getString("surname");
            }

            if (customerName == null) {
                System.out.println("Customer name not found");
                throw new CustomerNotFoundException("Customer name not found");
            }
            return customerName;
        } catch (SQLException throwable) {
            throwable.printStackTrace();
            throw new CustomerNotFoundException("Failed to get customer Name");
        } finally {
            closeConnection(connection);
        }
    }

    public static void main(String[] args) throws CustomerNotFoundException, NoEnoughMoneyException, AccountNotFoundException, ConnectionFailedException, PinChangeFailedException {
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

        String toAccount =   "2222222222222222";
        String fromAccount = "1111111111111111";
        BigDecimal transferAmount = new BigDecimal("4444.34");
        try {
            transfer(fromAccount, toAccount, transferAmount, "AMD");
        } catch (AccountNotFoundException e) {
            System.out.println(e.getMessage());
        }
        transfer(fromAccount, toAccount, transferAmount, "AMD");
        changePin("9999999999999999", "9998");
        changePin("9999999999999999", "9999");
        String customerName = getCustomerFullNameByAccountNumber("2222222222222222");
        System.out.println(customerName);
    }
}
