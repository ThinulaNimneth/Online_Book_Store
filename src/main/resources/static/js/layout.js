/* ==========================================================================
   layout.js - loads the shared navbar/footer partials into every page and
   keeps the account block, cart count/total, and category dropdown in sync.
   Call initLayout() once on $(document).ready() in every page.
   ========================================================================== */

function initLayout() {
    $.get("partials/navbar.html", function (html) {
        $("#navbar-placeholder").html(html);
        bindNavbar();
        refreshNavCounts();
    });

    $.get("partials/footer.html", function (html) {
        $("#footer-placeholder").html(html);
        bindFooter();
    });
}

function bindNavbar() {
    applyAuthState();
    bindDropdown($("#nav-account-strip-label"), $("#nav-account-strip-menu"));
    bindDropdown($("#strip-categories-label"), $("#strip-categories-menu"));

    $("#nav-search-form").on("submit", function (e) {
        e.preventDefault();
        const q = $("#nav-search-input").val().trim();
        window.location.href = "index.html" + (q ? "?q=" + encodeURIComponent(q) : "");
    });
}

/**
 * Generic click-toggle dropdown: trigger opens/closes menu, menu is
 * JS-positioned (position:fixed in CSS) directly under the trigger so it
 * escapes any ancestor's overflow clipping, and closes on an outside
 * click, Escape, resize, or scroll. Used for both the "Account ▾" item in
 * the second nav strip and the main "Hi, {name} ▾" trigger next to the
 * account icon - previously the account icon just showed two stacked,
 * easily-mis-clicked links ("Hi, Name" / "Sign out") with no visible way
 * to reach the admin dashboard; this replaces that with one clear trigger
 * and a real menu.
 */
function bindDropdown($trigger, $menu) {
    if ($trigger.length === 0 || $menu.length === 0) return;

    function closeMenu() {
        $menu.removeClass("open");
        $trigger.attr("aria-expanded", "false");
    }

    function openMenu() {
        const rect = $trigger[0].getBoundingClientRect();
        $menu.css({ top: rect.bottom + 6 + "px", left: rect.left + "px" });
        $menu.addClass("open");
        $trigger.attr("aria-expanded", "true");
    }

    $trigger.attr({ role: "button", tabindex: 0, "aria-haspopup": "true", "aria-expanded": "false" });

    $trigger.off("click.dropdown").on("click.dropdown", function (e) {
        e.stopPropagation();
        $menu.hasClass("open") ? closeMenu() : openMenu();
    });

    $trigger.off("keydown.dropdown").on("keydown.dropdown", function (e) {
        if (e.key === "Enter" || e.key === " ") {
            e.preventDefault();
            $trigger.trigger("click");
        }
    });

    $menu.off("click.dropdown").on("click.dropdown", "a", closeMenu);
    $(document).on("click.dropdown-" + $trigger.attr("id"), closeMenu);
    $(window).on("resize scroll", closeMenu);
    $(document).on("keydown", function (e) {
        if (e.key === "Escape") closeMenu();
    });
}

function applyAuthState() {
    const $links = $("#nav-account-links");
    const $blockMenu = $("#nav-account-block-menu");
    const $stripMenu = $("#nav-account-strip-menu");

    if (Auth.isLoggedIn()) {
        const user = Auth.getUser() || {};
        const firstName = (user.fullName || "Account").split(" ")[0];
        const menuItemsHtml = `
            <a href="orders.html">My Orders</a>
            <a href="wishlist.html">Wishlist</a>
            ${Auth.isAdmin() ? '<a href="admin-dashboard.html">Admin Dashboard</a>' : ""}
            <a href="#" class="nav-logout-link">Sign out</a>
        `;

        // Single clear trigger - "Hi, Name ▾" - instead of two stacked,
        // easily-confused links. Clicking opens a real dropdown menu.
        $links.html(`
            <span id="nav-account-trigger">Hi, ${escapeHtmlBasic(firstName)} <span class="chevron">&#9662;</span></span>
        `);
        $blockMenu.html(menuItemsHtml);
        $stripMenu.html(menuItemsHtml);

        bindDropdown($("#nav-account-trigger"), $blockMenu);

        $(document).off("click.navLogout").on("click.navLogout", ".nav-logout-link", function (e) {
            e.preventDefault();
            Auth.logout();
        });
    } else {
        $links.html(`<a href="login.html">Sign in</a><a href="register.html">Create An Account</a>`);
        $stripMenu.html(`<a href="login.html">Sign In</a><a href="register.html">Register</a>`);
    }
}

function refreshNavCounts() {
    // TODO: once GET /carts is implemented, replace this with a real
    // api.get("/carts") call and sum quantity * unitPrice server-side.
    // For now this reads a small local cache so the nav feels alive
    // even before the backend endpoints exist.
    const cartCount = Number(localStorage.getItem("potha_cart_count") || 0);
    const cartTotal = Number(localStorage.getItem("potha_cart_total") || 0);

    $("#cart-count-text").text(cartCount);
    $("#cart-amount-text").text(formatLKR(cartTotal));
}

function bumpLocalCount(key, delta) {
    const current = Number(localStorage.getItem(key) || 0);
    localStorage.setItem(key, Math.max(0, current + delta));
    refreshNavCounts();
}

function bumpCartTotal(deltaAmount) {
    const current = Number(localStorage.getItem("potha_cart_total") || 0);
    localStorage.setItem("potha_cart_total", Math.max(0, current + deltaAmount));
    refreshNavCounts();
}

function bindFooter() {
    $("#footer-subscribe-form").on("submit", function (e) {
        e.preventDefault();
        showToast("Subscribed! (demo - no backend endpoint yet)");
        $("#footer-subscribe-input").val("");
    });
}

function escapeHtmlBasic(str) {
    return $("<div>").text(str || "").html();
}