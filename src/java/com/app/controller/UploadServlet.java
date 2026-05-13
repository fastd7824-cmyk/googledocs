package com.app.controller;

import com.app.dao.DocumentAttachmentDAO;
import com.app.dao.DocumentDAO;
import com.app.dao.SharedDocumentDAO;
import com.app.model.Document;
import com.app.model.DocumentAttachment;
import com.app.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@WebServlet("/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,  // 1 MB
    maxFileSize = 10 * 1024 * 1024,   // 10 MB
    maxRequestSize = 10 * 1024 * 1024 // 10 MB
)
public class UploadServlet extends HttpServlet {
    private static final String UPLOAD_DIR = "assets/uploads";
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final SharedDocumentDAO sharedDocumentDAO = new SharedDocumentDAO();
    private final DocumentAttachmentDAO attachmentDAO = new DocumentAttachmentDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendError(401);
            return;
        }

        User user = (User) session.getAttribute("user");
        if (user == null) {
            resp.sendError(401);
            return;
        }

        // Check if request contains multipart content
        if (!req.getContentType().startsWith("multipart/form-data")) {
            resp.sendError(400, "Invalid content type");
            return;
        }

        try {
            // Get the document ID from form field
            String docIdStr = req.getParameter("docId");
            if (docIdStr == null || docIdStr.trim().isEmpty()) {
                resp.sendError(400, "Document ID missing");
                return;
            }
            int docId = Integer.parseInt(docIdStr);
            Document document = documentDAO.findById(docId);
            if (document == null ||
                    (document.getCreatedBy() != user.getId() &&
                     !sharedDocumentDAO.hasPermission(docId, user.getId(), "write"))) {
                resp.sendError(403, "You do not have permission to attach files to this document");
                return;
            }

            // Get the uploaded file part
            Part filePart = req.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                resp.sendError(400, "No file uploaded");
                return;
            }

            // Get original file name
            String originalName = getSubmittedFileName(filePart);
            if (originalName == null || originalName.isEmpty()) {
                resp.sendError(400, "Invalid file name");
                return;
            }

            // Build upload path
            String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Generate unique file name
            String extension = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalName.substring(dotIndex);
            }
            String uniqueName = UUID.randomUUID().toString() + extension;
            String relativePath = UPLOAD_DIR + "/" + uniqueName;
            Path absolutePath = new File(uploadPath, uniqueName).toPath();

            // Save the file using a stream copy because Part.write() can mangle absolute Windows paths on GlassFish.
            try (InputStream inputStream = filePart.getInputStream()) {
                Files.copy(inputStream, absolutePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Insert record into database
            DocumentAttachment attachment = new DocumentAttachment();
            attachment.setDocumentId(docId);
            attachment.setFileName(originalName);
            attachment.setFilePath(relativePath);
            attachment.setFileSize(filePart.getSize());
            attachment.setUploadedBy(user.getId());
            attachmentDAO.create(attachment);

            // Return JSON response
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write("{\"success\":true, \"fileName\":\"" + escapeJson(originalName) + "\",\"attachmentId\":" + attachment.getId() + "}");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * Extracts file name from Part header (workaround for Servlet API limitation).
     */
    private String getSubmittedFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition == null) return null;
        for (String token : contentDisposition.split(";")) {
            if (token.trim().startsWith("filename")) {
                String fileName = token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                // Remove any path info (some browsers send full path)
                int lastSlash = fileName.lastIndexOf('\\');
                if (lastSlash != -1) {
                    fileName = fileName.substring(lastSlash + 1);
                }
                return fileName;
            }
        }
        return null;
    }

    /**
     * Simple JSON string escaping.
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
