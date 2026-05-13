<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Profile | DocsFlow</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
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
                        <div class="eyebrow">Account Profile</div>
                        <h1>Keep your collaborator identity current.</h1>
                        <p>Your profile details appear across shared documents, activity records, and account-level workspace actions.</p>
                    </div>
                    <div class="hero-actions">
                        <a href="${pageContext.request.contextPath}/dashboard" class="btn-secondary"><i class="fas fa-arrow-left"></i> Dashboard</a>
                    </div>
                </section>

                <div class="two-column-grid">
                    <section class="panel-card">
                        <div class="section-heading">
                            <h2>Update Profile</h2>
                            <p class="panel-subtitle">Edit your public identity details for cleaner collaboration.</p>
                        </div>
                        <div class="form-stack" style="margin-top: 18px;">
                            <c:if test="${not empty success}"><div class="alert alert-success">${success}</div></c:if>
                            <c:if test="${not empty error}"><div class="alert alert-error">${error}</div></c:if>
                        </div>
                        <form action="${pageContext.request.contextPath}/profile" method="post" class="form-stack" style="margin-top: 18px;">
                            <div class="form-group">
                                <label>Username</label>
                                <input type="text" value="${profileUser.username}" disabled class="form-control">
                            </div>
                            <div class="form-group">
                                <label for="fullName">Full Name</label>
                                <input type="text" id="fullName" name="fullName" value="${profileUser.fullName}" required class="form-control">
                            </div>
                            <div class="form-group">
                                <label for="email">Email</label>
                                <input type="email" id="email" name="email" value="${profileUser.email}" required class="form-control">
                            </div>
                            <div class="form-group">
                                <label>Role</label>
                                <input type="text" value="${profileUser.roleName}" disabled class="form-control">
                            </div>
                            <button type="submit" class="btn-primary">Save Profile Changes</button>
                        </form>
                    </section>

                    <aside class="panel-card">
                        <div class="section-heading">
                            <h2>Profile Snapshot</h2>
                            <p class="panel-subtitle">A quick summary of how your account appears inside the app.</p>
                        </div>
                        <div class="mini-grid" style="margin-top: 18px;">
                            <div class="mini-card">
                                <strong>Display Name</strong>
                                <p>${profileUser.fullName}</p>
                            </div>
                            <div class="mini-card">
                                <strong>Primary Email</strong>
                                <p>${profileUser.email}</p>
                            </div>
                            <div class="mini-card">
                                <strong>Role</strong>
                                <p>${profileUser.roleName}</p>
                            </div>
                            <div class="mini-card">
                                <strong>Username</strong>
                                <p>${profileUser.username}</p>
                            </div>
                        </div>
                    </aside>
                </div>

                <jsp:include page="/fragments/footer.jsp" />
            </div>
        </main>
    </div>
    <script src="${pageContext.request.contextPath}/assets/js/common.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/notifications.js"></script>
</body>
</html>
