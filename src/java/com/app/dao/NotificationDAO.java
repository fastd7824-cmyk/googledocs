package com.app.dao;

import com.app.config.DatabaseConfig;
import com.app.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Notifications.
 */
public class NotificationDAO {

    /**
     * Create a new notification (auto-commit).
     */
    public boolean create(Notification notif) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, title, message, type, is_read) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            return createInternal(stmt, notif);
        }
    }

    /**
     * Create a new notification using an existing connection (for transactions).
     */
    public boolean create(Notification notif, Connection conn) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, title, message, type, is_read) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            return createInternal(stmt, notif);
        }
    }

    private boolean createInternal(PreparedStatement stmt, Notification notif) throws SQLException {
        stmt.setInt(1, notif.getUserId());
        stmt.setString(2, notif.getTitle());
        stmt.setString(3, notif.getMessage());
        stmt.setString(4, notif.getType());
        stmt.setBoolean(5, notif.isRead());
        int affected = stmt.executeUpdate();
        if (affected > 0) {
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    notif.setId(rs.getInt(1));
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Find unread notifications for a user.
     */
    public List<Notification> findUnreadByUser(int userId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND is_read = false ORDER BY created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractNotification(rs));
                }
            }
        }
        return list;
    }

    /**
     * Find recent notifications for a user (with limit).
     */
    public List<Notification> findByUser(int userId, int limit) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractNotification(rs));
                }
            }
        }
        return list;
    }

    /**
     * Mark a single notification as read (only if owned by user).
     */
    public boolean markAsRead(int notificationId, int userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = true WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Mark all notifications for a user as read.
     */
    public boolean markAllAsRead(int userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = true WHERE user_id = ? AND is_read = false";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Delete a notification (owned by user).
     */
    public boolean delete(int notificationId, int userId) throws SQLException {
        String sql = "DELETE FROM notifications WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Notification extractNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUserId(rs.getInt("user_id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setType(rs.getString("type"));
        n.setRead(rs.getBoolean("is_read"));
        n.setCreatedAt(rs.getTimestamp("created_at"));
        return n;
    }
}