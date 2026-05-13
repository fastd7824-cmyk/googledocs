package com.app.service;

import com.app.config.DatabaseConfig;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for logging user activities asynchronously.
 * This prevents logging from slowing down the user experience.
 */
public class ActivityLogService {
    private static final Logger LOGGER = Logger.getLogger(ActivityLogService.class.getName());
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "activity-logger");
        t.setDaemon(true); // Allows JVM to exit even if this thread is running
        return t;
    });

    /**
     * Log a user action asynchronously.
     *
     * @param userId  the ID of the user performing the action (can be null for system actions)
     * @param action  the action name (e.g., "LOGIN", "CREATE_DOCUMENT")
     * @param details additional details about the action
     * @param request the HttpServletRequest to extract IP and User-Agent (can be null)
     */
    public void logActivity(Integer userId, String action, String details, HttpServletRequest request) {
        String ipAddress = null;
        String userAgent = null;

        if (request != null) {
            // Get real IP address considering proxies
            ipAddress = getClientIpAddress(request);
            userAgent = request.getHeader("User-Agent");
        }

        final Integer finalUserId = userId;
        final String finalAction = action;
        final String finalDetails = details;
        final String finalIpAddress = ipAddress;
        final String finalUserAgent = userAgent;

        EXECUTOR.submit(() -> {
            String sql = "INSERT INTO activity_logs (user_id, action, details, ip_address, user_agent) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                if (finalUserId != null) {
                    stmt.setInt(1, finalUserId);
                } else {
                    stmt.setNull(1, java.sql.Types.INTEGER);
                }
                stmt.setString(2, finalAction);
                stmt.setString(3, finalDetails);
                stmt.setString(4, finalIpAddress);
                stmt.setString(5, finalUserAgent);

                stmt.executeUpdate();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Failed to log activity: " + finalAction + " for user " + finalUserId, e);
            }
        });
    }

    /**
     * Synchronous version for critical logs where you need immediate guarantee (rarely needed).
     */
    public void logActivitySync(Integer userId, String action, String details, HttpServletRequest request) {
        String ipAddress = (request != null) ? getClientIpAddress(request) : null;
        String userAgent = (request != null) ? request.getHeader("User-Agent") : null;

        String sql = "INSERT INTO activity_logs (user_id, action, details, ip_address, user_agent) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (userId != null) {
                stmt.setInt(1, userId);
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setString(2, action);
            stmt.setString(3, details);
            stmt.setString(4, ipAddress);
            stmt.setString(5, userAgent);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to log activity synchronously", e);
        }
    }

    /**
     * Extract the real client IP address, handling proxies and load balancers.
     *
     * @param request HttpServletRequest
     * @return IP address string
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // In case multiple IPs (X-Forwarded-For can contain a list), take the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * Shutdown the executor service (call from context listener on app stop).
     */
    public static void shutdown() {
        EXECUTOR.shutdown();
    }
}