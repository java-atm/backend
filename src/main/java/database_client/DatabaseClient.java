package database_client;

import utils.exceptions.ConnectionFailedException;
import utils.exceptions.CustomerNotFoundException;
import  java.sql.*;


public class DatabaseClient{
    public static final String URL = "jdbc:mysql://database-1.cbvxvxkpbwei.us-east-2.rds.amazonaws.com:3306/bank_db";

//    public static final String URL = "jdbc:mysql://localhost:3306/bank_db";
    public static final String USERNAME = "atm";
    public static final String PASSWORD = "atm-java";


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
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.err.println("Start connecting...");
            Connection connection = getConnection();
            System.err.println("Connecting may be OK");
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

//    public static void main(String[] args) throws CustomerNotFoundException{
//        String a = getCustomerIDByCardID("5523112233445566", "0101");
//        System.out.println("Customer ID is : " + a);
//    }
//    public static void main(String[] args) {
//        try {
//            Connection connection = getConnection();
//            Statement statement = connection.createStatement();
//            ResultSet cards = statement.executeQuery("SELECT * FROM cards;");
//            System.out.println("YUHU");
//
//            while (cards.next()) {
//                System.out.println(cards.getBigDecimal("cardNumber"));
//                System.out.println(cards.getBigDecimal("accountNumber"));
//                Object s = cards.getObject("type");
//                System.out.println(s);
//            }
//
//        } catch (SQLException | ConnectionFailedException throwable) {
//            throwable.printStackTrace();
//        }
//    }
}
