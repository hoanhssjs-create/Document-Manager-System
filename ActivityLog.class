package com.documentmanager.util;

import com.documentmanager.model.User;

import java.util.Optional;

public final class SessionManager {
    private static User currentUser;

    private SessionManager() {
    }

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static Optional<User> currentUser() {
        return Optional.ofNullable(currentUser);
    }

    public static int userId() {
        return currentUser().map(User::userId).orElseThrow(() -> new IllegalStateException("No active user session"));
    }
}
