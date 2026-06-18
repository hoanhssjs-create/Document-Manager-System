package com.documentmanager.dao;

import com.documentmanager.model.Tag;
import com.documentmanager.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TagDAO {
    public List<Tag> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM Tags WHERE UserID = ? ORDER BY TagName";
        List<Tag> tags = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) tags.add(map(rs));
            }
        }
        return tags;
    }

    public void insert(String name, String colorHex, int userId) throws SQLException {
        String sql = "INSERT INTO Tags (TagName, ColorHex, UserID) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, colorHex);
            statement.setInt(3, userId);
            statement.executeUpdate();
        }
    }

    public void update(int tagId, String name, String colorHex) throws SQLException {
        String sql = "UPDATE Tags SET TagName = ?, ColorHex = ? WHERE TagID = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, colorHex);
            statement.setInt(3, tagId);
            statement.executeUpdate();
        }
    }

    public void delete(int tagId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM Tags WHERE TagID = ?")) {
            statement.setInt(1, tagId);
            statement.executeUpdate();
        }
    }

    public long countDocuments(int tagId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM DocumentTags WHERE TagID = ?")) {
            statement.setInt(1, tagId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    private Tag map(ResultSet rs) throws SQLException {
        Timestamp created = rs.getTimestamp("CreatedDate");
        return new Tag(rs.getInt("TagID"), rs.getString("TagName"), rs.getString("ColorHex"), rs.getInt("UserID"), created == null ? null : created.toLocalDateTime());
    }
}
