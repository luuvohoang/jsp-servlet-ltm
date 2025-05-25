package dao;

import model.User;
import util.DBUtil;
import java.sql.*;

public class UserDAO {
    private Connection connection;
    
    public UserDAO() {
        connection = DBUtil.getConnection();
    }
    
    public void register(User user) throws SQLException {
        String query = "INSERT INTO user (username, email, password) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword()); // Trong thực tế nên mã hóa password
            ps.executeUpdate();
        }
    }
    
    public User login(String username, String password) throws SQLException {
        String query = "SELECT * FROM user WHERE username = ? AND password = ?";
        
        // Sử dụng try-with-resources để tự động đóng connection
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, username);
            ps.setString(2, password);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    return user;
                }
            }
        }
        return null;
    }
}