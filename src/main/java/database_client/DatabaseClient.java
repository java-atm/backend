package database_client;

import utils.exceptions.ConnectionFailedException;
import utils.exceptions.CustomerNotFoundException;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;


public class DatabaseClient{

    public static final String URL = detectConnectionURL();
    public static final String USERNAME = "atm";
    public static final String PASSWORD = "atm-java";

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
            //return DriverManager.getConnection(URL);
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

            PreparedStatement statement = connection.prepareStatement("SELECT accountNumber FROM cards WHERE pin=? AND cardNumber=?;");
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

            statement = connection.prepareStatement("SELECT customerID FROM accounts WHERE accountNumber=?;");
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

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM accounts WHERE customerID=?;");
            statement.setString(1, customerID);
            ResultSet result = statement.executeQuery();

            String accountNumber;
            BigDecimal balance;
            HashMap<String, BigDecimal> hashMap = new HashMap<>();

            while (result.next()) {
                accountNumber = result.getString("accountNUmber");
                balance = result.getBigDecimal("balance");
                hashMap.put(accountNumber, balance);
            }

            return hashMap;
        } catch (ConnectionFailedException | SQLException throwable) {
            throwable.printStackTrace();
            throw new CustomerNotFoundException("Something went wrong");
        }
    }

    public static void main(String[] args) throws CustomerNotFoundException{
        String customerID = getCustomerIDByCardID("9999999999999999", "9999");
        System.out.println("Customer ID is : " + customerID);

        HashMap<String, BigDecimal> result = getCustomerBalances(customerID);

        for (Map.Entry<String, BigDecimal> pair : result.entrySet()) {
            System.out.println("Balance of " + pair.getKey() + " is " + pair.getValue());
        }
    }
}
