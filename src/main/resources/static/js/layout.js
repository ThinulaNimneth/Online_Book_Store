/* ==========================================================================
   layout.js - loads the shared navbar/footer partials into every page and
   keeps the account block, cart count/total, and category dropdown in sync.
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
    loadCategoryMenus();
    bindDropdown($("#nav-account-strip-label"), $("#nav-account-strip-menu"));
    bindDropdown($("#strip-categories-label"), $("#strip-categories-menu"));
    bindNavSearch();
}

//dropdown on the top search bar
function bindNavSearch() {
    const $form = $("#nav-search-form");
    const $input = $("#nav-search-input");
    const $results = $("#nav-search-results");
    if ($form.length === 0 || $input.length === 0 || $results.length === 0) return;

    let debounceTimer = null;
    let activeIndex = -1;
    let requestToken = 0;

    function closeResults() {
        $results.removeClass("open").empty();
        activeIndex = -1;
    }

    function highlight(index) {
        const $items = $results.find(".nav-search-result-item");
        $items.removeClass("active");
        if (index >= 0 && index < $items.length) {
            $items.eq(index).addClass("active");
            $items.get(index).scrollIntoView({ block: "nearest" });
        }
        activeIndex = index;
    }

    function goToBook(bookId) {
        if (!bookId) return;
        closeResults();
        $input.val("");
        window.location.href = "book-details.html?id=" + bookId;
    }

    function coverHtml(book) {
        if (book.images && book.images.length) {
            return `<img src="${book.images[0]}" alt="">`;
        }
        const initials = (book.title || "?").trim().slice(0, 2).toUpperCase();
        return `<span>${escapeHtmlBasic(initials)}</span>`;
    }

    function renderLoading() {
        $results.html('<div class="nav-search-loading">Searching&hellip;</div>').addClass("open");
    }

    function renderEmpty() {
        $results.html(`
            <div class="nav-search-empty">
                <strong>No books found</strong>
                <span>Try a different search term or browse another category.</span>
            </div>
        `).addClass("open");
    }

    function renderResults(books) {
        if (!books.length) { renderEmpty(); return; }

        const itemsHtml = books.slice(0, 8).map(book => {
            const hasDiscount = book.specialPrice && book.specialPrice < book.price;
            const priceHtml = formatLKR(hasDiscount ? book.specialPrice : book.price);
            return `
                <div class="nav-search-result-item" data-book-id="${book.id}">
                    <div class="ns-cover">${coverHtml(book)}</div>
                    <div class="ns-title">${escapeHtmlBasic(book.title)}</div>
                    <div class="ns-price">${priceHtml}</div>
                </div>
            `;
        }).join("");

        $results.html(itemsHtml).addClass("open");
        activeIndex = -1;
    }

    function runSearch(query) {
        const myToken = ++requestToken;
        renderLoading();
        api.get("/books?keyword=" + encodeURIComponent(query))
            .done(res => {
                if (myToken !== requestToken) return; // a newer keystroke/search superseded this one
                renderResults(res.body || []);
            })
            .fail(() => {
                if (myToken !== requestToken) return;
                renderEmpty();
            });
    }

    $input.on("input", function () {
        const q = $(this).val().trim();
        clearTimeout(debounceTimer);

        if (q.length < 2) {
            requestToken++;
            closeResults();
            return;
        }

        debounceTimer = setTimeout(() => runSearch(q), 300);
    });

    $input.on("focus", function () {
        const q = $(this).val().trim();
        if (q.length >= 2 && $results.find(".nav-search-result-item, .nav-search-empty").length) {
            $results.addClass("open");
        }
    });

    $input.on("keydown", function (e) {
        const $items = $results.find(".nav-search-result-item");

        if (e.key === "ArrowDown") {
            e.preventDefault();
            if ($items.length) highlight(Math.min(activeIndex + 1, $items.length - 1));
        } else if (e.key === "ArrowUp") {
            e.preventDefault();
            if ($items.length) highlight(Math.max(activeIndex - 1, 0));
        } else if (e.key === "Escape") {
            closeResults();
        }
    });

    $results.on("click", ".nav-search-result-item", function () {
        goToBook($(this).data("book-id"));
    });

    $form.on("submit", function (e) {
        e.preventDefault();
        const q = $input.val().trim();
        if (!q) { closeResults(); return; }

        const $items = $results.find(".nav-search-result-item");

        // If the user arrow-keyed to a result, Enter opens that book.
        if (activeIndex >= 0 && $items.eq(activeIndex).length) {
            goToBook($items.eq(activeIndex).data("book-id"));
            return;
        }

        //
        clearTimeout(debounceTimer);
        if (q.length >= 2) runSearch(q);
    });

    $(document).on("click.navSearchClose", function (e) {
        if (!$(e.target).closest("#nav-search-form").length) closeResults();
    });
}


function loadCategoryMenus() {
    api.get("/categories")
        .done(res => renderCategoryMenus(res.body || []))
        .fail(() => { /*API call fails */ });
}

function renderCategoryMenus(categories) {
    if (!categories.length) return;



    const linksHtml = categories
        .map(c => `<a href="category.html?category=${encodeURIComponent((c.name || "").trim())}">${escapeHtmlBasic(c.name)}</a>`)
        .join("");
    $("#category-dropdown-menu").html(linksHtml);
    $("#strip-categories-menu").html(linksHtml);
}




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

        // Clicking opens a real dropdown menu.
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