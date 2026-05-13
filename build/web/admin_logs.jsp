<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Activity Logs | DocsFlow</title>
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
                        <div class="eyebrow">Admin Logs</div>
                        <h1>Review system activity with fast filters.</h1>
                        <p>Filter by username or action to inspect authentication, document, and administration events.</p>
                    </div>
                    <div class="hero-actions">
                        <a href="${pageContext.request.contextPath}/admin" class="btn-secondary"><i class="fas fa-arrow-left"></i> Admin Dashboard</a>
                    </div>
                </section>

                <section class="panel-card">
                    <form action="${pageContext.request.contextPath}/admin/logs" method="get" class="toolbar-row">
                        <div class="search-input-wrap" style="flex: 1 1 240px;">
                            <i class="fas fa-user"></i>
                            <input type="text" name="username" value="${param.username}" placeholder="Filter by username">
                        </div>
                        <select name="action" class="filter-select" style="max-width: 240px;">
                            <option value="">All actions</option>
                            <option value="LOGIN" ${param.action == 'LOGIN' ? 'selected' : ''}>LOGIN</option>
                            <option value="CREATE_DOCUMENT" ${param.action == 'CREATE_DOCUMENT' ? 'selected' : ''}>CREATE_DOCUMENT</option>
                            <option value="UPDATE_DOCUMENT" ${param.action == 'UPDATE_DOCUMENT' ? 'selected' : ''}>UPDATE_DOCUMENT</option>
                            <option value="DELETE_DOCUMENT" ${param.action == 'DELETE_DOCUMENT' ? 'selected' : ''}>DELETE_DOCUMENT</option>
                            <option value="SHARE_DOCUMENT" ${param.action == 'SHARE_DOCUMENT' ? 'selected' : ''}>SHARE_DOCUMENT</option>
                        </select>
                        <button type="submit" class="btn-primary"><i class="fas fa-filter"></i> Apply Filters</button>
                    </form>
                </section>

                <section class="table-card">
                    <div class="table-toolbar">
                        <div>
                            <h2>Activity History</h2>
                            <p class="table-meta">Showing the latest matching activity records.</p>
                        </div>
                    </div>
                    <table class="data-table">
                        <thead>
                            <tr><th>User</th><th>Action</th><th>Details</th><th>IP Address</th><th>User Agent</th><th>Timestamp</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="log" items="${activityLogs}">
                                <tr>
                                    <td>${log.username != null ? log.username : 'System'}</td>
                                    <td><span class="badge">${log.action}</span></td>
                                    <td>${log.details}</td>
                                    <td>${log.ipAddress}</td>
                                    <td>${log.userAgent}</td>
                                    <td><fmt:formatDate value="${log.timestamp}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty activityLogs}">
                                <tr><td colspan="6">No activity logs found for the current filter.</td></tr>
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
