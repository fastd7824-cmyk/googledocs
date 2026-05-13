package com.app.service;

import com.app.config.DatabaseConfig;
import com.app.dao.UserDAO;
import com.app.model.User;
import com.app.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Authentication Service.
 * Handles user login, registration, and password management.
 */
public class AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    private UserDAO userDAO = new UserDAO();

    // Simple validation methods (replace with ValidationUtil if needed)
    private boolean isValidUsername(String username) {
        return username != null && username.trim().length() >= 3 && username.matches("^[a-zA-Z0-9_]+$");
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Authenticate a user by username/email and password.
     */
    public User authenticate(String login, String password) throws SQLException {
        User user = userDAO.findByUsername(login);
        if (user == null) {
            user = userDAO.findByEmail(login);
        }

        if (user != null && user.isActive() && PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            userDAO.updateLastLogin(user.getId());
            LOGGER.info("User authenticated: " + user.getUsername());
            return user;
        }

        LOGGER.warning("Failed authentication attempt for: " + login);
        return null;
    }

    /**
     * Register a new user.
     */
    public boolean registerUser(String username, String email, String password, String fullName, int roleId) throws SQLException {
        // Validate input
        if (!isValidUsername(username) || !isValidEmail(email) || !isValidPassword(password)) {
            return false;
        }

        // Check if username or email already exists
        if (userDAO.findByUsername(username) != null || userDAO.findByEmail(email) != null) {
            return false;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        newUser.setRoleId(roleId);
        newUser.setActive(true);

        return userDAO.createUser(newUser, password);
    }

    /**
     * Change password for a user.
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) throws SQLException {
        User user = userDAO.findById(userId);
        if (user == null || !PasswordUtil.checkPassword(oldPassword, user.getPasswordHash())) {
            return false;
        }

        if (!isValidPassword(newPassword)) {
            return false;
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newHash);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }
}