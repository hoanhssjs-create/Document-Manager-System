package com.documentmanager.dao;

import com.documentmanager.model.Folder;
import com.documentmanager.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FolderDAO {
    public List<Folder> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM Folders WHERE UserID = ? ORDER BY FolderName";
        List<Folder> folders = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) folders.add(map(rs));
            }
        }
        return folders;
    }

    public void insert(String name, int userId) throws SQLException {
        String sql = "INSERT INTO Folders (FolderName, UserID) VALUES (?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    public void update(int folderId, String name) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE Folders SET FolderName = ? WHERE FolderID = ?")) {
            statement.setString(1, name);
            statement.setInt(2, folderId);
            statement.executeUpdate();
        }
    }

    public void delete(int folderId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM Folders WHERE FolderID = ?")) {
            statement.setInt(1, folderId);
            statement.executeUpdate();
        }
    }

    public long countDocuments(int folderId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM Documents WHERE FolderID = ?")) {
            statement.setInt(1, folderId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    private Folder map(ResultSet rs) throws SQLException {
        Timestamp created = rs.getTimestamp("CreatedDate");
        return new Folder(rs.getInt("FolderID"), rs.getString("FolderName"), rs.getInt("UserID"), created == null ? null : created.toLocalDateTime());
    }
}
