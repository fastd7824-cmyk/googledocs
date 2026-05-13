(function () {
    var searchInput = document.getElementById("globalSearch");
    var shareModal = document.getElementById("shareModal");
    var shareDocIdInput = document.getElementById("shareDocId");
    var shareUsernameInput = document.getElementById("shareUsername");
    var sharePermissionInput = document.getElementById("sharePermission");
    var contextPath = document.body.dataset.contextPath || "";

    window.performSearch = function () {
        if (!searchInput) {
            return;
        }
        var query = searchInput.value.trim();
        var url = contextPath + "/dashboard";
        if (query) {
            url += "?search=" + encodeURIComponent(query);
        }
        window.location.href = url;
    };

    window.deleteDocument = function (docId) {
        if (!window.confirm("Delete this document permanently?")) {
            return;
        }

        var form = document.createElement("form");
        form.method = "post";
        form.action = contextPath + "/document/" + docId;
        form.innerHTML =
            '<input type="hidden" name="action" value="delete">' +
            '<input type="hidden" name="id" value="' + docId + '">';
        document.body.appendChild(form);
        form.submit();
    };

    window.showShareModal = function (docId) {
        if (!shareModal) {
            return;
        }
        shareDocIdInput.value = docId;
        shareUsernameInput.value = "";
        sharePermissionInput.value = "read";
        shareModal.classList.add("show");
        shareUsernameInput.focus();
    };

    window.submitShare = function () {
        var docId = shareDocIdInput.value;
        var username = shareUsernameInput.value.trim();
        var permission = sharePermissionInput.value;

        if (!username) {
            window.alert("Enter a username to share with.");
            return;
        }

        fetch(contextPath + "/share", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
            },
            body:
                "docId=" + encodeURIComponent(docId) +
                "&username=" + encodeURIComponent(username) +
                "&permission=" + encodeURIComponent(permission)
        })
            .then(function (response) {
                if (!response.ok) {
                    return response.text().then(function (message) {
                        throw new Error(message || "Unable to share document.");
                    });
                }
                return response.json();
            })
            .then(function () {
                shareModal.classList.remove("show");
                window.alert("Document shared successfully.");
            })
            .catch(function (error) {
                window.alert(error.message || "Share failed.");
            });
    };

    if (searchInput) {
        searchInput.addEventListener("keydown", function (event) {
            if (event.key === "Enter") {
                event.preventDefault();
                window.performSearch();
            }
        });
    }
})();
