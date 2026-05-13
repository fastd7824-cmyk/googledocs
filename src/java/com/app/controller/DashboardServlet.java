package com.app.controller;

import com.app.dao.DocumentDAO;
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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DashboardServlet.class.getName());
    private DocumentDAO documentDAO = new DocumentDAO();
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

        String searchKeyword = req.getParameter("search");
        try {
            List<Document> myDocuments;
            List<Document> sharedDocuments;

            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                // Search results (both owned and shared)
                List<Document> searchResults = documentDAO.searchByTitleOrContent(searchKeyword, currentUser.getId());
                myDocuments = new ArrayList<>();
                sharedDocuments = new ArrayList<>();

                // Load full shared documents with permissions
                List<Document> allSharedWithPerm = documentDAO.findSharedWithUser(currentUser.getId());
                Set<Integer> sharedDocIds = new HashSet<>();
                for (Document sharedDoc : allSharedWithPerm) {
                    if (sharedDoc != null) {
                        sharedDocIds.add(sharedDoc.getId());
                    }
                }

                int currentUserId = currentUser.getId();
                for (Document doc : searchResults) {
                    if (doc == null) continue;
                    Integer createdBy = doc.getCreatedBy(); // returns Integer (object)
                    if (createdBy != null && createdBy.intValue() == currentUserId) {
                        myDocuments.add(doc);
                    } else if (sharedDocIds.contains(doc.getId())) {
                        // Find the shared version with permission info
                        for (Document sharedDoc : allSharedWithPerm) {
                            if (sharedDoc != null && sharedDoc.getId() == doc.getId()) {
                                sharedDocuments.add(sharedDoc);
                                break;
                            }
                        }
                    }
                }
                req.setAttribute("searchKeyword", searchKeyword);
            } else {
                myDocuments = documentDAO.findByUser(currentUser.getId());
                sharedDocuments = documentDAO.findSharedWithUser(currentUser.getId());
            }

            req.setAttribute("myDocuments", myDocuments != null ? myDocuments : new ArrayList<>());
            req.setAttribute("sharedDocuments", sharedDocuments != null ? sharedDocuments : new ArrayList<>());
            req.setAttribute("totalDocuments", myDocuments != null ? myDocuments.size() : 0);
            req.setAttribute("totalShared", sharedDocuments != null ? sharedDocuments.size() : 0);

            activityLogService.logActivity(currentUser.getId(), "VIEW_DASHBOARD", "User viewed dashboard", req);
            req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Dashboard DB error", e);
            req.setAttribute("error", "Unable to load your documents. Please try again later.");
            req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
