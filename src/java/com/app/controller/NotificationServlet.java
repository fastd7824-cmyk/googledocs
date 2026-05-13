package com.app.controller;

import com.app.dao.NotificationDAO;
import com.app.model.Notification;
import com.app.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/notifications")
public class NotificationServlet extends HttpServlet {
    private NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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

        try {
            List<Notification> notifications = notificationDAO.findByUser(user.getId(), 30);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            PrintWriter out = resp.getWriter();
            out.print(toJson(notifications));
            out.flush();
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

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

        String action = req.getParameter("action");
        try {
            if ("markRead".equals(action)) {
                int notifId = Integer.parseInt(req.getParameter("id"));
                notificationDAO.markAsRead(notifId, user.getId());
                resp.setStatus(200);
            } else if ("markAllRead".equals(action)) {
                notificationDAO.markAllAsRead(user.getId());
                resp.setStatus(200);
            } else {
                resp.sendError(400);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * Convert a list of Notification objects to JSON string manually.
     */
    private String toJson(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < notifications.size(); i++) {
            Notification n = notifications.get(i);
            sb.append("{");
            sb.append("\"id\":").append(n.getId()).append(",");
            sb.append("\"userId\":").append(n.getUserId()).append(",");
            sb.append("\"title\":\"").append(escapeJson(n.getTitle())).append("\",");
            sb.append("\"message\":\"").append(escapeJson(n.getMessage())).append("\",");
            sb.append("\"type\":\"").append(escapeJson(n.getType())).append("\",");
            sb.append("\"read\":").append(n.isRead()).append(",");
            sb.append("\"createdAt\":\"").append(n.getCreatedAt() != null ? n.getCreatedAt().getTime() : 0).append("\"");
            sb.append("}");
            if (i < notifications.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Escape JSON special characters.
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