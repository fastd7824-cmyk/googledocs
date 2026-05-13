(function () {
    var contextPath = document.body.dataset.contextPath || "";
    var list = document.getElementById("notificationList");
    var badge = document.getElementById("notificationBadge");

    function formatTimestamp(value) {
        if (!value) {
            return "";
        }
        try {
            return new Date(value).toLocaleString();
        } catch (error) {
            return value;
        }
    }

    function renderNotifications(notifications) {
        if (!list) {
            return;
        }

        if (!notifications.length) {
            list.innerHTML = '<div class="notification-empty">No notifications yet.</div>';
        } else {
            list.innerHTML = notifications.map(function (notification) {
                return '' +
                    '<div class="notification-item ' + (notification.read ? 'read' : 'unread') + '">' +
                    '<strong>' + notification.title + '</strong>' +
                    '<p>' + notification.message + '</p>' +
                    '<div class="notification-meta">' +
                    '<small>' + formatTimestamp(notification.createdAt) + '</small>' +
                    (notification.read ? '' : '<button class="text-btn" data-notification-id="' + notification.id + '">Mark read</button>') +
                    '</div>' +
                    '</div>';
            }).join("");
        }

        var unreadCount = notifications.filter(function (notification) {
            return !notification.read;
        }).length;
        if (badge) {
            badge.textContent = unreadCount > 9 ? "9+" : String(unreadCount);
            badge.classList.toggle("show", unreadCount > 0);
        }
    }

    function loadNotifications() {
        if (!list) {
            return;
        }
        fetch(contextPath + "/api/notifications")
            .then(function (response) {
                if (!response.ok) {
                    throw new Error("Unable to load notifications.");
                }
                return response.json();
            })
            .then(renderNotifications)
            .catch(function () {
                list.innerHTML = '<div class="notification-empty">Notifications are unavailable right now.</div>';
            });
    }

    window.markAllNotificationsRead = function () {
        fetch(contextPath + "/api/notifications?action=markAllRead", { method: "POST" })
            .then(loadNotifications);
    };

    document.addEventListener("click", function (event) {
        var button = event.target.closest("[data-notification-id]");
        if (!button) {
            return;
        }

        var id = button.getAttribute("data-notification-id");
        fetch(contextPath + "/api/notifications?action=markRead&id=" + encodeURIComponent(id), { method: "POST" })
            .then(loadNotifications);
    });

    loadNotifications();
})();
