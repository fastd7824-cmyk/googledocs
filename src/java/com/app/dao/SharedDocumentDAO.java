package com.app.dao;

import com.app.config.DatabaseConfig;
import com.app.model.SharedDocument;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Document Sharing.
 */
public class SharedDocumentDAO {

    /**
     * Save a new share entry.
     */
    public boolean save(SharedDocument share, Connection conn) throws SQLException {
        String sql = "INSERT INTO shared_documents (document_id, shared_with_user_id, permission, shared_by) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, share.getDocumentId());
            stmt.setInt(2, share.getSharedWithUserId());
            stmt.setString(3, share.getPermission());
            stmt.setInt(4, share.getSharedBy());
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Save a new share entry using auto-commit.
     */
    public boolean save(int documentId, int sharedWithUserId, String permission, int sharedBy) throws SQLException {
        String sql = "INSERT INTO shared_documents (document_id, shared_with_user_id, permission, shared_by) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, documentId);
            stmt.setInt(2, sharedWithUserId);
            stmt.setString(3, permission);
            stmt.setInt(4, sharedBy);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Delete a share entry.
     */
    public boolean delete(int documentId, int sharedWithUserId) throws SQLException {
        String sql = "DELETE FROM shared_documents WHERE document_id = ? AND shared_with_user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, documentId);
            stmt.setInt(2, sharedWithUserId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Check if a user has a permission level on a document.
     *
     * @param documentId document ID
     * @param userId user ID
     * @param requiredPerm "read" or "write" - if "read", any share works; if "write", only write permission counts
     * @return true if permission is granted
     */
    public boolean hasPermission(int documentId, int userId, String requiredPerm) throws SQLException {
        String sql = "SELECT permission FROM shared_documents WHERE document_id = ? AND shared_with_user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, documentId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String perm = rs.getString("permission");
                    if ("write".equals(requiredPerm)) {
                        return "write".equals(perm);
                    }
                    return true; // read access is satisfied by any share
                }
            }
        }
        return false;
    }

    /**
     * Get all users with whom a document is shared.
     */
    public List<SharedDocument> findByDocument(int documentId) throws SQLException {
        List<SharedDocument> shares = new ArrayList<>();
        String sql = "SELECT * FROM shared_documents WHERE document_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, documentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    shares.add(extractShare(rs));
                }
            }
        }
        return shares;
    }

    /**
     * Get all documents shared with a user (returns share records, not documents).
     */
    public List<SharedDocument> findBySharedWithUser(int userId) throws SQLException {
        List<SharedDocument> shares = new ArrayList<>();
        String sql = "SELECT * FROM shared_documents WHERE shared_with_user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    shares.add(extractShare(rs));
                }
            }
        }
        return shares;
    }

    private SharedDocument extractShare(ResultSet rs) throws SQLException {
        SharedDocument share = new SharedDocument();
        share.setId(rs.getInt("id"));
        share.setDocumentId(rs.getInt("document_id"));
        share.setSharedWithUserId(rs.getInt("shared_with_user_id"));
        share.setPermission(rs.getString("permission"));
        share.setSharedBy(rs.getInt("shared_by"));
        share.setSharedAt(rs.getTimestamp("shared_at"));
        return share;
    }
}