

$(document).ready(function () {
    initLayout();

    const params = new URLSearchParams(window.location.search);
    const category = (params.get("category") || "").trim();

    if (!category) {
        // No category given
        // showing a blank/broken page.
        window.location.href = "index.html";
        return;
    }

    $("#catalog-eyebrow").text("Category");
    $("#catalog-heading").text(category);
    document.title = category + " Books \u2014 Readora";

    loadCategoryBooks(category);
});

function loadCategoryBooks(category) {
    api.get("/books?category=" + encodeURIComponent(category))
        .done(res => renderCategoryResult(res.body || []))
        .fail(() => {
            showToast("Couldn't reach the category endpoint - is the backend running?");
            renderCategoryResult([]);
        });
}

function renderCategoryResult(books) {
    renderCategoryBooks("#catalog-grid", books);
    $("#catalog-empty").toggle(books.length === 0);
}

function renderCategoryBooks(gridSelector, books) {
    const $grid = $(gridSelector);
    $grid.empty();
    books.forEach(book => $grid.append(categoryBookCardHtml(book)));
    bindCategoryBookCardEvents();
}

function categoryBookCardHtml(book) {
    const hasDiscount = book.specialPrice && book.specialPrice < book.price;
    const ribbon = !book.inStock
        ? '<span class="ribbon stock-out">Out of Stock</span>'
        : (book.isNew ? '<span class="ribbon">New</span>'
            : (hasDiscount ? '<span class="ribbon">-' + Math.round((1 - book.specialPrice / book.price) * 100) + '%</span>' : ''));

    const priceHtml = hasDiscount
        ? `<span class="price-special">${formatLKR(book.specialPrice)}</span><span class="price-original">${formatLKR(book.price)}</span>`
        : `<span class="price-special">${formatLKR(book.price)}</span>`;

    const reviewCount = book.reviewCount ? `<span class="review-count">(${book.reviewCount})</span>` : "";

    return `
    <div class="book-card" data-book-id="${book.id}" data-price="${hasDiscount ? book.specialPrice : book.price}">
        <a href="book-details.html?id=${book.id}">
            <div class="book-cover">
                ${ribbon}
                <button class="wishlist-toggle" data-book-id="${book.id}" aria-label="Save to wishlist">
                    <svg viewBox="0 0 24 24" fill="none"><path d="M12 20s-7-4.35-9.5-8.5C.7 8.1 2.4 4.5 6 4.5c2 0 3.4 1.1 4 2.2.6-1.1 2-2.2 4-2.2 3.6 0 5.3 3.6 3.5 7C19 15.65 12 20 12 20z" stroke="currentColor" stroke-width="1.8"/></svg>
                </button>
                ${escapeHtmlCat(book.title)}
            </div>
            <div class="book-info">
                <div class="book-category">${escapeHtmlCat(book.category || "")}</div>
                <div class="book-title">${escapeHtmlCat(book.title)}</div>
                <div class="book-author">by ${escapeHtmlCat(book.author)}</div>
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

function bindCategoryBookCardEvents() {
    $(".wishlist-toggle").off("click").on("click", function (e) {
        e.preventDefault();
        e.stopPropagation();
        const bookId = $(this).data("book-id");
        $(this).toggleClass("active");
        addToWishlistFromCategory(bookId);
    });

    $(".add-to-cart-btn").off("click").on("click", function (e) {
        e.preventDefault();
        e.stopPropagation();
        const $card = $(this).closest(".book-card");
        const bookId = $card.data("book-id");
        const price = Number($card.data("price")) || 0;
        addToCartFromCategory(bookId, 1, price);
    });
}

function addToCartFromCategory(bookId, quantity, unitPrice) {
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

function addToWishlistFromCategory(bookId) {
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

function escapeHtmlCat(str) {
    return $("<div>").text(str || "").html();
}
