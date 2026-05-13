<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard | DocsFlow</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/responsive.css">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700;800&family=Source+Sans+3:wght@400;600;700&family=Space+Grotesk:wght@500;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body data-context-path="${pageContext.request.contextPath}">
    <div class="app-shell">
        <jsp:include page="/fragments/sidebar.jsp" />
        <main class="main-content">
            <jsp:include page="/fragments/topbar.jsp" />
            <div class="content-area dashboard-grid">
                <section class="page-hero">
                    <div>
                        <div class="eyebrow">Admin Analytics</div>
                        <h1>Keep the document platform healthy, active, and reviewable.</h1>
                        <p>Track adoption, collaboration volume, and recent system actions from a more polished control surface.</p>
                    </div>
                    <div class="hero-actions">
                        <a href="${pageContext.request.contextPath}/admin/users" class="btn-primary"><i class="fas fa-users"></i> Manage Users</a>
                        <a href="${pageContext.request.contextPath}/admin/logs" class="btn-secondary"><i class="fas fa-history"></i> View Logs</a>
                    </div>
                </section>

                <section class="stats-grid">
                    <article class="stat-card">
                        <div class="stat-icon"><i class="fas fa-users"></i></div>
                        <div class="stat-info"><h3>${totalUsers}</h3><p>Total users</p></div>
                    </article>
                    <article class="stat-card">
                        <div class="stat-icon"><i class="fas fa-user-check"></i></div>
                        <div class="stat-info"><h3>${activeUsers}</h3><p>Active users</p></div>
                    </article>
                    <article class="stat-card">
                        <div class="stat-icon"><i class="fas fa-file-lines"></i></div>
                        <div class="stat-info"><h3>${totalDocuments}</h3><p>Total documents</p></div>
                    </article>
                    <article class="stat-card">
                        <div class="stat-icon"><i class="fas fa-share-nodes"></i></div>
                        <div class="stat-info"><h3>${totalShares}</h3><p>Total shares</p></div>
                    </article>
                </section>

                <section class="charts-row">
                    <div class="chart-card">
                        <h3>Documents Created</h3>
                        <canvas id="docChart"></canvas>
                    </div>
                    <div class="chart-card">
                        <h3>User Registrations</h3>
                        <canvas id="userChart"></canvas>
                    </div>
                </section>

                <section class="chart-card">
                    <h3>Top Users by Document Count</h3>
                    <canvas id="topUsersChart"></canvas>
                </section>

                <section class="table-card">
                    <div class="table-toolbar">
                        <div>
                            <h2>Recent Activity Logs</h2>
                            <p class="table-meta">The latest user and system actions across the platform.</p>
                        </div>
                    </div>
                    <table class="data-table">
                        <thead>
                            <tr><th>User</th><th>Action</th><th>Details</th><th>IP Address</th><th>Timestamp</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="log" items="${recentLogs}">
                                <tr>
                                    <td>${log.username != null ? log.username : 'System'}</td>
                                    <td><span class="badge">${log.action}</span></td>
                                    <td>${log.details}</td>
                                    <td>${log.ipAddress}</td>
                                    <td><fmt:formatDate value="${log.timestamp}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty recentLogs}">
                                <tr><td colspan="5">No activity logs found.</td></tr>
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
    <script>
        fetch('${pageContext.request.contextPath}/admin/analytics')
            .then(function (response) { return response.json(); })
            .then(function (data) {
                new Chart(document.getElementById('docChart'), {
                    type: 'line',
                    data: {
                        labels: data.dailyDocuments.map(function (item) { return item.date; }),
                        datasets: [{ label: 'Documents', data: data.dailyDocuments.map(function (item) { return item.count; }), borderColor: '#2563eb', backgroundColor: 'rgba(37, 99, 235, 0.16)', fill: true, tension: 0.3 }]
                    }
                });
                new Chart(document.getElementById('userChart'), {
                    type: 'line',
                    data: {
                        labels: data.dailyUsers.map(function (item) { return item.date; }),
                        datasets: [{ label: 'Registrations', data: data.dailyUsers.map(function (item) { return item.count; }), borderColor: '#0f9f6e', backgroundColor: 'rgba(15, 159, 110, 0.16)', fill: true, tension: 0.3 }]
                    }
                });
                new Chart(document.getElementById('topUsersChart'), {
                    type: 'bar',
                    data: {
                        labels: data.topUsers.map(function (item) { return item.username; }),
                        datasets: [{ label: 'Documents', data: data.topUsers.map(function (item) { return item.doc_count; }), backgroundColor: ['#2563eb', '#3b82f6', '#60a5fa', '#93c5fd', '#bfdbfe'] }]
                    }
                });
            });
    </script>
</body>
</html>
