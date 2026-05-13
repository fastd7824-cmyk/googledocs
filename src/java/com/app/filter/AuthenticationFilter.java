package com.app.filter;

import com.app.model.User;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Authentication Filter for Enterprise Document Manager.
 * - Ensures user is logged in for all protected pages.
 * - Allows public access to login, register, and static assets.
 * - Enforces role-based access (e.g., /admin/* requires Admin role).
 * - Handles session timeout gracefully.
 */
@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    // Public URIs that do NOT require authentication
    private static final Set<String> PUBLIC_URIS = new HashSet<>(Arrays.asList(
            "/login",
            "/register",
            "/assets/",
            "/fragments/",
            "/"
    ));

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String contextPath = req.getContextPath();
        String requestUri = req.getRequestURI();
        String path = requestUri.substring(contextPath.length());

        // 1. Allow public resources and pages
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Check if user is authenticated
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null) {
            // Not logged in → redirect to login page
            res.sendRedirect(contextPath + "/login");
            return;
        }

        // 3. Role-based access control for admin area
        if (path.startsWith("/admin")) {
            String roleName = currentUser.getRoleName();
            if (roleName == null || !"Admin".equals(roleName)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Admin role required.");
                return;
            }
        }

        // Optional: Add session timeout warning (e.g., set an attribute for frontend)
        // Refresh session timeout on each request
        if (session != null) {
            session.setAttribute("lastActivity", System.currentTimeMillis());
        }

        // 4. Proceed to the requested resource
        chain.doFilter(request, response);
    }

    /**
     * Determines if the request path is public (no authentication required).
     *
     * @param path the request path (without context path)
     * @return true if the path is public, false otherwise
     */
    private boolean isPublicPath(String path) {
        if (path == null || path.isEmpty()) {
            return true;
        }
        // Exact matches
        if (PUBLIC_URIS.contains(path)) {
            return true;
        }
        // Prefix matches (e.g., /assets/css/, /fragments/sidebar.jsp)
        for (String publicPrefix : PUBLIC_URIS) {
            if (path.startsWith(publicPrefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        // Cleanup logic if needed
    }
}