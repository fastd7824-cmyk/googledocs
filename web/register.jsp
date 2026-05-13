<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register | DocsFlow</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/responsive.css">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700;800&family=Source+Sans+3:wght@400;600;700&family=Space+Grotesk:wght@500;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body class="auth-body">
    <div class="auth-layout">
        <section class="auth-hero">
            <div class="eyebrow">Set Up Your Workspace</div>
            <h1>Create an account and start drafting in minutes.</h1>
            <p>Register once, then move between the dashboard, rich editor, sharing tools, profile, and notifications without losing your flow.</p>
            <div class="feature-list">
                <div class="feature-item">
                    <strong>Personal dashboard</strong>
                    <span>See your documents, collaboration queue, and search-ready workspace at a glance.</span>
                </div>
                <div class="feature-item">
                    <strong>Permission-aware editing</strong>
                    <span>Open shared files with the right access level and keep editing boundaries clear.</span>
                </div>
                <div class="feature-item">
                    <strong>Admin-friendly foundation</strong>
                    <span>The project still keeps its servlet and admin structure while feeling much more polished.</span>
                </div>
            </div>
        </section>

        <section class="auth-card">
            <div class="eyebrow">Register</div>
            <h1>Create your account</h1>
            <p class="auth-subtitle">Choose your identity details, then sign in and start working.</p>
            <c:if test="${not empty error}">
                <div class="alert alert-error" style="margin-top: 22px;"><i class="fas fa-circle-exclamation"></i> ${error}</div>
            </c:if>
            <form action="${pageContext.request.contextPath}/register" method="post" class="auth-form" style="margin-top: 22px;">
                <div class="form-group">
                    <label for="username">Username</label>
                    <input type="text" id="username" name="username" required placeholder="Choose a username">
                </div>
                <div class="form-group">
                    <label for="fullName">Full Name</label>
                    <input type="text" id="fullName" name="fullName" required placeholder="Your full name">
                </div>
                <div class="form-group">
                    <label for="email">Email</label>
                    <input type="email" id="email" name="email" required placeholder="Enter your email">
                </div>
                <div class="form-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" required placeholder="Choose a strong password">
                </div>
                <div class="form-group">
                    <label for="confirmPassword">Confirm Password</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" required placeholder="Repeat your password">
                </div>
                <button type="submit" class="btn-primary btn-block">Create Account <i class="fas fa-arrow-right"></i></button>
            </form>
            <div class="auth-footer">
                Already registered? <a href="${pageContext.request.contextPath}/login">Sign in here</a>
            </div>
        </section>
    </div>
</body>
</html>
