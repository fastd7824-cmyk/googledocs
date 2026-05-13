<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Settings | DocsFlow</title>
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
                        <div class="eyebrow">Security Settings</div>
                        <h1>Protect your workspace with a strong password routine.</h1>
                        <p>Use this panel to rotate your password and keep shared documents secure across the team.</p>
                    </div>
                </section>

                <div class="two-column-grid">
                    <section class="panel-card">
                        <div class="section-heading">
                            <h2>Change Password</h2>
                            <p class="panel-subtitle">Passwords should be memorable to you and hard for anyone else to guess.</p>
                        </div>
                        <div class="form-stack" style="margin-top: 18px;">
                            <c:if test="${not empty success}"><div class="alert alert-success">${success}</div></c:if>
                            <c:if test="${not empty error}"><div class="alert alert-error">${error}</div></c:if>
                        </div>
                        <form action="${pageContext.request.contextPath}/settings" method="post" class="form-stack" style="margin-top: 18px;">
                            <div class="form-group">
                                <label for="currentPassword">Current Password</label>
                                <input type="password" id="currentPassword" name="currentPassword" required class="form-control">
                            </div>
                            <div class="form-group">
                                <label for="newPassword">New Password</label>
                                <input type="password" id="newPassword" name="newPassword" required class="form-control">
                            </div>
                            <div class="form-group">
                                <label for="confirmNewPassword">Confirm New Password</label>
                                <input type="password" id="confirmNewPassword" name="confirmNewPassword" required class="form-control">
                            </div>
                            <button type="submit" class="btn-primary">Update Password</button>
                        </form>
                    </section>

                    <aside class="panel-card">
                        <div class="section-heading">
                            <h2>Security Notes</h2>
                            <p class="panel-subtitle">A few simple habits keep the workspace much safer.</p>
                        </div>
                        <div class="mini-grid" style="margin-top: 18px;">
                            <div class="mini-card">
                                <strong>Rotate regularly</strong>
                                <p>Change passwords anytime you suspect reuse or exposure.</p>
                            </div>
                            <div class="mini-card">
                                <strong>Use longer phrases</strong>
                                <p>A multi-word password is usually stronger and easier to remember.</p>
                            </div>
                            <div class="mini-card">
                                <strong>Keep sessions private</strong>
                                <p>Log out on shared devices so document access stays under your control.</p>
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
