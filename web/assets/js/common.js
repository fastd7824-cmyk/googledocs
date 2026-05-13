(function () {
    function closeAllDropdowns(except) {
        document.querySelectorAll(".dropdown.open").forEach(function (dropdown) {
            if (dropdown !== except) {
                dropdown.classList.remove("open");
            }
        });
    }

    document.addEventListener("click", function (event) {
        var toggle = event.target.closest("[data-dropdown-toggle]");
        if (toggle) {
            var dropdown = toggle.closest(".dropdown");
            var shouldOpen = !dropdown.classList.contains("open");
            closeAllDropdowns(dropdown);
            dropdown.classList.toggle("open", shouldOpen);
            return;
        }

        if (!event.target.closest(".dropdown")) {
            closeAllDropdowns(null);
        }
    });

    document.querySelectorAll("[data-modal-close]").forEach(function (button) {
        button.addEventListener("click", function () {
            var modal = button.closest(".modal");
            if (modal) {
                modal.classList.remove("show");
            }
        });
    });

    document.querySelectorAll(".modal").forEach(function (modal) {
        modal.addEventListener("click", function (event) {
            if (event.target === modal) {
                modal.classList.remove("show");
            }
        });
    });

    var mobileMenuButton = document.querySelector(".mobile-menu-toggle");
    if (mobileMenuButton) {
        mobileMenuButton.addEventListener("click", function () {
            document.body.classList.toggle("sidebar-open");
        });
    }
})();
