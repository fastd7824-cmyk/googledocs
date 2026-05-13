package com.app.controller;

import com.app.dao.DocumentDAO;
import com.app.model.Document;
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

@WebServlet("/search")
public class SearchServlet extends HttpServlet {
    private DocumentDAO documentDAO = new DocumentDAO();

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

        String keyword = req.getParameter("q");
        if (keyword == null || keyword.trim().isEmpty()) {
            resp.sendError(400);
            return;
        }

        try {
            List<Document> results = documentDAO.searchByTitleOrContent(keyword, user.getId());
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            PrintWriter out = resp.getWriter();
            out.print(toJson(results));
            out.flush();
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Convert a list of Document objects to JSON string manually.
     */
    private String toJson(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            sb.append("{");
            sb.append("\"id\":").append(doc.getId()).append(",");
            sb.append("\"title\":\"").append(escapeJson(doc.getTitle())).append("\",");
            sb.append("\"content\":\"").append(escapeJson(doc.getContent())).append("\",");
            sb.append("\"createdBy\":").append(doc.getCreatedBy()).append(",");
            sb.append("\"ownerName\":\"").append(escapeJson(doc.getOwnerName())).append("\",");
            sb.append("\"status\":\"").append(escapeJson(doc.getStatus())).append("\",");
            sb.append("\"createdAt\":\"").append(doc.getCreatedAt() != null ? doc.getCreatedAt().getTime() : 0).append("\",");
            sb.append("\"updatedAt\":\"").append(doc.getUpdatedAt() != null ? doc.getUpdatedAt().getTime() : 0).append("\"");
            if (doc.getSharedPermission() != null) {
                sb.append(",\"sharedPermission\":\"").append(escapeJson(doc.getSharedPermission())).append("\"");
            }
            sb.append("}");
            if (i < documents.size() - 1) {
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