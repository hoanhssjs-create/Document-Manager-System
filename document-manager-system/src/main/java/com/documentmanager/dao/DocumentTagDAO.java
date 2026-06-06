package com.documentmanager.dao;

import com.documentmanager.model.Tag;
import com.documentmanager.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentTagDAO {
    public void assign(int documentId, int tagId) throws SQLException {
        String sql = """
                IF NOT EXISTS (SELECT 1 FROM DocumentTags WHERE DocumentID = ? AND TagID = ?)
                INSERT INTO DocumentTags (DocumentID, TagID) VALUES (?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, documentId);
            statement.setInt(2, tagId);
            statement.setInt(3, documentId);
            statement.setInt(4, tagId);
            statement.executeUpdate();
        }
    }

    public void remove(int documentId, int tagId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM DocumentTags WHERE DocumentID = ? AND TagID = ?")) {
            statement.setInt(1, documentId);
            statement.setInt(2, tagId);
            statement.executeUpdate();
        }
    }

    public List<Tag> findTagsByDocument(int documentId) throws SQLException {
        String sql = """
                SELECT t.*
                FROM Tags t
                JOIN DocumentTags dt ON t.TagID = dt.TagID
                WHERE dt.DocumentID = ?
                ORDER BY t.TagName
                """;
        List<Tag> tags = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, documentId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Timestamp created = rs.getTimestamp("CreatedDate");
                    tags.add(new Tag(
                            rs.getInt("TagID"),
                            rs.getString("TagName"),
                            rs.getString("ColorHex"),
                            rs.getInt("UserID"),
                            created == null ? null : created.toLocalDateTime()
                    ));
                }
            }
        }
        return tags;
    }
}
