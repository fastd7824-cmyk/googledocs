package com.app.service;

import com.app.config.DatabaseConfig;
import com.app.dao.DocumentDAO;
import com.app.dao.SharedDocumentDAO;
import com.app.model.Document;
import com.app.model.SharedDocument;
import com.app.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Document Service.
 * Handles document CRUD operations, sharing, permissions, and search.
 */
public class DocumentService {
    private static final Logger LOGGER = Logger.getLogger(DocumentService.class.getName());
    private DocumentDAO documentDAO = new DocumentDAO();
    private SharedDocumentDAO sharedDocumentDAO = new SharedDocumentDAO();
    private NotificationService notificationService = new NotificationService();
    private ActivityLogService activityLogService = new ActivityLogService();

    /**
     * Create a new document.
     */
    public Document createDocument(String title, String content, int createdBy, String status) throws SQLException {
        Document doc = new Document();
        doc.setTitle(title);
        doc.setContent(content);
        doc.setCreatedBy(createdBy);
        doc.setStatus(status != null ? status : "draft");
        return documentDAO.create(doc);
    }

    /**
     * Update an existing document if user has write permission.
     */
    public boolean updateDocument(int docId, String title, String content, String status, User currentUser) throws SQLException {
        Document doc = documentDAO.findById(docId);
        if (doc == null) return false;

        if (!hasWritePermission(doc, currentUser.getId())) {
            return false;
        }

        doc.setTitle(title);
        doc.setContent(content);
        doc.setStatus(status);
        return documentDAO.update(doc);
    }

    /**
     * Delete a document (only owner can delete).
     */
    public boolean deleteDocument(int docId, int userId) throws SQLException {
        Document doc = documentDAO.findById(docId);
        if (doc == null || doc.getCreatedBy() != userId) {
            return false;
        }
        return documentDAO.delete(docId);
    }

    /**
     * Get document by ID with permission check (read permission needed).
     */
    public Document getDocumentForUser(int docId, int userId) throws SQLException {
        Document doc = documentDAO.findById(docId);
        if (doc == null) return null;
        if (hasReadPermission(doc, userId)) {
            return doc;
        }
        return null;
    }

    /**
     * Share a document with another user.
     */
    public boolean shareDocument(int docId, int ownerId, int targetUserId, String permission) throws SQLException {
        // Verify owner
        Document doc = documentDAO.findById(docId);
        if (doc == null || doc.getCreatedBy() != ownerId) {
            return false;
        }

        // Use transaction to ensure both share record and notification succeed
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                SharedDocument share = new SharedDocument();
                share.setDocumentId(docId);
                share.setSharedWithUserId(targetUserId);
                share.setSharedBy(ownerId);
                share.setPermission(permission);

                boolean shared = sharedDocumentDAO.save(share, conn);
                if (shared) {
                    // Create notification
                    notificationService.createNotification(
                        targetUserId,
                        "Document Shared",
                        doc.getTitle() + " has been shared with you with " + permission + " permission.",
                        "share",
                        conn
                    );
                }
                conn.commit();
                return shared;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Remove sharing access.
     */
    public boolean unshareDocument(int docId, int ownerId, int targetUserId) throws SQLException {
        Document doc = documentDAO.findById(docId);
        if (doc == null || doc.getCreatedBy() != ownerId) {
            return false;
        }
        return sharedDocumentDAO.delete(docId, targetUserId);
    }

    /**
     * Get all documents owned by a user.
     */
    public List<Document> getUserDocuments(int userId) throws SQLException {
        return documentDAO.findByUser(userId);
    }

    /**
     * Get documents shared with a user.
     */
    public List<Document> getSharedDocuments(int userId) throws SQLException {
        return documentDAO.findSharedWithUser(userId);
    }

    /**
     * Search documents by title or content for a user (includes owned + shared readable).
     */
    public List<Document> searchDocuments(String keyword, int userId) throws SQLException {
        return documentDAO.searchByTitleOrContent(keyword, userId);
    }

    // ---------- Permission helpers ----------
    private boolean hasReadPermission(Document doc, int userId) throws SQLException {
        return doc.getCreatedBy() == userId || sharedDocumentDAO.hasPermission(doc.getId(), userId, "read");
    }

    private boolean hasWritePermission(Document doc, int userId) throws SQLException {
        return doc.getCreatedBy() == userId || sharedDocumentDAO.hasPermission(doc.getId(), userId, "write");
    }
}
