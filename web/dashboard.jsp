<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard | DocsFlow</title>
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
            <div class="content-area dashboard-grid">
                <section class="page-hero">
                    <div>
                        <div class="eyebrow">Document Workspace</div>
                        <h1>Build, share, and manage docs with the speed of a real team editor.</h1>
                        <p>Your dashboard keeps writing, sharing, search, and status tracking in one polished surface inspired by modern online document tools.</p>
                    </div>
                    <div class="hero-actions">
                        <a href="${pageContext.request.contextPath}/document/new" class="btn-primary"><i class="fas fa-plus"></i> New Document</a>
                        <a href="${pageContext.request.contextPath}/profile" class="btn-secondary"><i class="fas fa-user"></i> View Profile</a>
                    </div>
                </section>

                <section class="stats-grid">
                    <article class="stat-card">
                        <div class="stat-icon"><i class="fas fa-file-lines"></i></div>
                        <div class="stat-info">
                            <h3>${totalDocuments}</h3>
                            <p>Owned documents</p>
                        </div>
                    </article>
                    <article class="stat-card">
                        <div class="stat-icon"><i class="fas fa-user-group"></i></div>
                        <div class="stat-info">
                            <h3>${totalShared}</h3>
                            <p>Shared with you</p>
                        </div>
                    </article>
                    <article class="stat-card">
                        <div class="stat-icon"><i class="fas fa-magnifying-glass"></i></div>
                        <div class="stat-info">
                            <h3><c:out value="${empty searchKeyword ? 'Live' : 'Filtered'}" /></h3>
                            <p><c:out value="${empty searchKeyword ? 'Search-ready workspace' : searchKeyword}" /></p>
                        </div>
                    </article>
                </section>

                <section class="search-card">
                    <div class="search-input-wrap">
                        <i class="fas fa-search"></i>
                        <input type="text" id="globalSearch" value="<c:out value='${searchKeyword}'/>" placeholder="Search by title or content and press Enter">
                    </div>
                    <div class="section-actions">
                        <button type="button" class="btn-primary" onclick="performSearch()"><i class="fas fa-search"></i> Search</button>
                        <a href="${pageContext.request.contextPath}/dashboard" class="btn-ghost"><i class="fas fa-rotate-left"></i> Reset</a>
                    </div>
                </section>

                <div class="documents-layout">
                    <div class="documents-stack">
                        <section class="documents-section">
                            <div class="section-row">
                                <div class="section-heading">
                                    <h2>My Documents</h2>
                                    <p>Your active drafts and published workspaces.</p>
                                </div>
                            </div>
                            <div class="doc-grid">
                                <c:forEach var="doc" items="${myDocuments}">
                                    <article class="document-card">
                                        <div class="document-card-header">
                                            <span class="doc-badge"><i class="fas fa-file-lines"></i> Owner</span>
                                            <span class="status-pill ${doc.status == 'published' ? 'success' : 'warning'}"><c:out value="${empty doc.status ? 'draft' : doc.status}" /></span>
                                        </div>
                                        <div>
                                            <h3 class="doc-title"><c:out value="${doc.title}" /></h3>
                                            <div class="document-meta">
                                                <span><i class="fas fa-clock"></i> <fmt:formatDate value="${doc.updatedAt}" pattern="MMM d, yyyy HH:mm"/></span>
                                                <span><i class="fas fa-user"></i> <c:out value="${sessionScope.user.fullName}" /></span>
                                            </div>
                                        </div>
                                        <p class="document-snippet"><c:out value="${empty doc.content ? 'No content yet. Open the editor to start drafting.' : doc.content}" /></p>
                                        <div class="doc-actions">
                                            <a href="${pageContext.request.contextPath}/document/${doc.id}" class="doc-action-link"><i class="fas fa-pen"></i> Open</a>
                                            <button type="button" onclick="showShareModal(${doc.id})" class="doc-action-button"><i class="fas fa-share-nodes"></i> Share</button>
                                            <button type="button" onclick="deleteDocument(${doc.id})" class="doc-action-button delete"><i class="fas fa-trash"></i> Delete</button>
                                        </div>
                                    </article>
                                </c:forEach>
                                <c:if test="${empty myDocuments}">
                                    <div class="empty-state">
                                        <i class="fas fa-file-circle-plus"></i>
                                        <h3>No documents yet</h3>
                                        <p>Create your first document and this workspace will start feeling like home.</p>
                                    </div>
                                </c:if>
                            </div>
                        </section>

                        <section class="documents-section">
                            <div class="section-row">
                                <div class="section-heading">
                                    <h2>Shared With Me</h2>
                                    <p>Files you can review, read, or update with your team.</p>
                                </div>
                            </div>
                            <div class="doc-grid">
                                <c:forEach var="doc" items="${sharedDocuments}">
                                    <article class="document-card">
                                        <div class="document-card-header">
                                            <span class="doc-badge"><i class="fas fa-share-nodes"></i> Shared</span>
                                            <span class="badge"><c:out value="${doc.sharedPermission}" /></span>
                                        </div>
                                        <div>
                                            <h3 class="doc-title"><c:out value="${doc.title}" /></h3>
                                            <div class="document-meta">
                                                <span><i class="fas fa-user"></i> <c:out value="${doc.ownerName}" /></span>
                                                <span><i class="fas fa-clock"></i> <fmt:formatDate value="${doc.updatedAt}" pattern="MMM d, yyyy HH:mm"/></span>
                                            </div>
                                        </div>
                                        <p class="document-snippet"><c:out value="${empty doc.content ? 'Shared document ready to review.' : doc.content}" /></p>
                                        <div class="doc-actions">
                                            <a href="${pageContext.request.contextPath}/document/${doc.id}" class="doc-action-link"><i class="fas fa-eye"></i> Open</a>
                                        </div>
                                    </article>
                                </c:forEach>
                                <c:if test="${empty sharedDocuments}">
                                    <div class="empty-state">
                                        <i class="fas fa-user-clock"></i>
                                        <h3>No shared docs yet</h3>
                                        <p>As collaborators share work with you, it will appear here with its permission level.</p>
                                    </div>
                                </c:if>
                            </div>
                        </section>
                    </div>

                    <aside class="dashboard-side-column">
                        <section class="panel-card">
                            <div class="panel-header">
                                <div>
                                    <h2>Workflow Tips</h2>
                                    <p class="panel-subtitle">A tighter process gives the app a stronger docs-tool feel.</p>
                                </div>
                            </div>
                            <div class="mini-grid">
                                <div class="mini-card">
                                    <strong>Use statuses intentionally</strong>
                                    <p>Keep active drafts in draft mode, then publish when the content is ready to share more broadly.</p>
                                </div>
                                <div class="mini-card">
                                    <strong>Share by permission</strong>
                                    <p>Give read access for review, or write access when you want true collaborative editing.</p>
                                </div>
                                <div class="mini-card">
                                    <strong>Search across content</strong>
                                    <p>Use the global search to surface titles or body content across your accessible documents.</p>
                                </div>
                            </div>
                        </section>

                        <section class="panel-card">
                            <div class="section-heading">
                                <h2>Quick Links</h2>
                                <p class="panel-subtitle">Jump to the parts of the workspace you use most.</p>
                            </div>
                            <div class="quick-links">
                                <a href="${pageContext.request.contextPath}/document/new" class="quick-link">
                                    <strong>Start a fresh draft</strong>
                                    <p>Open the editor with a clean page and start writing immediately.</p>
                                </a>
                                <a href="${pageContext.request.contextPath}/settings" class="quick-link">
                                    <strong>Review account settings</strong>
                                    <p>Update password and keep your workspace secure.</p>
                                </a>
                                <a href="${pageContext.request.contextPath}/profile" class="quick-link">
                                    <strong>Refresh profile details</strong>
                                    <p>Keep name and email current so shared docs stay easy to manage.</p>
                                </a>
                            </div>
                        </section>
                    </aside>
                </div>

                <jsp:include page="/fragments/footer.jsp" />
            </div>
        </main>
    </div>

    <div id="shareModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <div>
                    <h3>Share Document</h3>
                    <p class="panel-subtitle">Invite a teammate by username and choose access level.</p>
                </div>
                <button type="button" class="modal-close" data-modal-close><i class="fas fa-xmark"></i></button>
            </div>
            <input type="hidden" id="shareDocId">
            <div class="form-stack">
                <div class="form-group">
                    <label for="shareUsername">Username</label>
                    <input type="text" id="shareUsername" placeholder="Enter collaborator username" class="form-control">
                </div>
                <div class="form-group">
                    <label for="sharePermission">Permission</label>
                    <select id="sharePermission" class="form-control">
                        <option value="read">Read only</option>
                        <option value="write">Read and write</option>
                    </select>
                </div>
                <button type="button" onclick="submitShare()" class="btn-primary">Share Access</button>
            </div>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/assets/js/common.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/dashboard.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/notifications.js"></script>
</body>
</html>
