package com.app.service;

import com.app.config.DatabaseConfig;
import com.app.dao.NotificationDAO;
import com.app.model.Notification;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Notification Service.
 * Handles creation, retrieval, and management of user notifications.
 */
public class NotificationService {
    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());
    private NotificationDAO notificationDAO = new NotificationDAO();

    /**
     * Create a new notification for a user (auto-commit version).
     */
    public void createNotification(int userId, String title, String message, String type) throws SQLException {
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setTitle(title);
        notif.setMessage(message);
        notif.setType(type);
        notif.setRead(false);
        notificationDAO.create(notif);
    }

    /**
     * Create a new notification using an existing connection (for transactions).
     */
    public void createNotification(int userId, String title, String message, String type, Connection conn) throws SQLException {
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setTitle(title);
        notif.setMessage(message);
        notif.setType(type);
        notif.setRead(false);
        notificationDAO.create(notif, conn);
    }

    /**
     * Get all unread notifications for a user.
     */
    public List<Notification> getUnreadNotifications(int userId) throws SQLException {
        return notificationDAO.findUnreadByUser(userId);
    }

    /**
     * Get all notifications for a user (with pagination, limit 50).
     */
    public List<Notification> getUserNotifications(int userId) throws SQLException {
        return notificationDAO.findByUser(userId, 50);
    }

    /**
     * Mark a notification as read.
     */
    public boolean markAsRead(int notificationId, int userId) throws SQLException {
        return notificationDAO.markAsRead(notificationId, userId);
    }

    /**
     * Mark all notifications for a user as read.
     */
    public boolean markAllAsRead(int userId) throws SQLException {
        return notificationDAO.markAllAsRead(userId);
    }

    /**
     * Delete a notification (user can delete their own).
     */
    public boolean deleteNotification(int notificationId, int userId) throws SQLException {
        return notificationDAO.delete(notificationId, userId);
    }
}