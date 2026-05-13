package com.app.dao;

import com.app.config.DatabaseConfig;
import com.app.model.ActivityLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Activity Logs.
 */
public class ActivityLogDAO {

    /**
     * Insert a new activity log.
     */
    public boolean create(ActivityLog log) throws SQLException {
        String sql = "INSERT INTO activity_logs (user_id, action, details, ip_address, user_agent) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getUserId() != null) {
                stmt.setInt(1, log.getUserId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, log.getAction());
            stmt.setString(3, log.getDetails());
            stmt.setString(4, log.getIpAddress());
            stmt.setString(5, log.getUserAgent());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        log.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Find recent activity logs (limited).
     */
    public List<ActivityLog> findRecent(int limit) throws SQLException {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT l.*, u.username FROM activity_logs l LEFT JOIN users u ON l.user_id = u.id ORDER BY l.timestamp DESC LIMIT ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(extractLog(rs));
                }
            }
        }
        return logs;
    }

    /**
     * Find logs by username (exact match).
     */
    public List<ActivityLog> findByUsername(String username) throws SQLException {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT l.*, u.username FROM activity_logs l JOIN users u ON l.user_id = u.id WHERE u.username = ? ORDER BY l.timestamp DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(extractLog(rs));
                }
            }
        }
        return logs;
    }

    /**
     * Find logs by action type.
     */
    public List<ActivityLog> findByAction(String action) throws SQLException {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT l.*, u.username FROM activity_logs l LEFT JOIN users u ON l.user_id = u.id WHERE l.action = ? ORDER BY l.timestamp DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, action);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(extractLog(rs));
                }
            }
        }
        return logs;
    }

    /**
     * Find logs by user ID.
     */
    public List<ActivityLog> findByUserId(int userId) throws SQLException {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT l.*, u.username FROM activity_logs l LEFT JOIN users u ON l.user_id = u.id WHERE l.user_id = ? ORDER BY l.timestamp DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(extractLog(rs));
                }
            }
        }
        return logs;
    }

    private ActivityLog extractLog(ResultSet rs) throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setId(rs.getInt("id"));
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            log.setUserId(userId);
        }
        log.setAction(rs.getString("action"));
        log.setDetails(rs.getString("details"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setUserAgent(rs.getString("user_agent"));
        log.setTimestamp(rs.getTimestamp("timestamp"));
        log.setUsername(rs.getString("username"));
        return log;
    }
}