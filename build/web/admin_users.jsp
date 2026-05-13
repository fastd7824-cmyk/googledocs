<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Users | DocsFlow</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/responsive.css">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700;800&family=Source+Sans+3:wght@400;600;700&family=Space+Grotesk:wght@500;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">
    <div class="app-shell">
        <jsp:include page="/fragments/sidebar.jsp" />
        <main class="main-content">
            <jsp:include page="/fragments/topbar.jsp" />
            <div class="content-area">
                <section class="page-hero">
                    <div>
                        <div class="eyebrow">Admin Users</div>
                        <h1>Manage account access and activity state.</h1>
                        <p>Activate, deactivate, and review the users who can enter the document workspace.</p>
                    </div>
                    <div class="hero-actions">
                        <a href="${pageContext.request.contextPath}/admin" class="btn-secondary"><i class="fas fa-arrow-left"></i> Admin Dashboard</a>
                    </div>
                </section>

                <section class="table-card">
                    <div class="table-toolbar">
                        <div>
                            <h2>Users</h2>
                            <p class="table-meta">Each action below is wired to the existing admin servlet.</p>
                        </div>
                    </div>
                    <table class="data-table">
                        <thead>
                            <tr><th>Name</th><th>Username</th><th>Email</th><th>Role</th><th>Status</th><th>Actions</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="user" items="${users}">
                                <tr>
                                    <td>${user.fullName}</td>
                                    <td>${user.username}</td>
                                    <td>${user.email}</td>
                                    <td>${user.roleName}</td>
                                    <td>
                                        <span class="status-pill ${user.active ? 'success' : 'danger'}">${user.active ? 'Active' : 'Inactive'}</span>
                                    </td>
                                    <td>
                                        <div class="inline-actions">
                                            <form action="${pageContext.request.contextPath}/admin/users" method="post">
                                                <input type="hidden" name="action" value="toggleUserStatus">
                                                <input type="hidden" name="userId" value="${user.id}">
                                                <input type="hidden" name="activate" value="${user.active ? 'false' : 'true'}">
                                                <button type="submit" class="doc-action-button">${user.active ? 'Deactivate' : 'Activate'}</button>
                                            </form>
                                            <form action="${pageContext.request.contextPath}/admin/users" method="post" onsubmit="return confirm('Delete this user?');">
                                                <input type="hidden" name="action" value="deleteUser">
                                                <input type="hidden" name="userId" value="${user.id}">
                                                <button type="submit" class="doc-action-button delete">Delete</button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty users}">
                                <tr><td colspan="6">No users found.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </section>

                <jsp:include page="/fragments/footer.jsp" />
            </div>
        </main>
    </div>
    <script src="${pageContext.request.contextPath}/assets/js/common.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/notifications.js"></script>
</body>
</html>
