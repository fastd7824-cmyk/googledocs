package com.app.dao;

import com.app.config.DatabaseConfig;
import com.app.model.DocumentAttachment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DocumentAttachmentDAO {

    public DocumentAttachment create(DocumentAttachment attachment) throws SQLException {
        String sql = "INSERT INTO document_attachments (document_id, file_name, file_path, file_size, uploaded_by) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, attachment.getDocumentId());
            stmt.setString(2, attachment.getFileName());
            stmt.setString(3, attachment.getFilePath());
            stmt.setLong(4, attachment.getFileSize());
            stmt.setInt(5, attachment.getUploadedBy());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    attachment.setId(rs.getInt(1));
                }
            }
        }
        return attachment;
    }

    public DocumentAttachment findById(int id) throws SQLException {
        String sql = "SELECT * FROM document_attachments WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractAttachment(rs);
                }
            }
        }
        return null;
    }

    public List<DocumentAttachment> findByDocumentId(int documentId) throws SQLException {
        List<DocumentAttachment> attachments = new ArrayList<>();
        String sql = "SELECT * FROM document_attachments WHERE document_id = ? ORDER BY uploaded_at DESC, id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, documentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attachments.add(extractAttachment(rs));
                }
            }
        }
        return attachments;
    }

    private DocumentAttachment extractAttachment(ResultSet rs) throws SQLException {
        DocumentAttachment attachment = new DocumentAttachment();
        attachment.setId(rs.getInt("id"));
        attachment.setDocumentId(rs.getInt("document_id"));
        attachment.setFileName(rs.getString("file_name"));
        attachment.setFilePath(rs.getString("file_path"));
        attachment.setFileSize(rs.getLong("file_size"));
        attachment.setUploadedBy(rs.getInt("uploaded_by"));
        attachment.setUploadedAt(rs.getTimestamp("uploaded_at"));
        return attachment;
    }
}
