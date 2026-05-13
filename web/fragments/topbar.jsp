<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="topbar">
    <div class="topbar-left">
        <button type="button" class="mobile-menu-toggle">
            <i class="fas fa-bars"></i>
        </button>
        <div class="topbar-title">
            <h1>Welcome back, ${sessionScope.user.fullName}</h1>
            <p>Pick up where you left off and keep your workspace moving.</p>
        </div>
    </div>
    <div class="topbar-search">
        <i class="fas fa-search"></i>
        <input type="search" id="topbarSearch" placeholder="Search documents from anywhere" onkeydown="if (event.key === 'Enter') { location.href='${pageContext.request.contextPath}/dashboard?search=' + encodeURIComponent(this.value); }">
    </div>
    <div class="topbar-right">
        <div class="dropdown">
            <button type="button" class="icon-button" data-dropdown-toggle>
                <i class="fas fa-bell"></i>
                <span class="notification-badge" id="notificationBadge">0</span>
            </button>
            <div class="dropdown-menu">
                <jsp:include page="/fragments/notificationPanel.jsp" />
            </div>
        </div>
        <div class="dropdown">
            <button type="button" class="profile-trigger" data-dropdown-toggle>
                <img src="https://ui-avatars.com/api/?background=2563eb&color=fff&name=${sessionScope.user.fullName}" class="avatar" alt="avatar">
                <span class="profile-trigger-text">
                    <strong>${sessionScope.user.fullName}</strong>
                    <span>${sessionScope.role}</span>
                </span>
                <i class="fas fa-chevron-down"></i>
            </button>
            <div class="dropdown-menu profile-dropdown">
                <a class="dropdown-link" href="${pageContext.request.contextPath}/profile"><i class="fas fa-user"></i> My Profile</a>
                <a class="dropdown-link" href="${pageContext.request.contextPath}/settings"><i class="fas fa-cog"></i> Settings</a>
                <a class="dropdown-link" href="${pageContext.request.contextPath}/logout"><i class="fas fa-sign-out-alt"></i> Logout</a>
            </div>
        </div>
    </div>
</div>
