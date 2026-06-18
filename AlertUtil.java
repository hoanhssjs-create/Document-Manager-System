package com.documentmanager.dao;

import com.documentmanager.model.User;
import com.documentmanager.util.DatabaseConnection;

import java.sql.*;
import java.util.Optional;

public class UserDAO {
    public int insert(User user) throws SQLException {
        String sql = """
                INSERT INTO Users (Username, PasswordHash, PasswordSalt, FullName, Email)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.username());
            statement.setString(2, user.passwordHash());
            statement.setString(3, user.passwordSalt());
            statement.setString(4, user.fullName());
            statement.setString(5, user.email());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM Users WHERE Username = ? AND IsActive = 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM Users WHERE Email = ? AND IsActive = 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<User> findById(int userId) throws SQLException {
        String sql = "SELECT * FROM Users WHERE UserID = ? AND IsActive = 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public void updateProfile(int userId, String fullName, String email) throws SQLException {
        String sql = "UPDATE Users SET FullName = ?, Email = ? WHERE UserID = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fullName);
            statement.setString(2, email);
            statement.setInt(3, userId);
            statement.executeUpdate();
        }
    }

    public void updatePassword(int userId, String passwordHash, String passwordSalt) throws SQLException {
        String sql = "UPDATE Users SET PasswordHash = ?, PasswordSalt = ? WHERE UserID = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setString(2, passwordSalt);
            statement.setInt(3, userId);
            statement.executeUpdate();
        }
    }

    private User map(ResultSet rs) throws SQLException {
        Timestamp created = rs.getTimestamp("CreatedDate");
        return new User(
                rs.getInt("UserID"),
                rs.getString("Username"),
                rs.getString("PasswordHash"),
                rs.getString("PasswordSalt"),
                rs.getString("FullName"),
                rs.getString("Email"),
                created == null ? null : created.toLocalDateTime(),
                rs.getBoolean("IsActive")
        );
    }
}
