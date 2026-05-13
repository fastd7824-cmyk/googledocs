package com.app.dao;

import com.app.config.DatabaseConfig;
import com.app.model.Document;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Documents.
 */
public class DocumentDAO {

    /**
     * Create a new document.
     */
    public Document create(Document doc) throws SQLException {
        String sql = "INSERT INTO documents (title, content, created_by, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, doc.getTitle());
            stmt.setString(2, doc.getContent());
            stmt.setInt(3, doc.getCreatedBy());
            stmt.setString(4, doc.getStatus());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        doc.setId(rs.getInt(1));
                    }
                }
            }
        }
        return doc;
    }

    /**
     * Find document by ID, including owner username.
     */
    public Document findById(int id) throws SQLException {
        String sql = "SELECT d.*, u.username as owner_name FROM documents d LEFT JOIN users u ON d.created_by = u.id WHERE d.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractDocument(rs);
                }
            }
        }
        return null;
    }

    /**
     * Get all documents owned by a user.
     */
    public List<Document> findByUser(int userId) throws SQLException {
        List<Document> docs = new ArrayList<>();
        String sql = "SELECT d.*, u.username as owner_name FROM documents d JOIN users u ON d.created_by = u.id WHERE d.created_by = ? ORDER BY d.updated_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    docs.add(extractDocument(rs));
                }
            }
        }
        return docs;
    }

    /**
     * Get documents shared with a user (includes permission level).
     */
    public List<Document> findSharedWithUser(int userId) throws SQLException {
        List<Document> docs = new ArrayList<>();
        String sql = "SELECT d.*, u.username as owner_name, sd.permission FROM documents d " +
                     "JOIN shared_documents sd ON d.id = sd.document_id " +
                     "JOIN users u ON d.created_by = u.id " +
                     "WHERE sd.shared_with_user_id = ? ORDER BY sd.shared_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Document doc = extractDocument(rs);
                    doc.setSharedPermission(rs.getString("permission"));
                    docs.add(doc);
                }
            }
        }
        return docs;
    }

    /**
     * Update document.
     */
    public boolean update(Document doc) throws SQLException {
        String sql = "UPDATE documents SET title = ?, content = ?, status = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, doc.getTitle());
            stmt.setString(2, doc.getContent());
            stmt.setString(3, doc.getStatus());
            stmt.setInt(4, doc.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Delete document (cascade should remove shares and attachments).
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM documents WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Search documents by title or content for a specific user (owned or shared readable).
     */
    public List<Document> searchByTitleOrContent(String keyword, int userId) throws SQLException {
        List<Document> docs = new ArrayList<>();
        String sql = "SELECT DISTINCT d.*, u.username as owner_name FROM documents d " +
                     "JOIN users u ON d.created_by = u.id " +
                     "WHERE (d.title LIKE ? OR d.content LIKE ?) AND " +
                     "(d.created_by = ? OR d.id IN (SELECT document_id FROM shared_documents WHERE shared_with_user_id = ?)) " +
                     "ORDER BY d.updated_at DESC";
        String like = "%" + keyword + "%";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, like);
            stmt.setString(2, like);
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    docs.add(extractDocument(rs));
                }
            }
        }
        return docs;
    }

    private Document extractDocument(ResultSet rs) throws SQLException {
        Document doc = new Document();
        doc.setId(rs.getInt("id"));
        doc.setTitle(rs.getString("title"));
        doc.setContent(rs.getString("content"));
        doc.setCreatedBy(rs.getInt("created_by"));
        doc.setOwnerName(rs.getString("owner_name"));
        doc.setCreatedAt(rs.getTimestamp("created_at"));
        doc.setUpdatedAt(rs.getTimestamp("updated_at"));
        doc.setStatus(rs.getString("status"));
        return doc;
    }
}