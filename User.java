package com.documentmanager.dao;

import com.documentmanager.model.ActivityLog;
import com.documentmanager.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDAO {
    public void insert(int userId, Integer documentId, String action, String description) throws SQLException {
        String sql = "INSERT INTO ActivityLogs (UserID, DocumentID, Action, Description) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            if (documentId == null) statement.setNull(2, Types.INTEGER);
            else statement.setInt(2, documentId);
            statement.setString(3, action);
            statement.setString(4, description);
            statement.executeUpdate();
        }
    }

    public List<ActivityLog> findRecentByUser(int userId, int limit) throws SQLException {
        String sql = "SELECT TOP (?) * FROM ActivityLogs WHERE UserID = ? ORDER BY LogTime DESC";
        List<ActivityLog> logs = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            statement.setInt(2, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) logs.add(map(rs));
            }
        }
        return logs;
    }

    private ActivityLog map(ResultSet rs) throws SQLException {
        Timestamp time = rs.getTimestamp("LogTime");
        int documentId = rs.getInt("DocumentID");
        return new ActivityLog(
                rs.getLong("LogID"),
                rs.getInt("UserID"),
                rs.wasNull() ? null : documentId,
                rs.getString("Action"),
                rs.getString("Description"),
                time == null ? null : time.toLocalDateTime()
        );
    }
}
