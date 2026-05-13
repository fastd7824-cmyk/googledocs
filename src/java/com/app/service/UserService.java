package com.app.service;

import com.app.config.DatabaseConfig;
import com.app.dao.UserDAO;
import com.app.model.User;
import com.app.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * User Service.
 * Handles user profile management, role changes, and user listing.
 */
public class UserService {
    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());
    private UserDAO userDAO = new UserDAO();

    // Simple email validation (replace with regex if needed)
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Get user by ID.
     */
    public User getUserById(int id) throws SQLException {
        return userDAO.findById(id);
    }

    /**
     * Get user by username.
     */
    public User getUserByUsername(String username) throws SQLException {
        return userDAO.findByUsername(username);
    }

    /**
     * Get all users (for admin).
     */
    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAllUsers();
    }

    /**
     * Update user profile (full name, email).
     */
    public boolean updateProfile(int userId, String fullName, String email) throws SQLException {
        User user = userDAO.findById(userId);
        if (user == null) return false;

        if (!isValidEmail(email)) {
            return false;
        }

        // Check if email already taken by another user
        User existing = userDAO.findByEmail(email);
        if (existing != null && existing.getId() != userId) {
            return false;
        }

        String sql = "UPDATE users SET full_name = ?, email = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setInt(3, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Change user role (admin only).
     */
    public boolean changeUserRole(int targetUserId, int newRoleId, int adminUserId) throws SQLException {
        // Prevent admin from changing their own role to non-admin (lockout protection)
        if (targetUserId == adminUserId) {
            return false;
        }

        String sql = "UPDATE users SET role_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newRoleId);
            stmt.setInt(2, targetUserId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Activate or deactivate a user account (admin only).
     */
    public boolean setUserActiveStatus(int targetUserId, boolean active, int adminUserId) throws SQLException {
        if (targetUserId == adminUserId) {
            // Prevent self deactivation
            return false;
        }

        String sql = "UPDATE users SET is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, targetUserId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Delete a user account (admin only).
     */
    public boolean deleteUser(int targetUserId, int adminUserId) throws SQLException {
        if (targetUserId == adminUserId) {
            return false;
        }

        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, targetUserId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Get count of active users (for dashboard stats).
     */
    public int getActiveUsersCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE is_active = true";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}