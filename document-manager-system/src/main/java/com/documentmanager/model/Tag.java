package com.documentmanager.model;

import java.time.LocalDateTime;

public record Tag(int tagId, String tagName, String colorHex, int userId, LocalDateTime createdDate) {
    @Override
    public String toString() {
        return tagName;
    }
}
