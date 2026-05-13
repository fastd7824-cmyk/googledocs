package com.app.controller;

import com.app.dao.DocumentDAO;
import com.app.dao.SharedDocumentDAO;
import com.app.dao.UserDAO;
import com.app.model.Document;
import com.app.model.User;
import com.app.service.ActivityLogService;
import com.app.service.NotificationService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/share")
public class ShareDocumentServlet extends HttpServlet {
    private DocumentDAO documentDAO = new DocumentDAO();
    private UserDAO userDAO = new UserDAO();
    private SharedDocumentDAO sharedDocumentDAO = new SharedDocumentDAO();
    private NotificationService notificationService = new NotificationService();
    private ActivityLogService activityLogService = new ActivityLogService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendError(401);
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            resp.sendError(401);
            return;
        }

        int docId = Integer.parseInt(req.getParameter("docId"));
        String targetUsername = req.getParameter("username");
        String permission = req.getParameter("permission");

        try {
            Document doc = documentDAO.findById(docId);
            if (doc == null || doc.getCreatedBy() != currentUser.getId()) {
                resp.sendError(403, "Only owner can share");
                return;
            }

            User targetUser = userDAO.findByUsername(targetUsername);
            if (targetUser == null) {
                resp.sendError(404, "User not found");
                return;
            }

            if (targetUser.getId() == currentUser.getId()) {
                resp.sendError(400, "Cannot share with yourself");
                return;
            }

            // Save share
            boolean shared = sharedDocumentDAO.save(docId, targetUser.getId(), permission, currentUser.getId());
            if (shared) {
                notificationService.createNotification(targetUser.getId(),
                        "Document Shared",
                        currentUser.getUsername() + " shared '" + doc.getTitle() + "' with you (" + permission + " access)",
                        "share");
                activityLogService.logActivity(currentUser.getId(), "SHARE_DOCUMENT",
                        "Shared doc " + docId + " with " + targetUsername, req);
                resp.setStatus(200);
                resp.getWriter().write("{\"success\":true}");
            } else {
                resp.sendError(500, "Share failed");
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendError(401);
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            resp.sendError(401);
            return;
        }

        int docId = Integer.parseInt(req.getParameter("docId"));
        int targetUserId = Integer.parseInt(req.getParameter("userId"));

        try {
            Document doc = documentDAO.findById(docId);
            if (doc == null || doc.getCreatedBy() != currentUser.getId()) {
                resp.sendError(403);
                return;
            }
            boolean removed = sharedDocumentDAO.delete(docId, targetUserId);
            if (removed) {
                activityLogService.logActivity(currentUser.getId(), "UNSHARE_DOCUMENT",
                        "Removed share of doc " + docId + " from user " + targetUserId, req);
                resp.setStatus(200);
            } else {
                resp.sendError(404);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
