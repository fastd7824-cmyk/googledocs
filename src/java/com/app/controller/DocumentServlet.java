package com.app.controller;

import com.app.dao.DocumentDAO;
import com.app.dao.SharedDocumentDAO;
import com.app.model.Document;
import com.app.model.User;
import com.app.service.ActivityLogService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

@WebServlet("/document/*")
public class DocumentServlet extends HttpServlet {
    private DocumentDAO documentDAO = new DocumentDAO();
    private SharedDocumentDAO sharedDocumentDAO = new SharedDocumentDAO();
    private ActivityLogService activityLogService = new ActivityLogService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || "/new".equals(pathInfo)) {
            req.setAttribute("canEdit", true);
            req.getRequestDispatcher("/editor.jsp").forward(req, resp);
            return;
        }

        try {
            int docId = Integer.parseInt(pathInfo.substring(1));
            Document doc = documentDAO.findById(docId);
            if (doc == null) {
                resp.sendError(404, "Document not found");
                return;
            }
            boolean canRead = doc.getCreatedBy() == currentUser.getId()
                    || sharedDocumentDAO.hasPermission(docId, currentUser.getId(), "read");
            boolean canWrite = doc.getCreatedBy() == currentUser.getId()
                    || sharedDocumentDAO.hasPermission(docId, currentUser.getId(), "write");
            if (!canRead) {
                resp.sendError(403, "Access denied");
                return;
            }
            req.setAttribute("document", doc);
            req.setAttribute("canEdit", canWrite);
            req.setAttribute("documentVersion", getVersion(doc));
            req.getRequestDispatcher("/editor.jsp").forward(req, resp);
        } catch (NumberFormatException e) {
            resp.sendError(400, "Invalid document ID");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        try {
            if ("sync".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                Document doc = documentDAO.findById(id);
                if (doc == null) {
                    resp.sendError(404, "Document not found");
                    return;
                }
                boolean canRead = doc.getCreatedBy() == currentUser.getId()
                        || sharedDocumentDAO.hasPermission(id, currentUser.getId(), "read");
                if (!canRead) {
                    resp.sendError(403);
                    return;
                }
                writeDocumentJson(resp, doc, doc.getCreatedBy() == currentUser.getId()
                        || sharedDocumentDAO.hasPermission(id, currentUser.getId(), "write"));
                return;
            }

            if ("save".equals(action)) {
                String idParam = req.getParameter("id");
                String title = req.getParameter("title");
                String content = req.getParameter("content");
                String status = req.getParameter("status");
                String baseVersionParam = req.getParameter("baseVersion");
                boolean wantsJson = acceptsJson(req);

                if (idParam != null && !idParam.isEmpty()) {
                    int id = Integer.parseInt(idParam);
                    Document doc = documentDAO.findById(id);
                    if (doc != null && (doc.getCreatedBy() == currentUser.getId() ||
                            sharedDocumentDAO.hasPermission(id, currentUser.getId(), "write"))) {
                        long baseVersion = parseLong(baseVersionParam);
                        if (wantsJson && baseVersion > 0 && getVersion(doc) > baseVersion) {
                            resp.setStatus(HttpServletResponse.SC_CONFLICT);
                            writeDocumentJson(resp, doc, true);
                            return;
                        }
                        doc.setTitle(title);
                        doc.setContent(content);
                        doc.setStatus(status);
                        documentDAO.update(doc);
                        activityLogService.logActivity(currentUser.getId(), "UPDATE_DOCUMENT", "Updated doc " + id, req);
                        Document savedDoc = documentDAO.findById(id);
                        if (wantsJson) {
                            writeDocumentJson(resp, savedDoc != null ? savedDoc : doc, true);
                        } else {
                            resp.sendRedirect(req.getContextPath() + "/dashboard?success=updated");
                        }
                    } else {
                        resp.sendError(403);
                    }
                } else {
                    Document doc = new Document();
                    doc.setTitle(title);
                    doc.setContent(content);
                    doc.setCreatedBy(currentUser.getId());
                    doc.setStatus(status != null ? status : "draft");
                    documentDAO.create(doc);
                    activityLogService.logActivity(currentUser.getId(), "CREATE_DOCUMENT", "Created doc: " + title, req);
                    if (wantsJson) {
                        writeDocumentJson(resp, documentDAO.findById(doc.getId()), true);
                    } else {
                        resp.sendRedirect(req.getContextPath() + "/dashboard?success=created");
                    }
                }
            } else if ("delete".equals(action)) {
                int docId = Integer.parseInt(req.getParameter("id"));
                Document doc = documentDAO.findById(docId);
                if (doc != null && doc.getCreatedBy() == currentUser.getId()) {
                    documentDAO.delete(docId);
                    activityLogService.logActivity(currentUser.getId(), "DELETE_DOCUMENT", "Deleted doc " + docId, req);
                    resp.sendRedirect(req.getContextPath() + "/dashboard?success=deleted");
                } else {
                    resp.sendError(403);
                }
            } else {
                resp.sendError(400);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private boolean acceptsJson(HttpServletRequest req) {
        String requestedWith = req.getHeader("X-Requested-With");
        String accept = req.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains("application/json"));
    }

    private long getVersion(Document doc) {
        Date updatedAt = doc != null ? doc.getUpdatedAt() : null;
        Date createdAt = doc != null ? doc.getCreatedAt() : null;
        if (updatedAt != null) {
            return updatedAt.getTime();
        }
        if (createdAt != null) {
            return createdAt.getTime();
        }
        return 0L;
    }

    private void writeDocumentJson(HttpServletResponse resp, Document doc, boolean canEdit) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{"
                + "\"id\":" + doc.getId() + ","
                + "\"title\":\"" + json(doc.getTitle()) + "\","
                + "\"content\":\"" + json(doc.getContent()) + "\","
                + "\"status\":\"" + json(doc.getStatus()) + "\","
                + "\"version\":" + getVersion(doc) + ","
                + "\"canEdit\":" + canEdit
                + "}");
    }

    private String json(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (c < 32) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
                    break;
            }
        }
        return escaped.toString();
    }

    private long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
