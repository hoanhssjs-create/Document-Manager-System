package com.documentmanager.dao;

import com.documentmanager.model.Document;
import com.documentmanager.model.SearchCriteria;
import com.documentmanager.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DocumentDAO {
    public int insert(Document document) throws SQLException {
        String sql = """
                INSERT INTO Documents (Title, Description, FilePath, FileType, FileSize, FolderID, UserID)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, document.title());
            statement.setString(2, document.description());
            statement.setString(3, document.filePath());
            statement.setString(4, document.fileType());
            statement.setLong(5, document.fileSize());
            if (document.folderId() == null) statement.setNull(6, Types.INTEGER);
            else statement.setInt(6, document.folderId());
            statement.setInt(7, document.userId());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public List<Document> findByUserId(int userId) throws SQLException {
        return search(new SearchCriteria(userId, "", null, null, null));
    }

    public List<Document> search(SearchCriteria criteria) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT d.* FROM Documents d ");
        if (criteria.tagId() != null) sql.append("JOIN DocumentTags dt ON d.DocumentID = dt.DocumentID ");
        sql.append("WHERE d.UserID = ? ");
        List<Object> params = new ArrayList<>();
        params.add(criteria.userId());
        if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
            sql.append("AND (d.Title LIKE ? OR d.Description LIKE ?) ");
            params.add("%" + criteria.keyword().trim() + "%");
            params.add("%" + criteria.keyword().trim() + "%");
        }
        if (criteria.fileType() != null && !criteria.fileType().isBlank()) {
            sql.append("AND d.FileType = ? ");
            params.add(criteria.fileType());
        }
        if (criteria.folderId() != null) {
            sql.append("AND d.FolderID = ? ");
            params.add(criteria.folderId());
        }
        if (criteria.tagId() != null) {
            sql.append("AND dt.TagID = ? ");
            params.add(criteria.tagId());
        }
        sql.append("ORDER BY d.CreatedDate DESC");
        List<Document> documents = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) documents.add(map(rs));
            }
        }
        return documents;
    }

    public Optional<Document> findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM Documents WHERE DocumentID = ?")) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public void update(Document document) throws SQLException {
        String sql = """
                UPDATE Documents
                SET Title = ?, Description = ?, FolderID = ?, UpdatedDate = SYSUTCDATETIME()
                WHERE DocumentID = ? AND UserID = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, document.title());
            statement.setString(2, document.description());
            if (document.folderId() == null) statement.setNull(3, Types.INTEGER);
            else statement.setInt(3, document.folderId());
            statement.setInt(4, document.documentId());
            statement.setInt(5, document.userId());
            statement.executeUpdate();
        }
    }

    public void delete(int documentId, int userId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM Documents WHERE DocumentID = ? AND UserID = ?")) {
            statement.setInt(1, documentId);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    public long countByUser(int userId) throws SQLException {
        return scalar("SELECT COUNT(*) FROM Documents WHERE UserID = ?", userId);
    }

    public long sumSizeByUser(int userId) throws SQLException {
        return scalar("SELECT COALESCE(SUM(FileSize), 0) FROM Documents WHERE UserID = ?", userId);
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

    private void bind(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            if (value instanceof Integer integer) statement.setInt(i + 1, integer);
            else statement.setString(i + 1, value.toString());
        }
    }

    private Document map(ResultSet rs) throws SQLException {
        Timestamp created = rs.getTimestamp("CreatedDate");
        Timestamp updated = rs.getTimestamp("UpdatedDate");
        int folderId = rs.getInt("FolderID");
        Integer nullableFolderId = rs.wasNull() ? null : folderId;
        return new Document(
                rs.getInt("DocumentID"),
                rs.getString("Title"),
                rs.getString("Description"),
                rs.getString("FilePath"),
                rs.getString("FileType"),
                rs.getLong("FileSize"),
                created == null ? null : created.toLocalDateTime(),
                updated == null ? null : updated.toLocalDateTime(),
                nullableFolderId,
                rs.getInt("UserID")
        );
    }
}
