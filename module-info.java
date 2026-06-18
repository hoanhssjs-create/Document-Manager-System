package com.documentmanager.model;

import java.time.LocalDateTime;

public record ActivityLog(
        long logId,
        int userId,
        Integer documentId,
        String action,
        String description,
        LocalDateTime logTime
) {
}
