package com.documentmanager.model;

public record SearchCriteria(int userId, String keyword, String fileType, Integer folderId, Integer tagId) {
}
