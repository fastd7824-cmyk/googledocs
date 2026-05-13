<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DocsFlow Editor | <c:out value="${empty document.title ? 'Untitled document' : document.title}" /></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/editor.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/responsive.css">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700;800&family=Source+Sans+3:wght@400;600;700&family=Space+Grotesk:wght@500;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">
    <script src="https://cdn.quilljs.com/1.3.6/quill.js"></script>
</head>
<body
    class="editor-body"
    data-context-path="${pageContext.request.contextPath}"
    data-can-edit="${canEdit}"
    data-user-id="${sessionScope.user.id}"
    data-user-name="<c:out value='${empty sessionScope.user.fullName ? sessionScope.user.username : sessionScope.user.fullName}' />"
    data-document-version="${empty documentVersion ? 0 : documentVersion}">
    <div class="docs-editor-page">
        <header class="docs-editor-header">
            <div class="docs-editor-brand">
                <a href="${pageContext.request.contextPath}/dashboard" class="docs-logo-link" aria-label="Back to dashboard">
                    <span class="docs-logo-mark"><i class="fas fa-file-lines"></i></span>
                </a>
                <div class="docs-title-stack">
                    <input
                        type="text"
                        id="docTitle"
                        class="docs-title-input"
                        value="<c:out value='${document.title}' />"
                        placeholder="Untitled document">
                    <div class="docs-header-actions">
                        <a href="${pageContext.request.contextPath}/dashboard" class="docs-header-link"><i class="fas fa-arrow-left"></i> Dashboard</a>
                        <button type="button" class="docs-header-link" id="starToggle"><i class="far fa-star"></i> Star</button>
                        <span class="docs-chip"><i class="fas fa-lock-open"></i> <c:out value="${canEdit ? 'Editable' : 'Read-only'}" /></span>
                    </div>
                </div>
            </div>
            <div class="docs-editor-controls">
                <div class="docs-collaborator-stack" aria-label="Live collaborators">
                    <div class="docs-avatar-row" id="collaboratorAvatars"></div>
                    <span class="docs-live-pill" id="collaborationState"><i class="fas fa-circle"></i> Live sync</span>
                </div>
                <select id="docStatus" class="docs-select">
                    <option value="draft" ${document.status == 'draft' || empty document.status ? 'selected' : ''}>Draft</option>
                    <option value="published" ${document.status == 'published' ? 'selected' : ''}>Published</option>
                    <option value="archived" ${document.status == 'archived' ? 'selected' : ''}>Archived</option>
                </select>
                <button type="button" id="shareQuickBtn" class="docs-secondary-btn"><i class="fas fa-share-alt"></i> Share</button>
                <button type="button" id="uploadBtn" class="docs-secondary-btn"><i class="fas fa-paperclip"></i> Attach</button>
                <button type="button" id="saveDocumentBtn" class="docs-primary-btn"><i class="fas fa-save"></i> Save</button>
            </div>
        </header>

        <nav class="docs-menu-bar" aria-label="Editor menus">
            <div class="docs-menu-group">
                <button type="button" class="docs-menu-trigger" data-menu-trigger="fileMenu">File</button>
                <div class="docs-menu-dropdown" id="fileMenu">
                    <button type="button" data-action="file-new"><i class="fas fa-file"></i> New document</button>
                    <button type="button" data-action="file-copy"><i class="fas fa-copy"></i> Make a copy</button>
                    <button type="button" data-action="file-rename"><i class="fas fa-i-cursor"></i> Rename</button>
                    <div class="docs-menu-divider"></div>
                    <button type="button" data-action="file-save"><i class="fas fa-save"></i> Save</button>
                    <button type="button" data-action="file-download-html"><i class="fas fa-code"></i> Download HTML</button>
                    <button type="button" data-action="file-download-txt"><i class="fas fa-file-alt"></i> Download text</button>
                    <button type="button" data-action="file-print"><i class="fas fa-print"></i> Print / Save as PDF</button>
                </div>
            </div>
            <div class="docs-menu-group">
                <button type="button" class="docs-menu-trigger" data-menu-trigger="editMenu">Edit</button>
                <div class="docs-menu-dropdown" id="editMenu">
                    <button type="button" data-action="edit-undo"><i class="fas fa-undo"></i> Undo</button>
                    <button type="button" data-action="edit-redo"><i class="fas fa-redo"></i> Redo</button>
                    <div class="docs-menu-divider"></div>
                    <button type="button" data-action="edit-cut"><i class="fas fa-cut"></i> Cut</button>
                    <button type="button" data-action="edit-copy"><i class="fas fa-copy"></i> Copy</button>
                    <button type="button" data-action="edit-paste"><i class="fas fa-paste"></i> Paste</button>
                    <button type="button" data-action="edit-select-all"><i class="fas fa-object-group"></i> Select all</button>
                    <button type="button" data-action="edit-find"><i class="fas fa-search"></i> Find and replace</button>
                </div>
            </div>
            <div class="docs-menu-group">
                <button type="button" class="docs-menu-trigger" data-menu-trigger="viewMenu">View</button>
                <div class="docs-menu-dropdown" id="viewMenu">
                    <button type="button" data-action="view-mode-paged"><i class="far fa-file"></i> Paged layout</button>
                    <button type="button" data-action="view-mode-focus"><i class="fas fa-expand"></i> Focus layout</button>
                    <button type="button" data-action="view-toggle-toolbar"><i class="fas fa-sliders-h"></i> Toggle toolbar</button>
                    <button type="button" data-action="view-toggle-sidebar"><i class="fas fa-columns"></i> Toggle sidebar</button>
                    <button type="button" data-action="view-fullscreen"><i class="fas fa-up-right-and-down-left-from-center"></i> Full screen</button>
                    <div class="docs-menu-divider"></div>
                    <button type="button" data-action="view-zoom-in"><i class="fas fa-search-plus"></i> Zoom in</button>
                    <button type="button" data-action="view-zoom-out"><i class="fas fa-search-minus"></i> Zoom out</button>
                    <button type="button" data-action="view-zoom-reset"><i class="fas fa-expand-arrows-alt"></i> Reset zoom</button>
                </div>
            </div>
            <div class="docs-menu-group">
                <button type="button" class="docs-menu-trigger" data-menu-trigger="insertMenu">Insert</button>
                <div class="docs-menu-dropdown" id="insertMenu">
                    <button type="button" data-action="insert-link"><i class="fas fa-link"></i> Link</button>
                    <button type="button" data-action="insert-image"><i class="fas fa-image"></i> Image from URL</button>
                    <button type="button" data-action="insert-table"><i class="fas fa-table"></i> Simple table</button>
                    <button type="button" data-action="insert-divider"><i class="fas fa-minus"></i> Horizontal line</button>
                    <button type="button" data-action="insert-date"><i class="far fa-calendar-alt"></i> Current date</button>
                    <button type="button" data-action="insert-signature"><i class="fas fa-signature"></i> Signature block</button>
                </div>
            </div>
            <div class="docs-menu-group">
                <button type="button" class="docs-menu-trigger" data-menu-trigger="formatMenu">Format</button>
                <div class="docs-menu-dropdown" id="formatMenu">
                    <button type="button" data-action="format-bold"><i class="fas fa-bold"></i> Bold</button>
                    <button type="button" data-action="format-italic"><i class="fas fa-italic"></i> Italic</button>
                    <button type="button" data-action="format-underline"><i class="fas fa-underline"></i> Underline</button>
                    <div class="docs-menu-divider"></div>
                    <button type="button" data-action="format-h1"><i class="fas fa-heading"></i> Heading 1</button>
                    <button type="button" data-action="format-h2"><i class="fas fa-heading"></i> Heading 2</button>
                    <button type="button" data-action="format-paragraph"><i class="fas fa-paragraph"></i> Normal text</button>
                    <div class="docs-menu-divider"></div>
                    <button type="button" data-action="format-bullets"><i class="fas fa-list-ul"></i> Bulleted list</button>
                    <button type="button" data-action="format-numbers"><i class="fas fa-list-ol"></i> Numbered list</button>
                    <button type="button" data-action="format-quote"><i class="fas fa-quote-left"></i> Block quote</button>
                    <button type="button" data-action="format-code"><i class="fas fa-code"></i> Code block</button>
                    <div class="docs-menu-divider"></div>
                    <button type="button" data-action="format-left"><i class="fas fa-align-left"></i> Align left</button>
                    <button type="button" data-action="format-center"><i class="fas fa-align-center"></i> Align center</button>
                    <button type="button" data-action="format-right"><i class="fas fa-align-right"></i> Align right</button>
                    <button type="button" data-action="format-clear"><i class="fas fa-eraser"></i> Clear formatting</button>
                </div>
            </div>
            <div class="docs-menu-group">
                <button type="button" class="docs-menu-trigger" data-menu-trigger="toolsMenu">Tools</button>
                <div class="docs-menu-dropdown" id="toolsMenu">
                    <button type="button" data-action="tools-word-count"><i class="fas fa-calculator"></i> Word count</button>
                    <button type="button" data-action="tools-reading-time"><i class="far fa-clock"></i> Reading time</button>
                    <button type="button" data-action="tools-outline"><i class="fas fa-stream"></i> Build outline</button>
                    <button type="button" data-action="tools-cleanup"><i class="fas fa-broom"></i> Cleanup spacing</button>
                    <button type="button" data-action="tools-preferences"><i class="fas fa-sliders-h"></i> Preferences</button>
                </div>
            </div>
            <div class="docs-menu-group">
                <button type="button" class="docs-menu-trigger" data-menu-trigger="extensionsMenu">Extensions</button>
                <div class="docs-menu-dropdown" id="extensionsMenu">
                    <button type="button" data-action="extensions-upload"><i class="fas fa-paperclip"></i> Upload attachment</button>
                    <button type="button" data-action="extensions-template-notes"><i class="fas fa-sticky-note"></i> Meeting notes template</button>
                    <button type="button" data-action="extensions-template-brief"><i class="fas fa-briefcase"></i> Project brief template</button>
                    <button type="button" data-action="extensions-template-letter"><i class="fas fa-envelope-open-text"></i> Letter template</button>
                    <button type="button" data-action="extensions-copy-link"><i class="fas fa-link"></i> Copy current document link</button>
                </div>
            </div>
            <div class="docs-menu-group">
                <button type="button" class="docs-menu-trigger" data-menu-trigger="helpMenu">Help</button>
                <div class="docs-menu-dropdown" id="helpMenu">
                    <button type="button" data-action="help-shortcuts"><i class="fas fa-keyboard"></i> Keyboard shortcuts</button>
                    <button type="button" data-action="help-writing"><i class="fas fa-lightbulb"></i> Writing tips</button>
                    <button type="button" data-action="help-about"><i class="fas fa-info-circle"></i> About DocsFlow</button>
                </div>
            </div>
        </nav>

        <section class="docs-toolbar-shell">
            <div class="docs-toolbar-meta">
                <div class="docs-toolbar-left">
                    <button type="button" class="docs-icon-btn" id="menuToggleMobile" aria-label="Toggle menus"><i class="fas fa-bars"></i></button>
                    <span class="docs-meta-pill" id="saveIndicator">Ready to edit</span>
                    <span class="docs-meta-pill" id="liveWordCount"><i class="fas fa-calculator"></i> 0 words</span>
                    <span class="docs-meta-pill" id="zoomLabel"><i class="fas fa-search"></i> 100%</span>
                </div>
                <div class="docs-toolbar-right">
                    <span class="docs-meta-pill"><i class="fas fa-user"></i> <c:out value="${sessionScope.user.fullName}" /></span>
                    <span class="docs-meta-pill" id="lastSavedLabel"><i class="fas fa-cloud"></i> Autosave ready</span>
                </div>
            </div>
            <div class="docs-ruler">
                <span>0</span><span>1</span><span>2</span><span>3</span><span>4</span><span>5</span><span>6</span><span>7</span><span>8</span><span>9</span><span>10</span>
            </div>
        </section>

        <div class="docs-editor-shell" id="editorShell">
            <aside class="docs-editor-sidebar" id="editorSidebar">
                <section class="docs-side-card">
                    <h3>Document Details</h3>
                    <p>Autosave, version checks, and shared write access keep collaborators aligned while they edit together.</p>
                    <ul class="docs-side-list">
                        <li><strong>Status:</strong> <span id="sidebarStatusLabel"><c:out value="${empty document.status ? 'draft' : document.status}" /></span></li>
                        <li><strong>Mode:</strong> <c:out value="${canEdit ? 'Editable' : 'Read-only'}" /></li>
                        <li><strong>Document ID:</strong> <c:out value="${empty document.id ? 'New' : document.id}" /></li>
                        <li><strong>Version:</strong> <span id="sidebarVersionLabel">${empty documentVersion ? 0 : documentVersion}</span></li>
                    </ul>
                </section>
                <section class="docs-side-card docs-presence-card">
                    <h3>Live Collaboration</h3>
                    <div id="presenceList" class="docs-presence-list"></div>
                </section>
                <section class="docs-side-card">
                    <h3>Quick Inserts</h3>
                    <div class="docs-side-actions">
                        <button type="button" class="docs-side-btn" data-action="insert-date">Insert date</button>
                        <button type="button" class="docs-side-btn" data-action="insert-signature">Insert signature</button>
                        <button type="button" class="docs-side-btn" data-action="extensions-template-notes">Meeting notes</button>
                        <button type="button" class="docs-side-btn" data-action="extensions-template-brief">Project brief</button>
                    </div>
                </section>
                <section class="docs-side-card">
                    <h3>Helpful Tips</h3>
                    <p>Use the menus above for formatting, inserts, exports, and preferences. Keyboard shortcuts work for save, bold, italic, underline, undo, and redo.</p>
                </section>
                <section class="docs-side-card">
                    <div class="docs-attachments-header">
                        <h3>Attachments</h3>
                        <button type="button" class="docs-side-btn" id="refreshAttachmentsBtn">Refresh</button>
                    </div>
                    <p>Open uploaded files, import editable text files into the current document, and share the document normally to give others access.</p>
                    <div id="attachmentsList" class="docs-attachments-list">
                        <div class="docs-attachment-empty">Save and upload files to manage them here.</div>
                    </div>
                </section>
            </aside>

            <main class="docs-canvas-wrap" id="canvasWrap">
                <div class="docs-page-surface" id="pageSurface">
                    <div class="docs-editor-stage">
                        <div id="editor-container"></div>
                    </div>
                </div>
            </main>
        </div>
    </div>

    <div class="docs-modal" id="findReplaceModal" aria-hidden="true">
        <div class="docs-modal-card">
            <div class="docs-modal-header">
                <h3>Find and Replace</h3>
                <button type="button" class="docs-icon-btn" data-close-modal="findReplaceModal"><i class="fas fa-times"></i></button>
            </div>
            <div class="docs-modal-body">
                <div class="form-group">
                    <label for="findText">Find</label>
                    <input type="text" id="findText" class="form-control" placeholder="Search text">
                </div>
                <div class="form-group">
                    <label for="replaceText">Replace With</label>
                    <input type="text" id="replaceText" class="form-control" placeholder="Replacement text">
                </div>
            </div>
            <div class="docs-modal-footer">
                <button type="button" class="docs-secondary-btn" id="findNextBtn">Find Next</button>
                <button type="button" class="docs-primary-btn" id="replaceAllBtn">Replace All</button>
            </div>
        </div>
    </div>

    <div class="docs-modal" id="preferencesModal" aria-hidden="true">
        <div class="docs-modal-card">
            <div class="docs-modal-header">
                <h3>Editor Preferences</h3>
                <button type="button" class="docs-icon-btn" data-close-modal="preferencesModal"><i class="fas fa-times"></i></button>
            </div>
            <div class="docs-modal-body">
                <div class="form-group">
                    <label for="prefFontSize">Font Size</label>
                    <select id="prefFontSize" class="form-control">
                        <option value="15px">Small</option>
                        <option value="17px" selected>Medium</option>
                        <option value="19px">Large</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="prefLineHeight">Line Height</label>
                    <select id="prefLineHeight" class="form-control">
                        <option value="1.55">Compact</option>
                        <option value="1.75" selected>Comfortable</option>
                        <option value="2">Spacious</option>
                    </select>
                </div>
            </div>
            <div class="docs-modal-footer">
                <button type="button" class="docs-secondary-btn" data-close-modal="preferencesModal">Cancel</button>
                <button type="button" class="docs-primary-btn" id="savePreferencesBtn">Apply</button>
            </div>
        </div>
    </div>

    <div class="docs-modal" id="infoModal" aria-hidden="true">
        <div class="docs-modal-card">
            <div class="docs-modal-header">
                <h3 id="infoModalTitle">DocsFlow</h3>
                <button type="button" class="docs-icon-btn" data-close-modal="infoModal"><i class="fas fa-times"></i></button>
            </div>
            <div class="docs-modal-body">
                <div id="infoModalContent" class="docs-info-copy"></div>
            </div>
            <div class="docs-modal-footer">
                <button type="button" class="docs-primary-btn" data-close-modal="infoModal">Close</button>
            </div>
        </div>
    </div>

    <input type="hidden" id="docId" value="${document.id}">
    <input type="file" id="fileInput" hidden>
    <textarea id="documentContent" hidden><c:out value="${document.content}" /></textarea>

    <script src="${pageContext.request.contextPath}/assets/js/editor.js"></script>
</body>
</html>
