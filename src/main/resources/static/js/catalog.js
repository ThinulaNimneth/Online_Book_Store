/* ==========================================================================
   catalog.js - homepage / full catalog.
   Real endpoints to implement on the backend:
     GET /books                    -> full catalog (bottom grid)
     GET /books?keyword=...        -> search
     GET /books?category=...       -> filter by category
     GET /books/new-arrivals       -> New Arrivals carousel
     GET /books/bestsellers?range=7d   -> Bestseller (Last 7 Days) carousel
     GET /books/bestsellers?range=all  -> Bestseller (All Time) carousel  (bonus ranking feature)
     GET /books/trending            -> Trending grid
   Until those exist, this falls back to DEMO_BOOKS below so the page still
   looks and behaves correctly - delete the fallback once your API is live.
   ========================================================================== */

const DEMO_BOOKS = [
    { id: 1, title: "The Silence of the Sea", author: "K. Jayatilaka", category: "Fiction", price: 1450, specialPrice: 1190, rating: 4, reviewCount: 32, inStock: true, isNew: true },
    { id: 2, title: "Mathematics for Grade 11", author: "Dept. of Education", category: "Education", price: 620, specialPrice: null, rating: 5, reviewCount: 58, inStock: true, isNew: false },
    { id: 3, title: "Whispers of the Highlands", author: "N. Ranasinghe", category: "Poetry", price: 890, specialPrice: 690, rating: 4, reviewCount: 14, inStock: true, isNew: true },
    { id: 4, title: "The Merchant's Ledger", author: "S. Fernando", category: "Biography", price: 1750, specialPrice: null, rating: 3, reviewCount: 9, inStock: false, isNew: false },
    { id: 5, title: "Little Star's Big Journey", author: "A. Perera", category: "Children's", price: 540, specialPrice: 430, rating: 5, reviewCount: 41, inStock: true, isNew: true },
    { id: 6, title: "Paths of the Dhamma", author: "Ven. W. Sumana", category: "Religious", price: 780, specialPrice: null, rating: 5, reviewCount: 67, inStock: true, isNew: false },
    { id: 7, title: "Colombo Nights", author: "R. de Silva", category: "Fiction", price: 1290, specialPrice: 990, rating: 4, reviewCount: 22, inStock: true, isNew: false },
    { id: 8, title: "Science for Grade 9", author: "Dept. of Education", category: "Education", price: 590, specialPrice: null, rating: 4, reviewCount: 30, inStock: true, isNew: false },
    { id: 9, title: "The Last Tea Estate", author: "M. Wickramasinghe", category: "Fiction", price: 1550, specialPrice: 1350, rating: 5, reviewCount: 48, inStock: true, isNew: false },
    { id: 10, title: "A Grandmother's Kitchen", author: "P. Gunasekara", category: "Biography", price: 990, specialPrice: null, rating: 4, reviewCount: 17, inStock: true, isNew: true },
    { id: 11, title: "The Vanished Monsoon", author: "T. Bandara", category: "Fiction", price: 1320, specialPrice: 1050, rating: 4, reviewCount: 26, inStock: true, isNew: false },
    { id: 12, title: "Verses at Dawn", author: "H. Karunaratne", category: "Poetry", price: 640, specialPrice: 510, rating: 5, reviewCount: 19, inStock: true, isNew: false },
];

$(document).ready(function () {
    initLayout();

    const params = new URLSearchParams(window.location.search);
    const query = params.get("q");
    const category = params.get("category");

    loadCarousel("#new-arrivals-row", "#new-arrivals-dots", "/books/new-arrivals", () => shuffled().slice(0, 6));
    loadCarousel("#bestsellers-7d-row", "#bestsellers-7d-dots", "/books/bestsellers?range=7d", () => shuffled().slice(0, 6));
    loadCarousel("#bestsellers-all-row", "#bestsellers-all-dots", "/books/bestsellers?range=all", () => shuffled().slice(0, 6));
    loadTrending();
    loadCatalog(query, category);

    if (query) {
        $("#catalog-eyebrow").text("Search results");
        $("#catalog-heading").text('Results for "' + query + '"');
    } else if (category) {
        $("#catalog-eyebrow").text("Category");
        $("#catalog-heading").text(category.charAt(0).toUpperCase() + category.slice(1));
    }
});

function shuffled() {
    return DEMO_BOOKS.slice().sort(() => Math.random() - 0.5);
}

function loadCarousel(rowSelector, dotsSelector, endpoint, demoFallback) {
    api.get(endpoint)
        .done(res => renderCarousel(rowSelector, dotsSelector, res.body || []))
        .fail(() => renderCarousel(rowSelector, dotsSelector, demoFallback()));
}

function loadTrending() {
    api.get("/books/trending")
        .done(res => renderBooks("#trending-grid", res.body || []))
        .fail(() => renderBooks("#trending-grid", shuffled().slice(0, 6)));
}

function loadCatalog(query, category) {
    let endpoint = "/books";
    const qs = [];
    if (query) qs.push("keyword=" + encodeURIComponent(query));
    if (category) qs.push("category=" + encodeURIComponent(category));
    if (qs.length) endpoint += "?" + qs.join("&");

    api.get(endpoint)
        .done(res => renderCatalogResult(res.body || []))
        .fail(() => {
            let demo = DEMO_BOOKS;
            if (query) {
                const q = query.toLowerCase();
                demo = demo.filter(b => b.title.toLowerCase().includes(q) || b.author.toLowerCase().includes(q));
            }
            if (category) {
                demo = demo.filter(b => b.category.toLowerCase().replace(/[^a-z]/g, "") === category.toLowerCase().replace(/[^a-z]/g, ""));
            }
            renderCatalogResult(demo);
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

/**
 * Ranked horizontal carousel (New Arrivals / Bestseller Last 7 Days / All Time)
 * - numbered rank badge on each card, matching Grantha's bestseller ranking UX
 * - dot controls scroll the row instead of paginating a real slider
 */
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
    // /carts/items requires a logged-in user on the backend (SecurityConfig ->
    // .anyRequest().authenticated()). Check client-side first so guests get a
    // clear "please sign in" prompt instead of a fake "Added to cart" toast
    // that only ever updated a local counter and never touched a real cart.
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