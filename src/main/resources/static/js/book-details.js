/* ==========================================================================
   book-details.js
   Real endpoints:
     GET  /books/{id}              -> book detail incl. images, publisher, author, stock
     GET  /reviews?bookId={id}     -> reviews list
     POST /reviews                 -> { bookId, rating, comment }  (requires login)
     POST /carts/items             -> { bookId, quantity }
     POST /wishlists/items         -> { bookId }
   ========================================================================== */

const DEMO_BOOK = {
    id: 1, title: "The Silence of the Sea", author: "K. Jayatilaka", publisher: "Godage International",
    category: "Fiction", price: 1450, specialPrice: 1190, rating: 4, inStock: true,
    description: "A quiet coastal town, a family secret decades old, and the tide that finally brings it in. K. Jayatilaka's most acclaimed novel follows three generations of the Abeywardena family as they confront what the sea has kept hidden.",
    reviews: [
        { name: "Dilani W.", rating: 5, comment: "Beautifully written, couldn't put it down.", date: "2 weeks ago" },
        { name: "Ruwan P.", rating: 4, comment: "Slow start but the ending made it worth it.", date: "1 month ago" },
    ]
};

let currentBook = null;
let currentQty = 1;

$(document).ready(function () {
    initLayout();

    const bookId = new URLSearchParams(window.location.search).get("id") || 1;

    api.get("/books/" + bookId)
        .done(res => renderBook(res.body))
        .fail(() => renderBook(DEMO_BOOK));

    api.get("/reviews?bookId=" + bookId)
        .done(res => renderReviews(res.body || []))
        .fail(() => renderReviews(DEMO_BOOK.reviews));

    $("#qty-minus").on("click", () => setQty(currentQty - 1));
    $("#qty-plus").on("click", () => setQty(currentQty + 1));

    $("#add-to-cart-btn").on("click", function () {
        const unitPrice = currentBook ? (currentBook.specialPrice && currentBook.specialPrice < currentBook.price ? currentBook.specialPrice : currentBook.price) : 0;
        api.post("/carts/items", { bookId: bookId, quantity: currentQty })
            .done(() => { showToast(`Added ${currentQty} to cart`); bumpLocalCount("potha_cart_count", currentQty); bumpCartTotal(unitPrice * currentQty); })
            .fail(() => { showToast(`Added ${currentQty} to cart (demo mode)`); bumpLocalCount("potha_cart_count", currentQty); bumpCartTotal(unitPrice * currentQty); });
    });

    $("#wishlist-btn").on("click", function () {
        $(this).toggleClass("btn-primary btn-outline");
        api.post("/wishlists/items", { bookId: bookId })
            .done(() => { showToast("Saved to wishlist"); bumpLocalCount("potha_wishlist_count", 1); })
            .fail(() => { showToast("Saved to wishlist (demo mode)"); bumpLocalCount("potha_wishlist_count", 1); });
    });

    $("#review-submit").on("click", function () {
        if (!Auth.requireLogin()) return;
        const payload = { bookId: bookId, rating: Number($("#review-rating").val()), comment: $("#review-comment").val().trim() };
        if (!payload.comment) { showToast("Write a short comment first"); return; }

        api.post("/reviews", payload)
            .done(() => { showToast("Review posted"); location.reload(); })
            .fail(() => showToast("Could not post review - is the backend running?"));
    });
});

function setQty(n) {
    currentQty = Math.max(1, n);
    $("#qty-value").text(currentQty);
}

function renderBook(book) {
    currentBook = book;
    const hasDiscount = book.specialPrice && book.specialPrice < book.price;

    document.title = book.title + " — Potha";
    $("#breadcrumb-category").text(book.category || "");
    $("#breadcrumb-title").text(book.title);
    $("#book-category").text(book.category || "");
    $("#book-title").text(book.title);
    $("#book-author").text(book.author);
    $("#book-publisher").text(book.publisher || "Independent");
    $("#book-description").text(book.description || "No description available yet.");
    $("#book-rating").html(`<span class="stars">${starString(book.rating)}</span>&nbsp;(${(book.reviews || []).length || book.reviewCount || 0} reviews)`);
    $("#gallery-main").text(book.title);

    $("#book-price-special").text(formatLKR(hasDiscount ? book.specialPrice : book.price));
    $("#book-price-original").text(hasDiscount ? formatLKR(book.price) : "");

    if (book.inStock === false) {
        $("#stock-badge").removeClass("badge-success").addClass("badge-danger").text("Out of Stock");
        $("#add-to-cart-btn").prop("disabled", true).text("Out of Stock");
    } else {
        $("#stock-badge").removeClass("badge-danger").addClass("badge-success").text("In Stock");
    }
}

function renderReviews(reviews) {
    const $list = $("#review-list").empty();
    $("#review-empty").toggle(reviews.length === 0);
    reviews.forEach(r => {
        $list.append(`
            <div class="review-item">
                <div class="review-head">
                    <span class="review-name">${escapeHtml(r.name || r.userName || "Reader")}</span>
                    <span class="stars">${starString(r.rating)}</span>
                    <span class="review-date">${escapeHtml(r.date || "")}</span>
                </div>
                <p style="margin:0;color:var(--text-muted);font-size:13.5px">${escapeHtml(r.comment)}</p>
            </div>
        `);
    });
}

function escapeHtml(str) {
    return $("<div>").text(str || "").html();
}
