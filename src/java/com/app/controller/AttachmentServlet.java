package com.app.controller;

import com.app.dao.DocumentAttachmentDAO;
import com.app.dao.DocumentDAO;
import com.app.dao.SharedDocumentDAO;
import com.app.model.Document;
import com.app.model.DocumentAttachment;
import com.app.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.List;

@WebServlet(urlPatterns = {"/attachment/*", "/attachment"})
public class AttachmentServlet extends HttpServlet {
    private static final long MAX_IMPORT_BYTES = 1024L * 1024L * 2L;

    private final DocumentAttachmentDAO attachmentDAO = new DocumentAttachmentDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final SharedDocumentDAO sharedDocumentDAO = new SharedDocumentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireUser(req, resp);
        if (user == null) {
            return;
        }

        String pathInfo = req.getPathInfo();
        String action = req.getParameter("action");

        try {
            if ("list".equals(action)) {
                listAttachments(req, resp, user);
                return;
            }

            if (pathInfo == null || pathInfo.length() <= 1) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Attachment ID required");
                return;
            }

            int attachmentId = Integer.parseInt(pathInfo.substring(1));
            DocumentAttachment attachment = attachmentDAO.findById(attachmentId);
            if (attachment == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Attachment not found");
                return;
            }

            Document document = documentDAO.findById(attachment.getDocumentId());
            if (!canRead(document, user.getId())) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return;
            }

            if ("import".equals(action)) {
                importAttachment(resp, attachment);
                return;
            }

            streamAttachment(req, resp, attachment);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid attachment ID");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void listAttachments(HttpServletRequest req, HttpServletResponse resp, User user) throws SQLException, IOException {
        String docIdStr = req.getParameter("docId");
        if (docIdStr == null || docIdStr.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Document ID required");
            return;
        }

        int docId = Integer.parseInt(docIdStr);
        Document document = documentDAO.findById(docId);
        if (!canRead(document, user.getId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        List<DocumentAttachment> attachments = attachmentDAO.findByDocumentId(docId);
        StringBuilder json = new StringBuilder();
        json.append("[");
        for (int i = 0; i < attachments.size(); i++) {
            DocumentAttachment attachment = attachments.get(i);
            String extension = getExtension(attachment.getFileName());
            json.append("{")
                .append("\"id\":").append(attachment.getId()).append(",")
                .append("\"fileName\":\"").append(escapeJson(attachment.getFileName())).append("\",")
                .append("\"fileSize\":").append(attachment.getFileSize()).append(",")
                .append("\"uploadedAt\":\"").append(attachment.getUploadedAt() != null ? escapeJson(attachment.getUploadedAt().toString()) : "").append("\",")
                .append("\"extension\":\"").append(escapeJson(extension)).append("\",")
                .append("\"importable\":").append(isImportable(extension, attachment.getFileSize()))
                .append("}");
            if (i < attachments.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.toString());
    }

    private void importAttachment(HttpServletResponse resp, DocumentAttachment attachment) throws IOException {
        String extension = getExtension(attachment.getFileName());
        if (!isImportable(extension, attachment.getFileSize())) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "This attachment type cannot be imported into the editor");
            return;
        }

        File file = resolveStoredFile(attachment);
        if (!file.exists()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Stored file not found");
            return;
        }

        String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        String escapedContent = escapeJson(content)
                .replace("\r\n", "\\n")
                .replace("\n", "\\n");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write("{\"fileName\":\"" + escapeJson(attachment.getFileName()) + "\",\"content\":\"" + escapedContent + "\"}");
    }

    private void streamAttachment(HttpServletRequest req, HttpServletResponse resp, DocumentAttachment attachment) throws IOException {
        File file = resolveStoredFile(attachment);
        if (!file.exists()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Stored file not found");
            return;
        }

        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) {
            mimeType = getServletContext().getMimeType(file.getName());
        }
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        String disposition = "download".equals(req.getParameter("mode")) ? "attachment" : "inline";
        resp.setContentType(mimeType);
        resp.setHeader("Content-Disposition", disposition + "; filename=\"" + attachment.getFileName().replace("\"", "") + "\"");
        resp.setContentLengthLong(file.length());
        Files.copy(file.toPath(), resp.getOutputStream());
    }

    private File resolveStoredFile(DocumentAttachment attachment) {
        String relativePath = attachment.getFilePath().replace("/", File.separator);
        String appRoot = getServletContext().getRealPath("/");
        return new File(appRoot, relativePath);
    }

    private boolean canRead(Document document, int userId) throws SQLException {
        return document != null && (document.getCreatedBy() == userId || sharedDocumentDAO.hasPermission(document.getId(), userId, "read"));
    }

    private User requireUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        User user = (User) session.getAttribute("user");
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        return user;
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex >= 0 ? fileName.substring(dotIndex + 1).toLowerCase() : "";
    }

    private boolean isImportable(String extension, long size) {
        return size <= MAX_IMPORT_BYTES &&
                ("txt".equals(extension) ||
                 "md".equals(extension) ||
                 "csv".equals(extension) ||
                 "json".equals(extension) ||
                 "xml".equals(extension) ||
                 "html".equals(extension) ||
                 "htm".equals(extension) ||
                 "java".equals(extension) ||
                 "js".equals(extension) ||
                 "css".equals(extension));
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}
