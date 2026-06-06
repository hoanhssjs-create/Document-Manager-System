package com.documentmanager.dao;

import com.documentmanager.model.DashboardStats;
import com.documentmanager.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardDAO {
    public DashboardStats getStats(int userId) throws SQLException {
        return new DashboardStats(
                scalar("SELECT COUNT(*) FROM Documents WHERE UserID = ?", userId),
                scalar("SELECT COUNT(*) FROM Folders WHERE UserID = ?", userId),
                scalar("SELECT COUNT(*) FROM Tags WHERE UserID = ?", userId),
                scalar("SELECT COALESCE(SUM(FileSize), 0) FROM Documents WHERE UserID = ?", userId)
        );
    }

    private long scalar(String sql, int userId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }
}
