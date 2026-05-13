package com.app.controller;

import com.app.dao.UserDAO;
import com.app.model.User;
import com.app.service.ActivityLogService;
import com.app.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private ActivityLogService activityLogService = new ActivityLogService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            req.setAttribute("error", "Username and password required");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        try {
            User user = userDAO.findByUsername(username);
            if (user == null) user = userDAO.findByEmail(username);

            if (user != null && user.isActive() && PasswordUtil.checkPassword(password, user.getPasswordHash())) {
                HttpSession session = req.getSession();
                session.setMaxInactiveInterval(30 * 60);
                session.setAttribute("user", user);
                session.setAttribute("role", user.getRoleName());
                userDAO.updateLastLogin(user.getId());
                activityLogService.logActivity(user.getId(), "LOGIN", "User logged in", req);

                if ("Admin".equals(user.getRoleName())) {
                    resp.sendRedirect(req.getContextPath() + "/admin");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/dashboard");
                }
            } else {
                req.setAttribute("error", "Invalid username/email or password");
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}