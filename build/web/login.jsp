<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login | DocsFlow</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/responsive.css">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700;800&family=Source+Sans+3:wght@400;600;700&family=Space+Grotesk:wght@500;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body class="auth-body">
    <div class="auth-layout">
        <section class="auth-hero">
            <div class="eyebrow">Google Docs Inspired</div>
            <h1>Write with clarity. Share with confidence.</h1>
            <p>DocsFlow gives this servlet app a modern document-workspace feel with polished dashboards, focused editing, and role-aware collaboration.</p>
            <div class="feature-list">
                <div class="feature-item">
                    <strong>Focused editor</strong>
                    <span>Rich text editing with cleaner controls and save state feedback.</span>
                </div>
                <div class="feature-item">
                    <strong>Team sharing</strong>
                    <span>Share documents by username and control read or write access quickly.</span>
                </div>
                <div class="feature-item">
                    <strong>Unified dashboard</strong>
                    <span>Track owned docs, shared docs, search results, and notifications in one place.</span>
                </div>
            </div>
        </section>

        <section class="auth-card">
            <div class="eyebrow">Sign In</div>
            <h1>Welcome back</h1>
            <p class="auth-subtitle">Use your username or email to reopen your workspace and continue writing.</p>
            <div class="form-stack" style="margin-top: 22px;">
                <c:if test="${not empty error}">
                    <div class="alert alert-error"><i class="fas fa-circle-exclamation"></i> ${error}</div>
                </c:if>
                <c:if test="${param.registered == 'true'}">
                    <div class="alert alert-success"><i class="fas fa-circle-check"></i> Account created successfully. You can sign in now.</div>
                </c:if>
            </div>
            <form action="${pageContext.request.contextPath}/login" method="post" class="auth-form" style="margin-top: 22px;">
                <div class="form-group">
                    <label for="username">Username or Email</label>
                    <input type="text" id="username" name="username" required placeholder="Enter email or username">
                </div>
                <div class="form-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" required placeholder="Enter your password">
                </div>
                <button type="submit" class="btn-primary btn-block">Sign In <i class="fas fa-arrow-right"></i></button>
            </form>
            <div class="auth-footer">
                Don't have an account? <a href="${pageContext.request.contextPath}/register">Create one</a>
            </div>
        </section>
    </div>
</body>
</html>
