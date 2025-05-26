package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database utility class for managing connections
 */
public class DBUtil {
    
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/dulieu";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "";
    
    public static Connection connection = getConnection();
    
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    return connection;
                }
            } catch (SQLException e) {
                System.out.println("Error checking connection status: " + e.getMessage());
            }
        }
        
        try {
            connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
            System.out.println("Database connected successfully");
        } catch (SQLException e) {
            System.out.println("Database Connection Creation Failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        return connection;
    }
}