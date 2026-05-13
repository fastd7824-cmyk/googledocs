<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="sidebar">
    <div class="sidebar-header">
        <div class="brand-mark"><i class="fas fa-file-alt"></i></div>
        <div class="brand-copy">
            <h2>DocsFlow</h2>
            <p>Focused writing, sharing, and team collaboration.</p>
        </div>
    </div>
    <div class="sidebar-section-title">Workspace</div>
    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/dashboard" class="nav-item ${pageContext.request.servletPath == '/dashboard' ? 'active' : ''}">
            <i class="fas fa-tachometer-alt"></i> Dashboard
        </a>
        <a href="${pageContext.request.contextPath}/document/new" class="nav-item">
            <i class="fas fa-plus-circle"></i> New Document
        </a>
        <a href="${pageContext.request.contextPath}/profile" class="nav-item ${pageContext.request.servletPath == '/profile.jsp' ? 'active' : ''}">
            <i class="fas fa-user"></i> Profile
        </a>
        <a href="${pageContext.request.contextPath}/settings" class="nav-item ${pageContext.request.servletPath == '/settings.jsp' ? 'active' : ''}">
            <i class="fas fa-cog"></i> Settings
        </a>
        <c:if test="${sessionScope.role == 'Admin'}">
            <a href="${pageContext.request.contextPath}/admin" class="nav-item ${pageContext.request.servletPath == '/admin.jsp' ? 'active' : ''}">
                <i class="fas fa-chart-line"></i> Admin Panel
            </a>
        </c:if>
        <a href="${pageContext.request.contextPath}/logout" class="nav-item logout-link">
            <i class="fas fa-sign-out-alt"></i> Logout
        </a>
    </div>
    <div class="sidebar-card">
        <strong>Write like a modern team.</strong>
        <p>Draft, share, review, and manage documents from one place with a cleaner Google Docs-style flow.</p>
    </div>
</div>
