/* ==========================================================================
   wishlist.js
   Real endpoints:
     GET    /wishlists              -> { items: [{ wishlistItemId, book }] }
     DELETE /wishlists/items/{id}
     POST   /carts/items            -> move to cart
   ========================================================================== */

const DEMO_WISHLIST = [
    { wishlistItemId: 1, id: 3, title: "Whispers of the Highlands", author: "N. Ranasinghe", category: "Poetry", price: 890, specialPrice: 690, rating: 4, reviewCount: 14, inStock: true, isNew: true },
    { wishlistItemId: 2, id: 9, title: "The Last Tea Estate", author: "M. Wickramasinghe", category: "Fiction", price: 1550, specialPrice: 1350, rating: 5, reviewCount: 48, inStock: true, isNew: false },
];

$(document).ready(function () {
    initLayout();
    if (!Auth.requireLogin()) return;

    api.get("/wishlists")
        .done(res => renderWishlist((res.body && res.body.items) || []))
        .fail(() => renderWishlist(DEMO_WISHLIST));
});

function renderWishlist(items) {
    const $grid = $("#wishlist-grid").empty();
    $("#wishlist-empty").toggle(items.length === 0);

    items.forEach(book => {
        const hasDiscount = book.specialPrice && book.specialPrice < book.price;
        const priceHtml = hasDiscount
            ? `<span class="price-special">${formatLKR(book.specialPrice)}</span><span class="price-original">${formatLKR(book.price)}</span>`
            : `<span class="price-special">${formatLKR(book.price)}</span>`;

        $grid.append(`
            <div class="book-card" data-wishlist-item-id="${book.wishlistItemId}">
                <a href="book-details.html?id=${book.id}">
                    <div class="book-cover">
                        ${book.inStock ? "" : '<span class="ribbon stock-out">Out of Stock</span>'}
                        ${escapeHtml(book.title)}
                    </div>
                    <div class="book-info">
                        <div class="book-category">${escapeHtml(book.category || "")}</div>
                        <div class="book-title">${escapeHtml(book.title)}</div>
                        <div class="book-author">by ${escapeHtml(book.author)}</div>
                        <div class="price-row">${priceHtml}</div>
                    </div>
                </a>
                <div class="book-actions" style="display:flex;gap:8px">
                    <button class="btn btn-primary btn-sm move-to-cart-btn" style="flex:1" ${!book.inStock ? "disabled" : ""}>Move to Cart</button>
                    <button class="btn btn-outline btn-sm remove-wishlist-btn" aria-label="Remove">&times;</button>
                </div>
            </div>
        `);
    });

    bindWishlistEvents();
}

function bindWishlistEvents() {
    $(".move-to-cart-btn").off("click").on("click", function () {
        const wishlistItemId = $(this).closest(".book-card").data("wishlist-item-id");
        api.post("/carts/items", { wishlistItemId: wishlistItemId })
            .done(() => showToast("Moved to cart"))
            .fail(() => showToast("Moved to cart (demo mode)"));
        bumpLocalCount("potha_cart_count", 1);
    });

    $(".remove-wishlist-btn").off("click").on("click", function () {
        const $card = $(this).closest(".book-card");
        const wishlistItemId = $card.data("wishlist-item-id");
        $card.remove();
        bumpLocalCount("potha_wishlist_count", -1);
        $("#wishlist-empty").toggle($("#wishlist-grid").children().length === 0);
        api.del("/wishlists/items/" + wishlistItemId).fail(() => {});
    });
}

function escapeHtml(str) {
    return $("<div>").text(str || "").html();
}
