package com.documentmanager.model;

import java.time.LocalDateTime;

public record Document(
        int documentId,
        String title,
        String description,
        String filePath,
        String fileType,
        long fileSize,
        LocalDateTime createdDate,
        LocalDateTime updatedDate,
        Integer folderId,
        int userId
) {
}
