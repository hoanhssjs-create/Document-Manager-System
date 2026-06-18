package com.documentmanager.service;

import com.documentmanager.dao.DocumentTagDAO;
import com.documentmanager.dao.TagDAO;
import com.documentmanager.model.Tag;
import com.documentmanager.util.SessionManager;

import java.sql.SQLException;
import java.util.List;

public class TagService {
    private final TagDAO tagDAO = new TagDAO();
    private final DocumentTagDAO documentTagDAO = new DocumentTagDAO();

    public List<Tag> list() throws SQLException {
        return tagDAO.findByUserId(SessionManager.userId());
    }

    public void create(String name, String colorHex) throws SQLException {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Tag name is required.");
        tagDAO.insert(name.trim(), colorHex == null || colorHex.isBlank() ? "#3366CC" : colorHex, SessionManager.userId());
    }

    public void update(Tag tag, String name, String colorHex) throws SQLException {
        if (tag == null) throw new IllegalArgumentException("Please choose a tag.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Tag name is required.");
        tagDAO.update(tag.tagId(), name.trim(), colorHex == null || colorHex.isBlank() ? "#3366CC" : colorHex);
    }

    public void delete(Tag tag) throws SQLException {
        if (tag == null) throw new IllegalArgumentException("Please choose a tag.");
        tagDAO.delete(tag.tagId());
    }

    public long documentCount(Tag tag) throws SQLException {
        return tag == null ? 0 : tagDAO.countDocuments(tag.tagId());
    }

    public List<Tag> listByDocument(int documentId) throws SQLException {
        return documentTagDAO.findTagsByDocument(documentId);
    }

    public void assignToDocument(int documentId, Tag tag) throws SQLException {
        if (tag == null) throw new IllegalArgumentException("Please choose a tag.");
        documentTagDAO.assign(documentId, tag.tagId());
    }

    public void removeFromDocument(int documentId, Tag tag) throws SQLException {
        if (tag == null) throw new IllegalArgumentException("Please choose a tag.");
        documentTagDAO.remove(documentId, tag.tagId());
    }
}
