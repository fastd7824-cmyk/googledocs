(function () {
    var contextPath = document.body.dataset.contextPath || "";
    var canEdit = document.body.dataset.canEdit === "true";
    var docIdField = document.getElementById("docId");
    var docId = docIdField ? docIdField.value : "";
    var docTitle = document.getElementById("docTitle");
    var statusInput = document.getElementById("docStatus");
    var saveButton = document.getElementById("saveDocumentBtn");
    var uploadButton = document.getElementById("uploadBtn");
    var shareQuickButton = document.getElementById("shareQuickBtn");
    var fileInput = document.getElementById("fileInput");
    var saveIndicator = document.getElementById("saveIndicator");
    var lastSavedLabel = document.getElementById("lastSavedLabel");
    var documentContent = document.getElementById("documentContent");
    var sidebarStatusLabel = document.getElementById("sidebarStatusLabel");
    var sidebarVersionLabel = document.getElementById("sidebarVersionLabel");
    var zoomLabel = document.getElementById("zoomLabel");
    var attachmentsList = document.getElementById("attachmentsList");
    var refreshAttachmentsBtn = document.getElementById("refreshAttachmentsBtn");
    var collaborationState = document.getElementById("collaborationState");
    var collaboratorAvatars = document.getElementById("collaboratorAvatars");
    var presenceList = document.getElementById("presenceList");
    var currentUserId = document.body.dataset.userId || String(Date.now());
    var currentUserName = document.body.dataset.userName || "You";
    var documentVersion = Number(document.body.dataset.documentVersion || 0);
    var quill;
    var dirty = false;
    var saving = false;
    var applyingRemoteChange = false;
    var autosaveTimer;
    var syncTimer;
    var presenceTimer;
    var zoomLevel = 1;
    var starred = false;

    function setSaveIndicator(text, tone) {
        if (!saveIndicator) {
            return;
        }
        saveIndicator.textContent = text;
        saveIndicator.className = "docs-meta-pill";
        if (tone === "warning") {
            saveIndicator.style.background = "rgba(245, 158, 11, 0.12)";
            saveIndicator.style.color = "#b77205";
        } else if (tone === "success") {
            saveIndicator.style.background = "rgba(15, 159, 110, 0.12)";
            saveIndicator.style.color = "#0f9f6e";
        } else {
            saveIndicator.style.background = "";
            saveIndicator.style.color = "";
        }
    }

    function setCollaborationState(text, tone) {
        if (!collaborationState) {
            return;
        }
        collaborationState.innerHTML = '<i class="fas fa-circle"></i> ' + escapeHtml(text);
        collaborationState.className = "docs-live-pill";
        if (tone) {
            collaborationState.classList.add("is-" + tone);
        }
    }

    function formatTime(date) {
        return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    }

    function updateLastSavedLabel() {
        if (lastSavedLabel) {
            lastSavedLabel.innerHTML = '<i class="fas fa-cloud"></i> Saved ' + formatTime(new Date());
        }
        if (sidebarVersionLabel) {
            sidebarVersionLabel.textContent = documentVersion || "Local draft";
        }
    }

    function scheduleAutosave() {
        if (!canEdit || !docId) {
            return;
        }
        window.clearTimeout(autosaveTimer);
        autosaveTimer = window.setTimeout(function () {
            saveDocument({ silent: true });
        }, 1200);
    }

    function markDirty() {
        if (!canEdit) {
            return;
        }
        dirty = true;
        setSaveIndicator("Unsaved changes", "warning");
        scheduleAutosave();
    }

    function getEditorHtml() {
        return quill ? quill.root.innerHTML : "";
    }

    function getPlainText() {
        return quill ? quill.getText().replace(/\s+$/g, "") : "";
    }

    function closeMenus() {
        document.querySelectorAll(".docs-menu-group.open").forEach(function (group) {
            group.classList.remove("open");
        });
    }

    function openInfoModal(title, html) {
        document.getElementById("infoModalTitle").textContent = title;
        document.getElementById("infoModalContent").innerHTML = html;
        document.getElementById("infoModal").classList.add("show");
    }

    function closeModal(id) {
        var modal = document.getElementById(id);
        if (modal) {
            modal.classList.remove("show");
        }
    }

    function showModal(id) {
        var modal = document.getElementById(id);
        if (modal) {
            modal.classList.add("show");
        }
    }

    function updateWordCount() {
        var text = getPlainText().trim();
        var words = text ? text.split(/\s+/).length : 0;
        var pill = document.getElementById("liveWordCount");
        if (pill) {
            pill.innerHTML = '<i class="fas fa-calculator"></i> ' + words + " words";
        }
        return words;
    }

    function getReadingTimeMinutes() {
        var words = updateWordCount();
        return Math.max(1, Math.ceil(words / 220));
    }

    function updateZoom() {
        var editor = document.querySelector(".ql-editor");
        if (!editor) {
            return;
        }
        editor.style.transform = "scale(" + zoomLevel + ")";
        editor.style.transformOrigin = "top center";
        editor.style.width = (100 / zoomLevel) + "%";
        if (zoomLabel) {
            zoomLabel.innerHTML = '<i class="fas fa-search"></i> ' + Math.round(zoomLevel * 100) + "%";
        }
    }

    function setSidebarStatus() {
        if (sidebarStatusLabel && statusInput) {
            sidebarStatusLabel.textContent = statusInput.value;
        }
    }

    function formatBytes(bytes) {
        if (!bytes && bytes !== 0) {
            return "";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return (bytes / 1024).toFixed(1) + " KB";
        }
        return (bytes / (1024 * 1024)).toFixed(1) + " MB";
    }

    function loadAttachments() {
        if (!attachmentsList) {
            return;
        }
        if (!docId) {
            attachmentsList.innerHTML = '<div class="docs-attachment-empty">Save this document first, then upload files to open, import, and manage them here.</div>';
            return;
        }

        attachmentsList.innerHTML = '<div class="docs-attachment-empty">Loading attachments...</div>';
        fetch(contextPath + "/attachment?action=list&docId=" + encodeURIComponent(docId))
            .then(function (response) {
                if (!response.ok) {
                    throw new Error("Unable to load attachments.");
                }
                return response.json();
            })
            .then(function (attachments) {
                if (!attachments.length) {
                    attachmentsList.innerHTML = '<div class="docs-attachment-empty">No attachments yet. Upload one and it will appear here.</div>';
                    return;
                }

                attachmentsList.innerHTML = attachments.map(function (attachment) {
                    var importButton = attachment.importable
                        ? '<button type="button" data-import-attachment="' + attachment.id + '"><i class="fas fa-file-import"></i> Import</button>'
                        : "";
                    return '' +
                        '<div class="docs-attachment-item">' +
                        '<strong>' + escapeHtml(attachment.fileName) + '</strong>' +
                        '<div class="docs-attachment-meta">Type: ' + escapeHtml(attachment.extension || "file") + ' · Size: ' + formatBytes(attachment.fileSize) + '</div>' +
                        '<div class="docs-attachment-actions">' +
                        '<a href="' + contextPath + '/attachment/' + attachment.id + '" target="_blank" rel="noopener"><i class="fas fa-folder-open"></i> Open</a>' +
                        '<a href="' + contextPath + '/attachment/' + attachment.id + '?mode=download"><i class="fas fa-download"></i> Download</a>' +
                        importButton +
                        '<button type="button" data-copy-attachment-link="' + attachment.id + '"><i class="fas fa-link"></i> Copy Link</button>' +
                        '<button type="button" data-share-document><i class="fas fa-share-alt"></i> Share</button>' +
                        '</div>' +
                        '</div>';
                }).join("");
            })
            .catch(function (error) {
                attachmentsList.innerHTML = '<div class="docs-attachment-empty">' + escapeHtml(error.message || "Unable to load attachments.") + '</div>';
            });
    }

    function importAttachment(attachmentId) {
        fetch(contextPath + "/attachment/" + encodeURIComponent(attachmentId) + "?action=import")
            .then(function (response) {
                if (!response.ok) {
                    return response.text().then(function (message) {
                        throw new Error(message || "Unable to import attachment.");
                    });
                }
                return response.json();
            })
            .then(function (payload) {
                quill.setText("");
                quill.clipboard.dangerouslyPasteHTML(0, "<pre>" + escapeHtml(payload.content).replace(/\n/g, "<br>") + "</pre>");
                if (!docTitle.value.trim()) {
                    docTitle.value = payload.fileName;
                }
                markDirty();
                setSaveIndicator("Imported " + payload.fileName + " into the editor", "success");
            })
            .catch(function (error) {
                setSaveIndicator(error.message || "Unable to import attachment", "warning");
            });
    }

    function saveDocument(options) {
        options = options || {};
        if (!canEdit) {
            return;
        }

        var title = (docTitle.value || "").trim();
        if (!title) {
            window.alert("Please add a document title.");
            docTitle.focus();
            return;
        }

        if (saving) {
            scheduleAutosave();
            return;
        }

        setSaveIndicator(options.silent ? "Autosaving..." : "Saving...", "");
        setCollaborationState("Syncing", "syncing");
        saving = true;
        saveButton.disabled = true;

        var params = new URLSearchParams();
        params.append("action", "save");
        params.append("id", docId);
        params.append("title", title);
        params.append("content", getEditorHtml());
        params.append("status", statusInput ? statusInput.value : "draft");
        params.append("baseVersion", documentVersion || 0);

        fetch(contextPath + "/document/" + (docId || "new"), {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
                "Accept": "application/json",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: params.toString()
        })
            .then(function (response) {
                if (response.status === 409) {
                    return response.json().then(function (payload) {
                        var error = new Error("A collaborator saved newer changes. Review the latest version before saving again.");
                        error.payload = payload;
                        error.conflict = true;
                        throw error;
                    });
                }
                if (!response.ok) {
                    throw new Error("Save failed.");
                }
                return response.json();
            })
            .then(function (payload) {
                dirty = false;
                docId = String(payload.id || docId);
                documentVersion = Number(payload.version || documentVersion);
                if (docIdField) {
                    docIdField.value = docId;
                }
                if (payload.id && /\/document\/new$/.test(window.location.pathname)) {
                    window.history.replaceState({}, "", contextPath + "/document/" + payload.id);
                }
                setSaveIndicator(options.silent ? "All changes saved" : "Saved successfully", "success");
                setCollaborationState("Live sync", "online");
                updateLastSavedLabel();
                loadAttachments();
            })
            .catch(function (error) {
                setSaveIndicator(error.message || "Save failed", "warning");
                setCollaborationState(error.conflict ? "Conflict detected" : "Offline changes", "warning");
                if (error.conflict && error.payload && window.confirm("A collaborator has newer changes. Load their latest version now? Your unsaved local edits will be replaced.")) {
                    applyRemotePayload(error.payload);
                    dirty = false;
                    setSaveIndicator("Latest collaborator version loaded", "success");
                }
            })
            .finally(function () {
                saving = false;
                saveButton.disabled = false;
            });
    }

    function applyRemotePayload(payload) {
        applyingRemoteChange = true;
        if (docTitle && payload.title !== docTitle.value) {
            docTitle.value = payload.title || "";
        }
        if (statusInput && payload.status !== statusInput.value) {
            statusInput.value = payload.status || "draft";
            setSidebarStatus();
        }
        if (quill && payload.content !== getEditorHtml()) {
            quill.setText("", "silent");
            quill.clipboard.dangerouslyPasteHTML(0, payload.content || "", "silent");
        }
        documentVersion = Number(payload.version || documentVersion);
        updateWordCount();
        updateLastSavedLabel();
        applyingRemoteChange = false;
    }

    function checkForRemoteChanges() {
        if (!docId || saving) {
            return;
        }
        var params = new URLSearchParams();
        params.append("action", "sync");
        params.append("id", docId);
        fetch(contextPath + "/document/" + encodeURIComponent(docId), {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
                "Accept": "application/json",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: params.toString()
        })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error("Sync unavailable");
                }
                return response.json();
            })
            .then(function (payload) {
                var remoteVersion = Number(payload.version || 0);
                if (remoteVersion > documentVersion) {
                    if (dirty) {
                        setCollaborationState("Remote edit waiting", "warning");
                        setSaveIndicator("A collaborator has newer changes. Save or reload before continuing.", "warning");
                    } else {
                        applyRemotePayload(payload);
                        setCollaborationState("Updated from teammate", "online");
                        setSaveIndicator("New collaborator changes loaded", "success");
                    }
                } else {
                    setCollaborationState("Live sync", "online");
                }
            })
            .catch(function () {
                setCollaborationState("Reconnecting", "warning");
            });
    }

    function uploadAttachment(file) {
        if (!file) {
            return;
        }
        if (!docId) {
            window.alert("Save the document first before uploading attachments.");
            return;
        }

        var formData = new FormData();
        formData.append("docId", docId);
        formData.append("file", file);

        setSaveIndicator("Uploading attachment...", "");
        fetch(contextPath + "/upload", {
            method: "POST",
            body: formData
        })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error("Attachment upload failed.");
                }
                return response.json();
            })
            .then(function (data) {
                setSaveIndicator("Attached " + data.fileName, "success");
                loadAttachments();
            })
            .catch(function (error) {
                setSaveIndicator(error.message || "Attachment upload failed", "warning");
            })
            .finally(function () {
                fileInput.value = "";
            });
    }

    function applyFormat(name, value) {
        if (!quill) {
            return;
        }
        quill.format(name, value);
        markDirty();
    }

    function insertAtCursor(text) {
        var range = quill.getSelection(true);
        var index = range ? range.index : quill.getLength();
        quill.insertText(index, text);
        quill.setSelection(index + text.length, 0);
        markDirty();
    }

    function insertHtml(html) {
        var range = quill.getSelection(true);
        var index = range ? range.index : quill.getLength();
        quill.clipboard.dangerouslyPasteHTML(index, html);
        markDirty();
    }

    function insertTable() {
        var rows = parseInt(window.prompt("Rows", "2"), 10);
        var cols = parseInt(window.prompt("Columns", "2"), 10);
        if (!rows || !cols || rows < 1 || cols < 1) {
            return;
        }
        var html = '<table style="width:100%; border-collapse:collapse; margin:12px 0;">';
        var rowIndex;
        var colIndex;
        for (rowIndex = 0; rowIndex < rows; rowIndex += 1) {
            html += "<tr>";
            for (colIndex = 0; colIndex < cols; colIndex += 1) {
                html += '<td style="border:1px solid #cbd5e1; padding:8px;">Cell</td>';
            }
            html += "</tr>";
        }
        html += "</table><p><br></p>";
        insertHtml(html);
    }

    function insertImageByUrl() {
        var url = window.prompt("Enter image URL");
        if (!url) {
            return;
        }
        var range = quill.getSelection(true);
        quill.insertEmbed(range ? range.index : quill.getLength(), "image", url);
        markDirty();
    }

    function insertLink() {
        var url = window.prompt("Enter link URL");
        var range = quill.getSelection(true);
        if (!url || !range || range.length === 0) {
            window.alert("Select some text first, then add a link.");
            return;
        }
        quill.format("link", url);
        markDirty();
    }

    function insertDivider() {
        insertHtml("<hr><p><br></p>");
    }

    function insertDate() {
        insertAtCursor(new Date().toLocaleDateString());
    }

    function insertSignature() {
        insertHtml(
            "<p>Best regards,</p>" +
            "<p><strong>" + escapeHtml((docTitle.value || "").trim() ? "Your Name" : "Your Name") + "</strong></p>" +
            "<p>Title</p><p>Company</p>"
        );
    }

    function applyTemplate(type) {
        var templates = {
            notes:
                "<h1>Meeting Notes</h1><p><strong>Date:</strong> " + new Date().toLocaleDateString() + "</p><p><strong>Attendees:</strong> </p><h2>Agenda</h2><ul><li></li></ul><h2>Decisions</h2><ul><li></li></ul><h2>Next Steps</h2><ul><li></li></ul>",
            brief:
                "<h1>Project Brief</h1><h2>Objective</h2><p></p><h2>Scope</h2><p></p><h2>Deliverables</h2><ul><li></li></ul><h2>Timeline</h2><p></p>",
            letter:
                "<p>" + new Date().toLocaleDateString() + "</p><p>Dear Recipient,</p><p></p><p></p><p>Sincerely,</p><p>Your Name</p>"
        };
        insertHtml(templates[type] || "");
    }

    function downloadFile(filename, content, mimeType) {
        var blob = new Blob([content], { type: mimeType });
        var link = document.createElement("a");
        link.href = URL.createObjectURL(blob);
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(link.href);
    }

    function escapeHtml(value) {
        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }

    function initials(name) {
        return String(name || "You")
            .trim()
            .split(/\s+/)
            .slice(0, 2)
            .map(function (part) { return part.charAt(0).toUpperCase(); })
            .join("") || "Y";
    }

    function presenceKey() {
        return "docsflow-presence-" + (docId || "new");
    }

    function readPresence() {
        try {
            return JSON.parse(localStorage.getItem(presenceKey()) || "{}");
        } catch (error) {
            return {};
        }
    }

    function writePresence(presence) {
        localStorage.setItem(presenceKey(), JSON.stringify(presence));
    }

    function publishPresence() {
        var presence = readPresence();
        var now = Date.now();
        Object.keys(presence).forEach(function (id) {
            if (now - presence[id].seenAt > 20000) {
                delete presence[id];
            }
        });
        presence[currentUserId] = {
            name: currentUserName,
            seenAt: now,
            status: dirty ? "Editing" : "Viewing"
        };
        writePresence(presence);
        renderPresence(presence);
    }

    function renderPresence(presence) {
        var users = Object.keys(presence).map(function (id) {
            return presence[id];
        }).sort(function (a, b) {
            return b.seenAt - a.seenAt;
        });

        if (collaboratorAvatars) {
            collaboratorAvatars.innerHTML = users.slice(0, 5).map(function (user) {
                return '<span class="docs-avatar" title="' + escapeHtml(user.name) + '">' + escapeHtml(initials(user.name)) + '</span>';
            }).join("");
        }

        if (presenceList) {
            presenceList.innerHTML = users.map(function (user) {
                return '<div class="docs-presence-item">' +
                    '<span class="docs-avatar">' + escapeHtml(initials(user.name)) + '</span>' +
                    '<span><strong>' + escapeHtml(user.name) + '</strong><small>' + escapeHtml(user.status) + ' now</small></span>' +
                    '</div>';
            }).join("");
        }
    }

    function downloadHtml() {
        var title = (docTitle.value || "document").trim();
        var html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" +
            escapeHtml(title) +
            "</title></head><body>" +
            getEditorHtml() +
            "</body></html>";
        downloadFile(title + ".html", html, "text/html;charset=utf-8");
    }

    function downloadText() {
        var title = (docTitle.value || "document").trim();
        downloadFile(title + ".txt", getPlainText(), "text/plain;charset=utf-8");
    }

    function makeCopy() {
        sessionStorage.setItem("docsflow-copy-content", getEditorHtml());
        sessionStorage.setItem("docsflow-copy-title", ((docTitle.value || "").trim() || "Copy of document"));
        window.location.href = contextPath + "/document/new";
    }

    function hydrateCopyIfNeeded() {
        if (docId) {
            return;
        }
        var copiedContent = sessionStorage.getItem("docsflow-copy-content");
        var copiedTitle = sessionStorage.getItem("docsflow-copy-title");
        if (copiedContent) {
            quill.root.innerHTML = copiedContent;
            docTitle.value = copiedTitle || "Copy of document";
            sessionStorage.removeItem("docsflow-copy-content");
            sessionStorage.removeItem("docsflow-copy-title");
            markDirty();
        }
    }

    function toggleStar() {
        starred = !starred;
        var starButton = document.getElementById("starToggle");
        if (starButton) {
            starButton.innerHTML = starred
                ? '<i class="fas fa-star"></i> Starred'
                : '<i class="far fa-star"></i> Star';
        }
    }

    function showWordCount() {
        var words = updateWordCount();
        var chars = getPlainText().replace(/\n/g, "").length;
        openInfoModal("Word Count", "<p><strong>Words:</strong> " + words + "</p><p><strong>Characters:</strong> " + chars + "</p>");
    }

    function showReadingTime() {
        openInfoModal("Reading Time", "<p>Estimated reading time: <strong>" + getReadingTimeMinutes() + " minute(s)</strong>.</p>");
    }

    function buildOutline() {
        var headings = quill.root.querySelectorAll("h1, h2, h3");
        if (!headings.length) {
            openInfoModal("Document Outline", "<p>No headings found yet. Use Format > Heading 1 or Heading 2 to build a quick outline.</p>");
            return;
        }
        var html = "<ul>";
        headings.forEach(function (heading) {
            html += "<li><strong>" + heading.tagName + ":</strong> " + escapeHtml(heading.textContent.trim()) + "</li>";
        });
        html += "</ul>";
        openInfoModal("Document Outline", html);
    }

    function cleanupSpacing() {
        var html = getEditorHtml()
            .replace(/<p><br><\/p>\s*<p><br><\/p>/g, "<p><br></p>")
            .replace(/\s{2,}/g, " ");
        quill.root.innerHTML = html;
        markDirty();
        setSaveIndicator("Spacing cleaned up", "success");
    }

    function applyPreferences() {
        var fontSize = document.getElementById("prefFontSize").value;
        var lineHeight = document.getElementById("prefLineHeight").value;
        var editor = document.querySelector(".ql-editor");
        if (editor) {
            editor.style.fontSize = fontSize;
            editor.style.lineHeight = lineHeight;
        }
        localStorage.setItem("docsflow-pref-font-size", fontSize);
        localStorage.setItem("docsflow-pref-line-height", lineHeight);
        closeModal("preferencesModal");
        setSaveIndicator("Preferences applied", "success");
    }

    function loadPreferences() {
        var fontSize = localStorage.getItem("docsflow-pref-font-size") || "17px";
        var lineHeight = localStorage.getItem("docsflow-pref-line-height") || "1.75";
        document.getElementById("prefFontSize").value = fontSize;
        document.getElementById("prefLineHeight").value = lineHeight;
        var editor = document.querySelector(".ql-editor");
        if (editor) {
            editor.style.fontSize = fontSize;
            editor.style.lineHeight = lineHeight;
        }
    }

    function findNext() {
        var value = document.getElementById("findText").value.trim();
        if (!value) {
            return;
        }
        var fullText = getPlainText();
        var currentSelection = quill.getSelection(true);
        var startIndex = currentSelection ? currentSelection.index + currentSelection.length : 0;
        var foundIndex = fullText.toLowerCase().indexOf(value.toLowerCase(), startIndex);
        if (foundIndex === -1) {
            foundIndex = fullText.toLowerCase().indexOf(value.toLowerCase());
        }
        if (foundIndex === -1) {
            window.alert("No match found.");
            return;
        }
        quill.setSelection(foundIndex, value.length, "user");
        quill.focus();
    }

    function replaceAll() {
        var findValue = document.getElementById("findText").value;
        var replaceValue = document.getElementById("replaceText").value;
        if (!findValue) {
            return;
        }
        var html = getEditorHtml().split(findValue).join(replaceValue);
        quill.root.innerHTML = html;
        markDirty();
        closeModal("findReplaceModal");
        setSaveIndicator("Replace all complete", "success");
    }

    function copyCurrentLink() {
        var link = window.location.href;
        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(link).then(function () {
                setSaveIndicator("Document link copied", "success");
            });
        } else {
            window.prompt("Copy this link", link);
        }
    }

    function copyAttachmentLink(attachmentId) {
        var link = window.location.origin + contextPath + "/attachment/" + attachmentId;
        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(link).then(function () {
                setSaveIndicator("Attachment link copied", "success");
            });
        } else {
            window.prompt("Copy this attachment link", link);
        }
    }

    function shareDocument() {
        if (!docId) {
            window.alert("Save the document first, then share it from the dashboard.");
            return;
        }
        window.location.href = contextPath + "/dashboard?share=" + encodeURIComponent(docId);
    }

    function toggleBodyClass(className) {
        document.body.classList.toggle(className);
    }

    function handleAction(action) {
        closeMenus();
        switch (action) {
            case "file-new":
                window.location.href = contextPath + "/document/new";
                break;
            case "file-copy":
                makeCopy();
                break;
            case "file-rename":
                docTitle.focus();
                docTitle.select();
                break;
            case "file-save":
                saveDocument();
                break;
            case "file-download-html":
                downloadHtml();
                break;
            case "file-download-txt":
                downloadText();
                break;
            case "file-print":
                window.print();
                break;
            case "edit-undo":
                quill.history.undo();
                break;
            case "edit-redo":
                quill.history.redo();
                break;
            case "edit-cut":
                document.execCommand("cut");
                break;
            case "edit-copy":
                document.execCommand("copy");
                break;
            case "edit-paste":
                if (navigator.clipboard && navigator.clipboard.readText) {
                    navigator.clipboard.readText().then(function (text) {
                        insertAtCursor(text);
                    }).catch(function () {
                        window.alert("Clipboard access is not available.");
                    });
                } else {
                    window.alert("Clipboard access is not available.");
                }
                break;
            case "edit-select-all":
                quill.setSelection(0, quill.getLength());
                break;
            case "edit-find":
                showModal("findReplaceModal");
                break;
            case "view-mode-paged":
                document.body.classList.remove("focus-layout");
                break;
            case "view-mode-focus":
                document.body.classList.add("focus-layout");
                break;
            case "view-toggle-toolbar":
                toggleBodyClass("hide-toolbar");
                break;
            case "view-toggle-sidebar":
                toggleBodyClass("hide-sidebar");
                break;
            case "view-fullscreen":
                if (!document.fullscreenElement) {
                    document.documentElement.requestFullscreen();
                } else {
                    document.exitFullscreen();
                }
                break;
            case "view-zoom-in":
                zoomLevel = Math.min(1.5, zoomLevel + 0.1);
                updateZoom();
                break;
            case "view-zoom-out":
                zoomLevel = Math.max(0.8, zoomLevel - 0.1);
                updateZoom();
                break;
            case "view-zoom-reset":
                zoomLevel = 1;
                updateZoom();
                break;
            case "insert-link":
                insertLink();
                break;
            case "insert-image":
                insertImageByUrl();
                break;
            case "insert-table":
                insertTable();
                break;
            case "insert-divider":
                insertDivider();
                break;
            case "insert-date":
                insertDate();
                break;
            case "insert-signature":
                insertSignature();
                break;
            case "format-bold":
                applyFormat("bold", true);
                break;
            case "format-italic":
                applyFormat("italic", true);
                break;
            case "format-underline":
                applyFormat("underline", true);
                break;
            case "format-h1":
                applyFormat("header", 1);
                break;
            case "format-h2":
                applyFormat("header", 2);
                break;
            case "format-paragraph":
                applyFormat("header", false);
                break;
            case "format-bullets":
                applyFormat("list", "bullet");
                break;
            case "format-numbers":
                applyFormat("list", "ordered");
                break;
            case "format-quote":
                applyFormat("blockquote", true);
                break;
            case "format-code":
                applyFormat("code-block", true);
                break;
            case "format-left":
                applyFormat("align", "");
                break;
            case "format-center":
                applyFormat("align", "center");
                break;
            case "format-right":
                applyFormat("align", "right");
                break;
            case "format-clear":
                var selection = quill.getSelection(true);
                if (selection) {
                    quill.removeFormat(selection.index, selection.length || 1);
                }
                markDirty();
                break;
            case "tools-word-count":
                showWordCount();
                break;
            case "tools-reading-time":
                showReadingTime();
                break;
            case "tools-outline":
                buildOutline();
                break;
            case "tools-cleanup":
                cleanupSpacing();
                break;
            case "tools-preferences":
                showModal("preferencesModal");
                break;
            case "extensions-upload":
                fileInput.click();
                break;
            case "extensions-template-notes":
                applyTemplate("notes");
                break;
            case "extensions-template-brief":
                applyTemplate("brief");
                break;
            case "extensions-template-letter":
                applyTemplate("letter");
                break;
            case "extensions-copy-link":
                copyCurrentLink();
                break;
            case "help-shortcuts":
                openInfoModal(
                    "Keyboard Shortcuts",
                    "<ul><li><strong>Ctrl+S</strong> Save</li><li><strong>Ctrl+B</strong> Bold</li><li><strong>Ctrl+I</strong> Italic</li><li><strong>Ctrl+U</strong> Underline</li><li><strong>Ctrl+Z</strong> Undo</li><li><strong>Ctrl+Y</strong> Redo</li></ul>"
                );
                break;
            case "help-writing":
                openInfoModal(
                    "Writing Tips",
                    "<p>Use headings to structure your page, keep paragraphs short, and use the Tools menu to review word count and reading time.</p><p>Templates in Extensions can help you start faster.</p>"
                );
                break;
            case "help-about":
                openInfoModal(
                    "About DocsFlow",
                    "<p>DocsFlow is a Google Docs-inspired editor layer built on your existing JSP and servlet app.</p><p>It supports menus for file actions, editing, formatting, inserts, tools, extensions, and help.</p>"
                );
                break;
            default:
                break;
        }
    }

    function bindMenus() {
        document.querySelectorAll("[data-menu-trigger]").forEach(function (trigger) {
            trigger.addEventListener("click", function (event) {
                event.stopPropagation();
                var group = trigger.parentElement;
                var isOpen = group.classList.contains("open");
                closeMenus();
                if (!isOpen) {
                    group.classList.add("open");
                }
            });
        });

        document.addEventListener("click", function (event) {
            if (!event.target.closest(".docs-menu-group")) {
                closeMenus();
            }
        });

        document.querySelectorAll("[data-action]").forEach(function (button) {
            button.addEventListener("click", function () {
                handleAction(button.getAttribute("data-action"));
            });
        });

        document.addEventListener("click", function (event) {
            var importButton = event.target.closest("[data-import-attachment]");
            var copyButton = event.target.closest("[data-copy-attachment-link]");
            var shareButton = event.target.closest("[data-share-document]");

            if (importButton) {
                importAttachment(importButton.getAttribute("data-import-attachment"));
            }
            if (copyButton) {
                copyAttachmentLink(copyButton.getAttribute("data-copy-attachment-link"));
            }
            if (shareButton) {
                shareDocument();
            }
        });
    }

    function bindModals() {
        document.querySelectorAll("[data-close-modal]").forEach(function (button) {
            button.addEventListener("click", function () {
                closeModal(button.getAttribute("data-close-modal"));
            });
        });

        document.querySelectorAll(".docs-modal").forEach(function (modal) {
            modal.addEventListener("click", function (event) {
                if (event.target === modal) {
                    modal.classList.remove("show");
                }
            });
        });

        document.getElementById("findNextBtn").addEventListener("click", findNext);
        document.getElementById("replaceAllBtn").addEventListener("click", replaceAll);
        document.getElementById("savePreferencesBtn").addEventListener("click", applyPreferences);
    }

    function bindMainControls() {
        saveButton.addEventListener("click", saveDocument);
        uploadButton.addEventListener("click", function () {
            fileInput.click();
        });
        shareQuickButton.addEventListener("click", shareDocument);
        fileInput.addEventListener("change", function () {
            uploadAttachment(fileInput.files[0]);
        });
        docTitle.addEventListener("input", markDirty);
        statusInput.addEventListener("change", function () {
            markDirty();
            setSidebarStatus();
        });
        document.getElementById("starToggle").addEventListener("click", toggleStar);
        var mobileToggle = document.getElementById("menuToggleMobile");
        if (mobileToggle) {
            mobileToggle.addEventListener("click", function () {
                toggleBodyClass("hide-sidebar");
            });
        }
        if (refreshAttachmentsBtn) {
            refreshAttachmentsBtn.addEventListener("click", loadAttachments);
        }
    }

    function setupKeyboardShortcuts() {
        document.addEventListener("keydown", function (event) {
            var ctrl = event.ctrlKey || event.metaKey;
            if (!ctrl) {
                return;
            }
            switch (event.key.toLowerCase()) {
                case "s":
                    event.preventDefault();
                    saveDocument();
                    break;
                case "b":
                    event.preventDefault();
                    applyFormat("bold", true);
                    break;
                case "i":
                    event.preventDefault();
                    applyFormat("italic", true);
                    break;
                case "u":
                    event.preventDefault();
                    applyFormat("underline", true);
                    break;
                case "z":
                    event.preventDefault();
                    quill.history.undo();
                    break;
                case "y":
                    event.preventDefault();
                    quill.history.redo();
                    break;
                case "f":
                    event.preventDefault();
                    showModal("findReplaceModal");
                    break;
                default:
                    break;
            }
        });
    }

    function initQuill() {
        quill = new Quill("#editor-container", {
            theme: "snow",
            placeholder: "Start writing your next document...",
            modules: {
                toolbar: [
                    [{ header: [1, 2, false] }],
                    [{ font: [] }],
                    [{ size: ["small", false, "large", "huge"] }],
                    ["bold", "italic", "underline", "strike"],
                    [{ color: [] }, { background: [] }],
                    [{ list: "ordered" }, { list: "bullet" }],
                    [{ align: [] }],
                    ["blockquote", "code-block", "link", "image"],
                    ["clean"]
                ],
                history: {
                    delay: 700,
                    maxStack: 100,
                    userOnly: true
                }
            },
            readOnly: !canEdit
        });

        if (documentContent && documentContent.value) {
            quill.root.innerHTML = documentContent.value;
        }

        hydrateCopyIfNeeded();
        loadPreferences();
        updateWordCount();
        setSidebarStatus();
        updateZoom();
        loadAttachments();

        quill.on("text-change", function () {
            updateWordCount();
            if (canEdit && !applyingRemoteChange) {
                markDirty();
            }
        });

        if (!canEdit) {
            docTitle.readOnly = true;
            statusInput.disabled = true;
            saveButton.disabled = true;
            uploadButton.disabled = true;
            shareQuickButton.disabled = false;
            setSaveIndicator("Read-only access", "");
        }
    }

    window.addEventListener("beforeunload", function (event) {
        if (!dirty || !canEdit) {
            return;
        }
        event.preventDefault();
        event.returnValue = "";
    });

    initQuill();
    bindMenus();
    bindModals();
    bindMainControls();
    setupKeyboardShortcuts();
    updateLastSavedLabel();
    publishPresence();
    presenceTimer = window.setInterval(publishPresence, 5000);
    syncTimer = window.setInterval(checkForRemoteChanges, 6000);

    window.addEventListener("storage", function (event) {
        if (event.key === presenceKey()) {
            renderPresence(readPresence());
        }
    });

    window.addEventListener("unload", function () {
        window.clearInterval(syncTimer);
        window.clearInterval(presenceTimer);
    });
})();
