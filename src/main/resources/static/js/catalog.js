

$(document).ready(function () {
    initLayout();

    loadCarousel("#new-arrivals-row", "#new-arrivals-dots", "/books/new-arrivals");
    loadCarousel("#bestsellers-7d-row", "#bestsellers-7d-dots", "/books/bestsellers?range=7d");
    loadCarousel("#bestsellers-all-row", "#bestsellers-all-dots", "/books/bestsellers?range=all");
    loadTrending();
    loadCatalog();
});

function loadCarousel(rowSelector, dotsSelector, endpoint) {
    api.get(endpoint)
        .done(res => renderCarousel(rowSelector, dotsSelector, res.body || []))
        .fail(() => renderCarousel(rowSelector, dotsSelector, []));
}

function loadTrending() {
    api.get("/books/trending")
        .done(res => renderBooks("#trending-grid", res.body || []))
        .fail(() => renderBooks("#trending-grid", []));
}

function loadCatalog() {
    api.get("/books")
        .done(res => renderCatalogResult(res.body || []))
        .fail(() => {
            showToast("Couldn't reach the catalog endpoint - is the backend running?");
            renderCatalogResult([]);
        });
}

function renderCatalogResult(books) {
    renderBooks("#catalog-grid", books);
    $("#catalog-empty").toggle(books.length === 0);
}

function renderBooks(gridSelector, books) {
    const $grid = $(gridSelector);
    $grid.empty();
    books.forEach(book => $grid.append(bookCardHtml(book)));
    bindBookCardEvents();
}

// Ranked horizontal carousel (New Arrivals / Bestseller Last 7 Days / All Time)

function renderCarousel(rowSelector, dotsSelector, books) {
    const $row = $(rowSelector).empty();
    books.forEach((book, i) => $row.append(bookCardHtml(book, i + 1)));
    bindBookCardEvents();

    const $dots = $(dotsSelector).empty();
    const dotCount = Math.min(books.length, 6);
    for (let i = 0; i < dotCount; i++) {
        $dots.append(`<button type="button" class="carousel-dot ${i === 0 ? "active" : ""}" data-index="${i}"></button>`);
    }
    $dots.find(".carousel-dot").on("click", function () {
        const idx = $(this).data("index");
        const $card = $row.children().eq(idx);
        if ($card.length) $row.animate({ scrollLeft: $card.position().left + $row.scrollLeft() }, 300);
        $dots.find(".carousel-dot").removeClass("active");
        $(this).addClass("active");
    });
}

function bookCardHtml(book, rank) {
    const hasDiscount = book.specialPrice && book.specialPrice < book.price;
    const ribbon = !book.inStock
        ? '<span class="ribbon stock-out">Out of Stock</span>'
        : (book.isNew ? '<span class="ribbon">New</span>'
            : (hasDiscount ? '<span class="ribbon">-' + Math.round((1 - book.specialPrice / book.price) * 100) + '%</span>' : ''));

    const priceHtml = hasDiscount
        ? `<span class="price-special">${formatLKR(book.specialPrice)}</span><span class="price-original">${formatLKR(book.price)}</span>`
        : `<span class="price-special">${formatLKR(book.price)}</span>`;

    const rankBadge = rank ? `<span class="rank-badge">${rank}</span>` : "";
    const reviewCount = (!rank && book.reviewCount) ? `<span class="review-count">(${book.reviewCount})</span>` : "";

    return `
    <div class="book-card" data-book-id="${book.id}" data-price="${hasDiscount ? book.specialPrice : book.price}">
        <a href="book-details.html?id=${book.id}">
            <div class="book-cover">
                ${ribbon}
                <button class="wishlist-toggle" data-book-id="${book.id}" aria-label="Save to wishlist">
                    <svg viewBox="0 0 24 24" fill="none"><path d="M12 20s-7-4.35-9.5-8.5C.7 8.1 2.4 4.5 6 4.5c2 0 3.4 1.1 4 2.2.6-1.1 2-2.2 4-2.2 3.6 0 5.3 3.6 3.5 7C19 15.65 12 20 12 20z" stroke="currentColor" stroke-width="1.8"/></svg>
                </button>
                ${escapeHtml(book.title)}
                ${rankBadge}
            </div>
            <div class="book-info">
                <div class="book-category">${escapeHtml(book.category || "")}</div>
                <div class="book-title">${escapeHtml(book.title)}</div>
                <div class="book-author">by ${escapeHtml(book.author)}</div>
                <div class="rating"><span class="stars">${starString(book.rating)}</span>${reviewCount}</div>
                <div class="price-row">${priceHtml}</div>
            </div>
        </a>
        <div class="book-actions">
            <button class="btn btn-primary btn-sm btn-block add-to-cart-btn" data-book-id="${book.id}" ${!book.inStock ? "disabled" : ""}>
                ${book.inStock ? "Add to Cart" : "Out of Stock"}
            </button>
        </div>
    </div>`;
}

function bindBookCardEvents() {
    $(".wishlist-toggle").off("click").on("click", function (e) {
        e.preventDefault();
        e.stopPropagation();
        const bookId = $(this).data("book-id");
        $(this).toggleClass("active");
        addToWishlist(bookId);
    });

    $(".add-to-cart-btn").off("click").on("click", function (e) {
        e.preventDefault();
        e.stopPropagation();
        const $card = $(this).closest(".book-card");
        const bookId = $card.data("book-id");
        const price = Number($card.data("price")) || 0;
        addToCart(bookId, 1, price);
    });
}

function addToCart(bookId, quantity, unitPrice) {


    if (!Auth.isLoggedIn()) {
        showToast("Please sign in to add items to your cart");
        window.location.href = "login.html?redirect=" + encodeURIComponent(window.location.pathname + window.location.search);
        return;
    }

    api.post("/carts/items", { bookId: bookId, quantity: quantity })
        .done(() => { showToast("Added to cart"); bumpLocalCount("potha_cart_count", quantity); bumpCartTotal(unitPrice * quantity); })
        .fail(xhr => {
            if (xhr.status === 401 || xhr.status === 403) {
                showToast("Your session has expired - please sign in again");
                Auth.logout();
                return;
            }
            showToast("Couldn't add to cart - is the backend running?");
        });
}

function addToWishlist(bookId) {
    if (!Auth.isLoggedIn()) {
        showToast("Please sign in to save items to your wishlist");
        window.location.href = "login.html?redirect=" + encodeURIComponent(window.location.pathname + window.location.search);
        return;
    }

    api.post("/wishlists/items", { bookId: bookId })
        .done(() => { showToast("Saved to wishlist"); bumpLocalCount("potha_wishlist_count", 1); })
        .fail(xhr => {
            if (xhr.status === 401 || xhr.status === 403) {
                showToast("Your session has expired - please sign in again");
                Auth.logout();
                return;
            }
            showToast("Couldn't save to wishlist - is the backend running?");
        });
}

function escapeHtml(str) {
    return $("<div>").text(str || "").html();
}