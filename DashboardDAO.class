package com.documentmanager.service;

import com.documentmanager.dao.ActivityLogDAO;
import com.documentmanager.dao.DocumentDAO;
import com.documentmanager.model.Document;
import com.documentmanager.model.SearchCriteria;
import com.documentmanager.util.FileStorageUtil;
import com.documentmanager.util.SessionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class DocumentService {
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    public int upload(Path source, String title, String description, Integer folderId) throws SQLException, IOException {
        if (source == null) throw new IllegalArgumentException("Please choose a file.");
        if (!FileStorageUtil.isSupported(source)) throw new IllegalArgumentException("Only PDF, DOCX, and TXT files are supported.");
        if (title == null || title.isBlank()) title = stripExtension(source.getFileName().toString());
        int userId = SessionManager.userId();
        Path copied = FileStorageUtil.copyToUserStorage(source, userId);
        try {
            Document document = new Document(0, title.trim(), description, copied.toString(), FileStorageUtil.extension(source), Files.size(copied), LocalDateTime.now(), null, folderId, userId);
            int id = documentDAO.insert(document);
            activityLogDAO.insert(userId, id, "UPLOAD", "Upload " + title.trim());
            return id;
        } catch (SQLException ex) {
            Files.deleteIfExists(copied);
            throw ex;
        }
    }

    public List<Document> listCurrentUserDocuments(String keyword) throws SQLException {
        return documentDAO.search(new SearchCriteria(SessionManager.userId(), keyword, null, null, null));
    }

    public List<Document> search(String keyword, String fileType, Integer folderId, Integer tagId) throws SQLException {
        return documentDAO.search(new SearchCriteria(SessionManager.userId(), keyword, fileType, folderId, tagId));
    }

    public void update(Document document, String title, String description, Integer folderId) throws SQLException {
        if (document == null) throw new IllegalArgumentException("Please choose a document.");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Document title is required.");
        Document updated = new Document(
                document.documentId(),
                title.trim(),
                description,
                document.filePath(),
                document.fileType(),
                document.fileSize(),
                document.createdDate(),
                document.updatedDate(),
                folderId,
                SessionManager.userId()
        );
        documentDAO.update(updated);
        activityLogDAO.insert(SessionManager.userId(), document.documentId(), "EDIT", "Edit " + title.trim());
    }

    public void delete(Document document) throws SQLException, IOException {
        documentDAO.delete(document.documentId(), SessionManager.userId());
        Files.deleteIfExists(Path.of(document.filePath()));
        activityLogDAO.insert(SessionManager.userId(), null, "DELETE", "Delete " + document.title());
    }

    public void open(Document document) throws SQLException, IOException {
        FileStorageUtil.open(Path.of(document.filePath()));
        activityLogDAO.insert(SessionManager.userId(), document.documentId(), "VIEW", "Open " + document.title());
    }

    private String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(0, dot);
    }
}
