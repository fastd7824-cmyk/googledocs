package com.app.controller;

import com.app.dao.ActivityLogDAO;
import com.app.dao.DocumentDAO;
import com.app.dao.UserDAO;
import com.app.model.ActivityLog;
import com.app.model.User;
import com.app.service.ActivityLogService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(urlPatterns = {"/admin", "/admin/users", "/admin/analytics", "/admin/logs"})
public class AdminServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AdminServlet.class.getName());
    private UserDAO userDAO = new UserDAO();
    private DocumentDAO documentDAO = new DocumentDAO();
    private ActivityLogDAO activityLogDAO = new ActivityLogDAO();
    private ActivityLogService activityLogService = new ActivityLogService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !"Admin".equals(currentUser.getRoleName())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required");
            return;
        }

        String path = req.getServletPath();
        try {
            switch (path) {
                case "/admin":
                    showDashboard(req, resp);
                    break;
                case "/admin/users":
                    listUsers(req, resp);
                    break;
                case "/admin/analytics":
                    getAnalyticsJson(req, resp);
                    break;
                case "/admin/logs":
                    showActivityLogs(req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Admin database error", e);
            req.setAttribute("error", "Database error: " + e.getMessage());
            req.getRequestDispatcher("/admin.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendError(403);
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !"Admin".equals(currentUser.getRoleName())) {
            resp.sendError(403);
            return;
        }

        String action = req.getParameter("action");
        try {
            if ("toggleUserStatus".equals(action)) {
                int userId = Integer.parseInt(req.getParameter("userId"));
                boolean activate = "true".equals(req.getParameter("activate"));
                User target = userDAO.findById(userId);
                if (target != null && target.getId() != currentUser.getId()) {
                    String sql = "UPDATE users SET is_active = ? WHERE id = ?";
                    try (Connection conn = com.app.config.DatabaseConfig.getConnection();
                         java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setBoolean(1, activate);
                        stmt.setInt(2, userId);
                        stmt.executeUpdate();
                    }
                    activityLogService.logActivity(currentUser.getId(),
                            activate ? "ACTIVATE_USER" : "DEACTIVATE_USER",
                            "User " + target.getUsername() + " " + (activate ? "activated" : "deactivated"), req);
                }
                resp.sendRedirect(req.getContextPath() + "/admin/users");
            } else if ("deleteUser".equals(action)) {
                int userId = Integer.parseInt(req.getParameter("userId"));
                User target = userDAO.findById(userId);
                if (target != null && target.getId() != currentUser.getId()) {
                    try (Connection conn = com.app.config.DatabaseConfig.getConnection();
                         java.sql.PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                        stmt.setInt(1, userId);
                        stmt.executeUpdate();
                    }
                    activityLogService.logActivity(currentUser.getId(), "DELETE_USER",
                            "Deleted user " + target.getUsername(), req);
                }
                resp.sendRedirect(req.getContextPath() + "/admin/users");
            } else {
                resp.sendError(400);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Admin POST error", e);
            resp.sendError(500);
        }
    }

    private void showDashboard(HttpServletRequest req, HttpServletResponse resp) throws SQLException, ServletException, IOException {
        int totalUsers = userDAO.findAllUsers().size();
        int activeUsers = 0;
        for (User u : userDAO.findAllUsers()) if (u.isActive()) activeUsers++;
        
        int totalDocuments = 0;
        try (Connection conn = com.app.config.DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM documents")) {
            if (rs.next()) totalDocuments = rs.getInt(1);
        }
        
        int totalShares = 0;
        try (Connection conn = com.app.config.DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM shared_documents")) {
            if (rs.next()) totalShares = rs.getInt(1);
        }
        
        List<ActivityLog> recentLogs = activityLogDAO.findRecent(20);
        req.setAttribute("totalUsers", totalUsers);
        req.setAttribute("activeUsers", activeUsers);
        req.setAttribute("totalDocuments", totalDocuments);
        req.setAttribute("totalShares", totalShares);
        req.setAttribute("recentLogs", recentLogs);
        req.getRequestDispatcher("/admin.jsp").forward(req, resp);
    }

    private void listUsers(HttpServletRequest req, HttpServletResponse resp) throws SQLException, ServletException, IOException {
        List<User> users = userDAO.findAllUsers();
        req.setAttribute("users", users);
        req.getRequestDispatcher("/admin_users.jsp").forward(req, resp);
    }

    private void getAnalyticsJson(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        List<Map<String, Object>> dailyDocuments = executeAnalyticsQuery(
            "SELECT DATE(created_at) as date, COUNT(*) as count FROM documents " +
            "WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) GROUP BY DATE(created_at) ORDER BY date");
        
        List<Map<String, Object>> dailyUsers = executeAnalyticsQuery(
            "SELECT DATE(created_at) as date, COUNT(*) as count FROM users " +
            "WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) GROUP BY DATE(created_at) ORDER BY date");
        
        List<Map<String, Object>> topUsers = executeAnalyticsQuery(
            "SELECT u.username, COUNT(d.id) as doc_count FROM users u " +
            "LEFT JOIN documents d ON u.id = d.created_by GROUP BY u.id ORDER BY doc_count DESC LIMIT 5");

        StringBuilder json = new StringBuilder();
        json.append("{");
        
        json.append("\"dailyDocuments\":[");
        for (int i = 0; i < dailyDocuments.size(); i++) {
            Map<String, Object> row = dailyDocuments.get(i);
            json.append("{");
            json.append("\"date\":\"").append(escapeJson(row.get("date").toString())).append("\",");
            json.append("\"count\":").append(row.get("count"));
            json.append("}");
            if (i < dailyDocuments.size() - 1) json.append(",");
        }
        json.append("],");
        
        json.append("\"dailyUsers\":[");
        for (int i = 0; i < dailyUsers.size(); i++) {
            Map<String, Object> row = dailyUsers.get(i);
            json.append("{");
            json.append("\"date\":\"").append(escapeJson(row.get("date").toString())).append("\",");
            json.append("\"count\":").append(row.get("count"));
            json.append("}");
            if (i < dailyUsers.size() - 1) json.append(",");
        }
        json.append("],");
        
        json.append("\"topUsers\":[");
        for (int i = 0; i < topUsers.size(); i++) {
            Map<String, Object> row = topUsers.get(i);
            json.append("{");
            json.append("\"username\":\"").append(escapeJson(row.get("username").toString())).append("\",");
            json.append("\"doc_count\":").append(row.get("doc_count"));
            json.append("}");
            if (i < topUsers.size() - 1) json.append(",");
        }
        json.append("]");
        
        json.append("}");
        
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(json.toString());
        out.flush();
    }

    private void showActivityLogs(HttpServletRequest req, HttpServletResponse resp) throws SQLException, ServletException, IOException {
        String username = req.getParameter("username");
        String actionFilter = req.getParameter("action");
        List<ActivityLog> logs;
        if (username != null && !username.isEmpty()) {
            logs = activityLogDAO.findByUsername(username);
        } else if (actionFilter != null && !actionFilter.isEmpty()) {
            logs = activityLogDAO.findByAction(actionFilter);
        } else {
            logs = activityLogDAO.findRecent(100);
        }
        req.setAttribute("activityLogs", logs);
        req.getRequestDispatcher("/admin_logs.jsp").forward(req, resp);
    }

    private List<Map<String, Object>> executeAnalyticsQuery(String sql) throws SQLException {
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        try (Connection conn = com.app.config.DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            java.sql.ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                result.add(row);
            }
        }
        return result;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
