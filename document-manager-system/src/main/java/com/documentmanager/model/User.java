package com.documentmanager.model;

import java.time.LocalDateTime;

public record User(
        int userId,
        String username,
        String passwordHash,
        String passwordSalt,
        String fullName,
        String email,
        LocalDateTime createdDate,
        boolean active
) {
}
