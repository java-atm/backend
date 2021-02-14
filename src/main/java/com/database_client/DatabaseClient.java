package com.database_client;


import com.utils.exceptions.db_exceptions.*;
import com.utils.exceptions.security_exceptions.PasswordHashingFailedException;
import com.utils.security.PasswordHashingClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DatabaseClient {
    private static final Logger LOGGER = LogManager.getLogger(DatabaseClient.class);

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
    public static final String VERIFY_ATM_ID;

    static {
        DEPOSIT_QUERY = "UPDATE accounts SET balance = balance + ? WHERE accountNumber = ? AND currency = ?;";
        WITHDRAW_QUERY = "UPDATE accounts SET balance = balance - ? WHERE accountNumber = ? AND currency = ?;";
        ACCOUNT_BY_CARD_QUERY = "SELECT accountNumber, pinSalt, pinHash FROM cards WHERE cardNumber = ?;";
        CUSTOMER_ID_BY_ACCOUNT_QUERY = "SELECT customerID FROM accounts WHERE accountNumber = ?;";
        CUSTOMER_NAME_BY_CUSTOMER_ID = "SELECT name, surname FROM customers WHERE id = ?;";
        ACCOUNTS_OF_CUSTOMER_QUERY = "SELECT accountNumber, balance FROM accounts WHERE customerID = ?;";
        ACCOUNTS_WITHOUT_BALANCES_QUERY = "SELECT accountNumber FROM accounts WHERE customerID = ?;";
        PIN_CHANGE_QUERY = "UPDATE cards SET pinHash = ?, pin = ? WHERE cardNumber=?;";
        VERIFY_ATM_ID = "SELECT COUNT(id) FROM atms WHERE id = ?;";
    }

    private static String detectConnectionURL() {
        String hostname = "";
        String connectionURL;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (IOException e) {
            LOGGER.error("Failed to get Hostname: '{}'}", e.getMessage(), e);
        }
        if (hostname.equals("ip-172-31-47-225")) {
            connectionURL = "jdbc:mysql://database-1.cbvxvxkpbwei.us-east-2.rds.amazonaws.com:3306/bank_db";
        } else {
            connectionURL = "jdbc:mysql://localhost:3306/bank_db";
        }
        LOGGER.info("USING '{}' as connection URL", connectionURL);
        return connectionURL;
    }

    private static Connection getConnection() throws ConnectionFailedException {
        Connection connection;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            LOGGER.info("Start connection.");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error("Failed to connect to DB: '{}'", e.getMessage(), e);
            throw new ConnectionFailedException("Failed to connect to the DB");
        }
        if (connection == null) {
            LOGGER.error("Failed to connect to DB, connection is null");
            throw new ConnectionFailedException("Failed to connect to the DB, connection is null");
        }
        return connection;
    }

    private static void closeConnection(Connection connection) throws ConnectionFailedException {
        if (connection == null) {
            LOGGER.warn("Connection is null, nothing to close.");
            return;
        }
        try {
            connection.close();
            LOGGER.info("Connection closed.");
        } catch (SQLException e) {
            LOGGER.error("Failed to close DB connection: '{}'", e.getMessage(), e);
            throw new ConnectionFailedException(e.getMessage());
        }
    }

    public static boolean verifyATMID(String atm_id) throws ConnectionFailedException {
        LOGGER.info("Starting ATM verification: '{}'", atm_id);
        Connection connection = getConnection();
        LOGGER.info("Connection established, creating VERIFY_ATM_ID statement for '{}'", atm_id);

        try (PreparedStatement statement = connection.prepareStatement(VERIFY_ATM_ID)) {
            statement.setString(1, atm_id);
            ResultSet result = statement.executeQuery();
            LOGGER.info("Statement executed, checking results");
            int number_of_rows = -1;
            while( result.next()) {
                number_of_rows = result.getInt(1);
            }
            LOGGER.info("'{}' rows matched '{}'",number_of_rows, atm_id);
            return number_of_rows == 1;

        } catch (SQLException e) {
            LOGGER.error("Failed to verify ATM ID '{}': '{}'", atm_id, e.getMessage(), e);
            return false;
        } finally {
            closeConnection(connection);
        }
    }
    public static Long getCustomerIDByCardID(Long cardNumber, String pin) throws CustomerNotFoundException, ConnectionFailedException {
        LOGGER.info("Starting to get customer ID by cardNumber '{}'.", cardNumber);
        Connection connection = getConnection();
        LOGGER.info("Connection established, creating ACCOUNT_BY_CARD_QUERY statement for '{}'", cardNumber);

        try {
            PreparedStatement statement = connection.prepareStatement(ACCOUNT_BY_CARD_QUERY);
            statement.setLong(1, cardNumber);
            ResultSet result = statement.executeQuery();
            LOGGER.info("Query ACCOUNT_BY_CARD_QUERY executed, processing results.");
            String acc = null;
            String salt = null;
            String hash = null;
            while (result.next()) {
                acc = result.getString("accountNUmber");
                salt = result.getString("pinSalt");
                hash = result.getString("pinHash");
            }
            if (acc == null || salt == null || hash == null) {
                LOGGER.error("No account found for CARD: '{}', acc: '{}, salt: '{}', hash: '{}'", cardNumber, acc, salt, hash);
                throw new CustomerNotFoundException("Invalid credentials.");
            }
            LOGGER.info("Found '{}' using CARD: '{}'", acc, cardNumber);
            String pinHash = PasswordHashingClient.hashThePassword(salt, pin);
            if (! pinHash.equals(hash)){
                throw new CustomerNotFoundException("Incorrect pin");
            }
            LOGGER.info("Creating CUSTOMER_ID_BY_ACCOUNT_QUERY statement for '{}'", acc);
            statement = connection.prepareStatement(CUSTOMER_ID_BY_ACCOUNT_QUERY);
            statement.setString(1, acc);
            result = statement.executeQuery();

            LOGGER.info("Query CUSTOMER_ID_BY_ACCOUNT_QUERY executed, processing results.");
            Long customerID = null;
            while (result.next()) {
                customerID = result.getLong("customerID");
            }

            if (customerID == null) {
                LOGGER.error("No customer found by '{}'", acc);
                throw new CustomerNotFoundException("Customer not found");
            }
            LOGGER.info("Customer ID: '{}', account number: '{}', card: '{}'", customerID, acc, cardNumber);
            return customerID;
        } catch (SQLException | PasswordHashingFailedException e) {
            LOGGER.error("Failed to get customer ID for '{}': '{}'", cardNumber, e.getMessage(), e);
            throw new CustomerNotFoundException("Customer not found");
        } finally {
            closeConnection(connection);
        }
    }

    public static ArrayList<String> getCustomerAccounts(Long customerID) throws CustomerNotFoundException, ConnectionFailedException {
        LOGGER.info("Starting to get customer's accounts by customer ID '{}'", customerID);
        Connection connection = getConnection();
        LOGGER.info("Connection established, creating ACCOUNTS_WITHOUT_BALANCES_QUERY statement for '{}'", customerID);

        try (PreparedStatement statement = connection.prepareStatement(ACCOUNTS_WITHOUT_BALANCES_QUERY)) {
            statement.setLong(1, customerID);
            ResultSet result = statement.executeQuery();
            LOGGER.info("Query ACCOUNTS_WITHOUT_BALANCES_QUERY executed, processing results.");
            ArrayList<String> accounts = new ArrayList<>();
            String accountNumber;
            while (result.next()) {
                accountNumber = result.getString("accountNumber");
                accounts.add(accountNumber);
            }
            LOGGER.info("'{}' accounts found for '{}'", accounts.size(), customerID);
            return accounts;
        } catch (SQLException e) {
            LOGGER.error("Failed to get accounts for '{}' customer: '{}'", customerID, e.getMessage(), e);
            throw new CustomerNotFoundException("Failed to get customer accounts");
        } finally {
            closeConnection(connection);
        }
    }

    public static HashMap<String, BigDecimal> getCustomerBalances(Long customerID) throws CustomerNotFoundException, ConnectionFailedException {
        LOGGER.info("Starting to get customer's accounts with balances by customer ID '{}'", customerID);
        Connection connection = getConnection();
        LOGGER.info("Connection established, creating ACCOUNTS_OF_CUSTOMER_QUERY statement for '{}'", customerID);

        try (PreparedStatement statement = connection.prepareStatement(ACCOUNTS_OF_CUSTOMER_QUERY)) {
            statement.setLong(1, customerID);
            ResultSet result = statement.executeQuery();
            LOGGER.info("Query ACCOUNTS_OF_CUSTOMER_QUERY executed, processing results.");

            String accountNumber;
            BigDecimal balance;
            HashMap<String, BigDecimal> hashMap = new HashMap<>();

            while (result.next()) {
                accountNumber = result.getString("accountNumber");
                balance = result.getBigDecimal("balance");
                hashMap.put(accountNumber, balance);
            }
            LOGGER.info("Found '{}' accounts for '{}'", hashMap.size(), customerID);
            return hashMap;
        } catch (SQLException e) {
            LOGGER.error("Failed to get accounts for '{}' customer: '{}'", customerID, e.getMessage(), e);
            throw new CustomerNotFoundException("Something went wrong");
        } finally {
            closeConnection(connection);
        }
    }

    private static void executeUpdate(Long accountNumber, BigDecimal amount, String currency, PreparedStatement statement) throws SQLException, AccountNotFoundException {
        LOGGER.info("Executing update for '{}', amount: '{}', currency: '{}'", accountNumber, amount, currency);
        statement.setBigDecimal(1, amount);
        statement.setLong(2, accountNumber);
        statement.setString(3, currency);
        int result = statement.executeUpdate();
        if (result == 1) {
            LOGGER.info("Successful update for '{}' by '{}'", accountNumber, amount);
        } else {
            LOGGER.error(" Number of rows updated for '{}' by '{}' is '{}'", accountNumber, amount, result);
            throw new AccountNotFoundException("No account/currency matched");
        }
    }

    public static void withdrawFromAccount(Long accountNumber, BigDecimal amount, String currency) throws NoEnoughMoneyException, ConnectionFailedException, AccountNotFoundException {
        LOGGER.info("Starting to withdraw '{}''{}' from '{}'", amount, currency, accountNumber);
        Connection connection = getConnection();
        LOGGER.info("Connection established, creating WITHDRAW_QUERY statement for '{}'", accountNumber);
        try (PreparedStatement statement = connection.prepareStatement(WITHDRAW_QUERY)) {
            executeUpdate(accountNumber, amount, currency, statement);
            LOGGER.info("Successful withdrawal for '{}' by '{}'", accountNumber, amount);
        } catch (SQLException e) {
            if (e.getMessage().contains("Out of range value for column 'balance'")) {
                LOGGER.error("Insufficient finances on  '{}': '{}'", accountNumber, e.getMessage(), e);
                throw new NoEnoughMoneyException("Insufficient finances.");
            }
            LOGGER.error("Failed to withdraw '{}''{}' from '{}': '{}'", amount, currency, accountNumber, e.getMessage(), e);
            throw new ConnectionFailedException("Failed to withdraw.");
        } finally {
            closeConnection(connection);
        }
    }

    public static void depositToAccount(Long accountNumber, BigDecimal amount, String currency) throws AccountNotFoundException, ConnectionFailedException {
        LOGGER.info("Starting to deposit '{}''{}' to '{}'", amount, currency, accountNumber);
        Connection connection = getConnection();
        LOGGER.info("Connection established, creating DEPOSIT_QUERY statement for '{}'", accountNumber);

        try (PreparedStatement statement = connection.prepareStatement(DEPOSIT_QUERY)) {
            executeUpdate(accountNumber, amount, currency, statement);
            LOGGER.info("Successful deposit to '{}' by '{}'", accountNumber, amount);

        } catch (SQLException e) {
            LOGGER.error("Failed to deposit '{}''{}' to '{}': '{}'", amount, currency, accountNumber, e.getMessage(), e);
            throw new ConnectionFailedException("Failed to withdraw.");
        } finally {
            closeConnection(connection);
        }
    }

    public static void transfer(Long fromAccount, Long toAccount, BigDecimal amount, String currency) throws ConnectionFailedException, AccountNotFoundException, NoEnoughMoneyException {
        LOGGER.info("Starting to transfer '{}''{}' from '{}' to '{}'", amount, currency, fromAccount, toAccount);
        Connection connection = getConnection();
        LOGGER.info("Connection established, creating WITHDRAW AND DEPOSIT statements for '{}' and '{}' by '{}''{}'", fromAccount, toAccount, amount, currency);

        try (PreparedStatement withdraw = connection.prepareStatement(WITHDRAW_QUERY);
             PreparedStatement deposit = connection.prepareStatement(DEPOSIT_QUERY)) {
            connection.setAutoCommit(false);
            LOGGER.info("Statements created, autocommit disabled.");
            withdraw.setBigDecimal(1, amount);
            withdraw.setLong(2, fromAccount);
            withdraw.setString(3, currency);

            deposit.setBigDecimal(1, amount);
            deposit.setLong(2, toAccount);
            deposit.setString(3, currency);
            LOGGER.info("Statement parameters all set.");
            int withdrawResult = withdraw.executeUpdate();
            LOGGER.info("Withdraw update executed, number of row affected: '{}'", withdrawResult);
            int depositResult = deposit.executeUpdate();
            LOGGER.info("Deposit update executed, number of row affected: '{}'", depositResult);
            if (withdrawResult == 1 && depositResult == 1) {
                LOGGER.info("All good, committing to the DB");
                connection.commit();
                LOGGER.info("Commit done.");
            } else {
                System.out.println("Result of the updates: deposit: " + depositResult + " withdraw: " + withdrawResult);
                LOGGER.error("Withdraw update: '{}', deposit update: '{}', from: '{}', to: '{}'", withdrawResult, depositResult, fromAccount, toAccount);
                throw new AccountNotFoundException("Failed to transfer money");
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
                LOGGER.info("Connection rolled back");
                if (e.getMessage().contains("Out of range value for column 'balance'")) {
                    LOGGER.error("Insufficient finances('{}''{}') for '{}' or '{}': '{}'", amount, currency, fromAccount, toAccount, e.getMessage(), e);
                    throw new NoEnoughMoneyException("Insufficient finances.");
                } else {
                    LOGGER.error("Failed to transfer '{}' from '{}' to '{}': '{}'", amount, fromAccount, toAccount, e.getMessage(), e);
                    throw new AccountNotFoundException("Failed to transfer money");
                }
            } catch (SQLException e1) {
                if (e1.getMessage().contains("Out of range value for column 'balance'")) {
                    LOGGER.error("Insufficient finances('{}') for '{}' or '{}': '{}'", amount, fromAccount, toAccount, e1.getMessage(), e1);
                    throw new NoEnoughMoneyException("Insufficient finances.");
                } else {
                    LOGGER.error("Failed to transfer '{}' from '{}' to '{}': '{}'", amount, fromAccount, toAccount, e1.getMessage(), e1);
                    throw new AccountNotFoundException("Failed to transfer money");
                }
            }
        } finally {
            closeConnection(connection);
        }
    }

    public static void changePin(Long cardNumber, String newPin) throws ConnectionFailedException, PinChangeFailedException, NewPinTooLongException{
        LOGGER.info("Starting to change PIN of '{}'", cardNumber);
        Connection connection = getConnection();
        LOGGER.info("Connection established, creating PIN_CHANGE_QUERY statement for '{}'", cardNumber);

        try {
            PreparedStatement getSaltStatement = connection.prepareStatement(ACCOUNT_BY_CARD_QUERY);
            getSaltStatement.setLong(1, cardNumber);
            ResultSet resultSalt = getSaltStatement.executeQuery();
            LOGGER.info("Query ACCOUNT_BY_CARD_QUERY executed, processing results.");
            String salt = null;
            while (resultSalt.next()) {
                salt = resultSalt.getString("pinSalt");
            }
            if (salt == null) {
                LOGGER.error("No account found for CARD: '{}', salt: '{}'", cardNumber, salt);
                throw new PinChangeFailedException("Invalid credentials.");
            }
            LOGGER.info("Hashing the password using CARD: '{}'", cardNumber);
            String pinHash = PasswordHashingClient.hashThePassword(salt, newPin);
            PreparedStatement changePin = connection.prepareStatement(PIN_CHANGE_QUERY);
            connection.setAutoCommit(false);
            LOGGER.info("Statements created, autocommit disabled.");
            changePin.setString(1, pinHash);
            changePin.setString(2, newPin);
            changePin.setLong(3, cardNumber);
            int result = changePin.executeUpdate();
            LOGGER.info("Change Pin update executed, number of row affected: '{}'", result);
            if (result == 1) {
                LOGGER.info("Pin change successful");
                connection.commit();
            } else {
                System.out.println("Result of the update: " + result);
                LOGGER.error("Row affect by pin change for '{}' is '{}'", cardNumber, result);
                throw new PinChangeFailedException("Failed to change pin");
            }
        } catch (SQLException | PasswordHashingFailedException e) {
            try {
                connection.rollback();
                LOGGER.info("Rolled back.");
                if (e.getMessage().contains("Data truncation: Data too long for column 'pin'")) {
                    LOGGER.error("New pin of '{}' is too long: '{}'", cardNumber, e.getMessage(), e);
                    throw new NewPinTooLongException("Pin too long");
                }
                LOGGER.error("Failed to change pin for '{}': '{}'", cardNumber, e.getMessage(), e);
                throw new PinChangeFailedException("Failed to change the pin");
            } catch (SQLException e1) {
                if (e1.getMessage().contains("Data truncation: Data too long for column 'pin'")) {
                    LOGGER.error("New pin of '{}' is too long: '{}'", cardNumber, e1.getMessage(), e1);
                    throw new NewPinTooLongException("Pin too long");
                }
                LOGGER.error("Failed to change pin for '{}': '{}'", cardNumber, e1.getMessage(), e1);
                throw new PinChangeFailedException("Failed to change the pin");
            }
        } finally {
            closeConnection(connection);
        }
    }

    public static String getCustomerFullNameByAccountNumber(Long accountNumber) throws CustomerNotFoundException, ConnectionFailedException {
        LOGGER.info("Starting to get customer full name by account number '{}'", accountNumber);
        Connection connection = getConnection();
        LOGGER.info("Connection established, creating CUSTOMER_ID_BY_ACCOUNT_QUERY statement for '{}'", accountNumber);

        try {
            PreparedStatement statement = connection.prepareStatement(CUSTOMER_ID_BY_ACCOUNT_QUERY);

            statement.setLong(1, accountNumber);
            ResultSet result = statement.executeQuery();
            LOGGER.info("customerID Statement executed.");
            String customerID = null;
            while (result.next()) {
                customerID = result.getString("customerID");
            }

            if (customerID == null) {
                LOGGER.error("Customer ID not found for '{}'", accountNumber);
                throw new CustomerNotFoundException("Account not found");
            }
            LOGGER.info("Customer ID for '{}' is '{}', creating CUSTOMER_NAME_BY_CUSTOMER_ID", customerID, accountNumber);
            statement = connection.prepareStatement(CUSTOMER_NAME_BY_CUSTOMER_ID);
            statement.setString(1, customerID);
            result = statement.executeQuery();
            LOGGER.info("CustomerName Statement executed.");
            String customerName = null;

            while (result.next()) {
                customerName = result.getString("name");
                customerName += " ";
                customerName += result.getString("surname");
            }

            if (customerName == null) {
                LOGGER.error("Customer name is null  for '{}'", customerID);
                throw new CustomerNotFoundException("Customer name not found");
            }
            LOGGER.info("Customer name for '{}' is '{}', creating CUSTOMER_NAME_BY_CUSTOMER_ID", customerName, customerID);
            return customerName;
        } catch (SQLException e) {
            LOGGER.error("Failed to get customer name for '{}': '{}'", accountNumber, e.getMessage(), e);
            throw new CustomerNotFoundException("Failed to get customer Name");
        } finally {
            closeConnection(connection);
        }
    }

    public static void main(String[] args) throws CustomerNotFoundException, NoEnoughMoneyException, AccountNotFoundException, ConnectionFailedException, NewPinTooLongException, PinChangeFailedException {
        Long customerID = getCustomerIDByCardID(Long.valueOf("9999999999999999"), "9999");
        System.out.println("Customer ID is : " + customerID);

        HashMap<String, BigDecimal> result = getCustomerBalances(customerID);

        for (Map.Entry<String, BigDecimal> pair : result.entrySet()) {
            System.out.println("Balance of " + pair.getKey() + " is " + pair.getValue());
        }
        Long accountNumber = Long.valueOf("1111111111111111");
        BigDecimal amount = new BigDecimal("3467");
        withdrawFromAccount(accountNumber, amount, "AMD");
        amount = new BigDecimal("4444.34");
        depositToAccount(accountNumber, amount, "AMD");

        Long toAccount =  Long.valueOf("2222222222222222");
        Long fromAccount = Long.valueOf("1111111111111111");
        BigDecimal transferAmount = new BigDecimal("4444.34");
        try {
            transfer(fromAccount, toAccount, transferAmount, "AMD");
        } catch (AccountNotFoundException e) {
            System.out.println(e.getMessage());
        }
        transfer(fromAccount, toAccount, transferAmount, "AMD");
        changePin(Long.valueOf("9999999999999999"), "1234");
        changePin(Long.valueOf("9999999999999999"), "9999");
        String customerName = getCustomerFullNameByAccountNumber(Long.valueOf("2222222222222222"));
        System.out.println(customerName);

        System.out.println(verifyATMID("HAT_INECOBANK_ATM_021"));
    }
}
