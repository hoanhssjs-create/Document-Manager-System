package com.documentmanager.service;

import com.documentmanager.dao.ActivityLogDAO;
import com.documentmanager.dao.UserDAO;
import com.documentmanager.model.User;
import com.documentmanager.util.PasswordHasher;
import com.documentmanager.util.SessionManager;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    public void register(String username, String password, String fullName, String email) throws SQLException {
        require(username, "Username is required.");
        require(password, "Password is required.");
        require(fullName, "Full name is required.");
        require(email, "Email is required.");
        if (userDAO.findByUsername(username).isPresent()) throw new IllegalArgumentException("Username already exists.");
        if (userDAO.findByEmail(email).isPresent()) throw new IllegalArgumentException("Email already exists.");
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hash(password, salt);
        userDAO.insert(new User(0, username.trim(), hash, salt, fullName.trim(), email.trim(), LocalDateTime.now(), true));
    }

    public User login(String username, String password) throws SQLException {
        User user = userDAO.findByUsername(username.trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));
        if (!PasswordHasher.matches(password, user.passwordSalt(), user.passwordHash())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        SessionManager.login(user);
        activityLogDAO.insert(user.userId(), null, "LOGIN", "User logged in");
        return user;
    }

    public void logout() throws SQLException {
        if (SessionManager.currentUser().isPresent()) {
            activityLogDAO.insert(SessionManager.userId(), null, "LOGOUT", "User logged out");
            SessionManager.logout();
        }
    }

    public void updateProfile(String fullName, String email) throws SQLException {
        require(fullName, "Full name is required.");
        require(email, "Email is required.");
        int userId = SessionManager.userId();
        userDAO.findByEmail(email.trim()).ifPresent(existing -> {
            if (existing.userId() != userId) throw new IllegalArgumentException("Email already exists.");
        });
        userDAO.updateProfile(userId, fullName.trim(), email.trim());
        userDAO.findById(userId).ifPresent(SessionManager::login);
    }

    public void changePassword(String currentPassword, String newPassword) throws SQLException {
        require(currentPassword, "Current password is required.");
        require(newPassword, "New password is required.");
        if (newPassword.length() < 6) throw new IllegalArgumentException("New password must be at least 6 characters.");
        User user = SessionManager.currentUser().orElseThrow(() -> new IllegalStateException("No active user session"));
        if (!PasswordHasher.matches(currentPassword, user.passwordSalt(), user.passwordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hash(newPassword, salt);
        userDAO.updatePassword(user.userId(), hash, salt);
        userDAO.findById(user.userId()).ifPresent(SessionManager::login);
    }

    private void require(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
    }
}
