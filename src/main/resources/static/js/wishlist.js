/* ==========================================================================
   wishlist.js
   ========================================================================== */

$(document).ready(function () {
    initLayout();
    if (!Auth.requireLogin()) return;

    api.get("/wishlists")
        .done(res => renderWishlist((res.body && res.body.items) || []))
        .fail(xhr => {
            if (xhr.status === 401 || xhr.status === 403) {
                showToast("Your session has expired - please sign in again");
                Auth.logout();
                return;
            }
            showToast("Couldn't load your wishlist - is the backend running?");
            renderWishlist([]);
        });
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
                        ${bookCoverContent(book) || escapeHtml(book.title)}
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
        const $btn = $(this).prop("disabled", true);
        const wishlistItemId = $(this).closest(".book-card").data("wishlist-item-id");
        api.post("/carts/items", { wishlistItemId: wishlistItemId })
            .done(() => { showToast("Moved to cart"); bumpLocalCount("potha_cart_count", 1); })
            .fail(xhr => {
                $btn.prop("disabled", false);
                if (xhr.status === 401 || xhr.status === 403) {
                    showToast("Your session has expired - please sign in again");
                    Auth.logout();
                    return;
                }
                const msg = (xhr.responseJSON && xhr.responseJSON.message) || "Couldn't move to cart - please try again";
                showToast(msg);
            });
    });

    $(".remove-wishlist-btn").off("click").on("click", function () {
        const $card = $(this).closest(".book-card");
        const wishlistItemId = $card.data("wishlist-item-id");

        api.del("/wishlists/items/" + wishlistItemId)
            .done(() => {
                $card.remove();
                bumpLocalCount("potha_wishlist_count", -1);
                $("#wishlist-empty").toggle($("#wishlist-grid").children().length === 0);
            })
            .fail(xhr => {
                if (xhr.status === 401 || xhr.status === 403) {
                    showToast("Your session has expired - please sign in again");
                    Auth.logout();
                    return;
                }
                // Removal failed server-side
                const msg = (xhr.responseJSON && xhr.responseJSON.message) || "Couldn't remove item - please try again";
                showToast(msg);
            });
    });
}

function escapeHtml(str) {
    return $("<div>").text(str || "").html();
}