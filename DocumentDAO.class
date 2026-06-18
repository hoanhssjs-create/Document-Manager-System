package com.documentmanager.service;

import com.documentmanager.dao.FolderDAO;
import com.documentmanager.model.Folder;
import com.documentmanager.util.SessionManager;

import java.sql.SQLException;
import java.util.List;

public class FolderService {
    private final FolderDAO folderDAO = new FolderDAO();

    public List<Folder> list() throws SQLException {
        return folderDAO.findByUserId(SessionManager.userId());
    }

    public void create(String name) throws SQLException {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Folder name is required.");
        folderDAO.insert(name.trim(), SessionManager.userId());
    }

    public void update(Folder folder, String name) throws SQLException {
        if (folder == null) throw new IllegalArgumentException("Please choose a folder.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Folder name is required.");
        folderDAO.update(folder.folderId(), name.trim());
    }

    public void delete(Folder folder) throws SQLException {
        if (folder == null) throw new IllegalArgumentException("Please choose a folder.");
        folderDAO.delete(folder.folderId());
    }

    public long documentCount(Folder folder) throws SQLException {
        return folder == null ? 0 : folderDAO.countDocuments(folder.folderId());
    }
}
